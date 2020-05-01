package org.jenkins_cli.plugins.ifdtms.util;

import java.net.URL;
import java.net.URLConnection;


public class UrlValidator {

    public static boolean isValidUrl(String string) {

        try {
			URL url = new URL(string);
			URLConnection conn = url.openConnection();
            conn.connect();

        } catch (Exception e) {
            return false;
        }
        return true;
    }

}
