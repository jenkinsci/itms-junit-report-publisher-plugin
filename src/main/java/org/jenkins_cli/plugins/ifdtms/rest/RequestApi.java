package org.jenkins_cli.plugins.ifdtms.rest;

import hidden.jth.org.apache.http.HttpEntity;
import hidden.jth.org.apache.http.HttpResponse;
import hidden.jth.org.apache.http.client.methods.HttpPost;
import hidden.jth.org.apache.http.entity.StringEntity;
import hidden.jth.org.apache.http.impl.client.CloseableHttpClient;
import hidden.jth.org.apache.http.impl.client.HttpClientBuilder;
import net.sf.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

import static org.jenkins_cli.plugins.ifdtms.model.ItmsConst.*;

public class RequestApi {

    private CloseableHttpClient httpClient;
    URLConnection connection;

    public RequestApi() {
        httpClient = HttpClientBuilder.create().build();
    }

    public StandardResponse sendAuthRequest(String baseUrl, String token, JSONObject postData) {
        HttpPost request = new HttpPost(baseUrl);
        request.addHeader("content-type", "application/json");
        request.addHeader("Authorization", token);
        setBodyRequest(request, postData);

        return createPOSTRequest(request);
    }

    public StandardResponse sendPostRequestToItms(String baseUrl, String token, JSONObject postData) {
        HttpPost request = new HttpPost(baseUrl);
        request.addHeader("content-type", "application/json");
        request.addHeader("Authorization", token);
        setBodyRequest(request, postData);

        return createPOSTRequest(request);
    }

    private StandardResponse createPOSTRequest(HttpPost request) {
        StandardResponse response = new StandardResponse();
        try {
            HttpResponse httpResponse = httpClient.execute(request);
            response = readResponse(httpResponse);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    private void setBodyRequest(HttpPost request, JSONObject postData) {
        StringEntity params = null;
        try {
            params = new StringEntity(postData.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        request.setEntity(params);
    }

    private StandardResponse readResponse(HttpResponse httpResponse) {
        StringBuilder sb = null;
        try {
            HttpEntity entity = httpResponse.getEntity();
            InputStream is = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.ISO_8859_1), 8);
            sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line).append("\n");
            is.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert sb != null;
        return new StandardResponse(httpResponse.getStatusLine().getStatusCode(),
                httpResponse.getStatusLine().getReasonPhrase(), sb.toString());
    }

    public StandardResponse sendReportToITMS(String baseUrl, String token, Map<String, String> postData,
                                             String fileName, String reportContent, boolean isJsonReport) {
        // Just generate some unique random value.
        String boundary = Long.toHexString(System.currentTimeMillis());
        // Line separator required by multipart/form-data.
        String CRLF = "\r\n";

        try {
            connection = new URL(baseUrl).openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", token);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        } catch (IOException e) {
            e.printStackTrace();
        }


        try (OutputStream output = connection.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true)) {
            // Send normal param.
            postData.forEach((key, value) -> {
                writer.append("--").append(boundary).append(CRLF);
                writer.append("Content-Disposition: form-data; name=\"").append(key).append("\"").append(CRLF);
                writer.append(TEXT_PLAIN_TYPE).append(String.valueOf(StandardCharsets.UTF_8)).append(CRLF);
                writer.append(CRLF).append(value).append(CRLF).flush();
            });

            // Send file.
            writer.append("--").append(boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"report_content\"; filename=\"").append(fileName).append("\"").append(CRLF);
            // Text file itself must be saved in this charset!
            writer.append(isJsonReport ? APPLICATION_JSON_TYPE : APPLICATION_XML_TYPE + StandardCharsets.UTF_8).append(CRLF);
            writer.append(CRLF).flush();
            
            output.write(reportContent.getBytes("UTF-8"));
            
            // Important before continuing with writer!
            output.flush();
            // CRLF is important! It indicates end of boundary.
            writer.append(CRLF).flush();

            // End of multipart/form-data.
            writer.append("--").append(boundary).append("--").append(CRLF).flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Request is lazily fired whenever you need to obtain information about response.
        int responseCode = 0;
        String type = null;
        StringBuilder message = new StringBuilder();
        try {
            responseCode = ((HttpURLConnection) connection).getResponseCode();
            type = ((HttpURLConnection) connection).getResponseMessage();
            message.append(String.format("%d: %s: ", responseCode, type));
            if (((HttpURLConnection) connection).getErrorStream() != null) {
                InputStreamReader inputStreamReader = new InputStreamReader((((HttpURLConnection) connection).getErrorStream()), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(inputStreamReader);
                String output;
                while ((output = br.readLine()) != null) {
                    message.append(output);
                }
                inputStreamReader.close();
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new StandardResponse(responseCode, type, message.toString());
    }

}