package io.jenkins.plugins;

import hidden.jth.org.apache.http.HttpStatus;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import io.jenkins.plugins.model.AuthenticationInfo;
import io.jenkins.plugins.rest.RequestAPI;
import io.jenkins.plugins.rest.StandardResponse;
import io.jenkins.plugins.util.URLValidator;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.verb.POST;

import javax.annotation.Nonnull;

import static io.jenkins.plugins.model.ITMSConst.*;


@Extension
public final class CucumberGlobalConfiguration extends BuildStepDescriptor<Publisher> {

    private String itmsServer;
    private Secret username;
    private Secret token;
    private AuthenticationInfo authenticationInfo = new AuthenticationInfo();

    /**
     * In order to load the persisted global configuration, you have to call
     * load() in the constructor.
     */
    public CucumberGlobalConfiguration() {
        super(CucumberPostBuild.class);
        load();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject formData)
            throws FormException {
        req.bindJSON(this, formData);
        // To persist global configuration information, set that to
        // properties and call save().
        itmsServer = formData.getString("itmsServer");
        username = Secret.fromString(formData.getString("username"));
        token = Secret.fromString(formData.getString("token"));

        authenticationInfo.setUsername(username);
        authenticationInfo.setToken(token);
        save();
        return super.configure(req, formData);
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
        return true;
    }

    @Nonnull
    @Override
    public String getDisplayName() {
        return POST_BUILD_NAME;
    }

    @POST
    public FormValidation doTestConnection(@QueryParameter String itmsServer, @QueryParameter String username,
                                           @QueryParameter String token) {

        Jenkins.get().checkPermission(Jenkins.ADMINISTER);
        if (StringUtils.isBlank(itmsServer)) {
            return FormValidation.error("Please enter the iTMS server address");
        }

        if (StringUtils.isBlank(username)) {
            return FormValidation.error("Please enter the username");
        }

        if (StringUtils.isBlank(token)) {
            return FormValidation.error("Please enter the token");
        }

        JSONObject postData = new JSONObject();
        postData.put("username", username);
        postData.put("service_name", "jenkins");

        RequestAPI request = new RequestAPI();
        StandardResponse response = request.sendAuthRequest(itmsServer, token, postData);

        if (response.getCode() != HttpStatus.SC_OK) {
            return FormValidation.error(response.getMessage());
        }

        return FormValidation.ok("Connection to iTMS has been validated");
    }

    @POST
    public FormValidation doTestConfiguration(@QueryParameter String itmsAddress, @QueryParameter String reportFolder,
                                              @QueryParameter String jiraProjectKey, @QueryParameter String jiraTicketKey, @QueryParameter String itmsCycleName) {

        Jenkins.get().checkPermission(Jenkins.ADMINISTER);
        if (StringUtils.isBlank(itmsAddress)) {
            return FormValidation.error("Please enter the iTMS server address");
        }

        if (!URLValidator.isValidUrl(itmsAddress)) {
            return FormValidation.error("This value is not a valid url!");
        }

        if (StringUtils.isBlank(reportFolder)) {
            return FormValidation.error("Please enter the report folder!");
        }

        if (!reportFolder.startsWith("/")) {
            return FormValidation.error("Please begin with forward slash! Ex: /target/report ");
        }

        if (StringUtils.isBlank(jiraProjectKey)) {
            return FormValidation.error("Please enter the Jira project key!");
        }

        if (StringUtils.isBlank(jiraTicketKey)) {
            return FormValidation.error("Please enter the Jira ticket key!");
        }

        if (StringUtils.isBlank(itmsCycleName)) {
            return FormValidation.error("Please enter the iTMS cycle name!");
        }

        return FormValidation.ok("Configuration is valid!");
    }

    public ListBoxModel doFillReportFormatItems(@QueryParameter String reportFormat) {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);
        ListBoxModel m = new ListBoxModel();
        m.add(JSON_FORMAT);
        m.add(XML_FORMAT);
        return m;
    }

    public String getItmsServer() {
        return itmsServer;
    }

    public String getUsername() {
        return Secret.toString(username);
    }

    public String getToken() {
        return Secret.toString(token);
    }

    public AuthenticationInfo getAuthenticationInfo() {
        return authenticationInfo;
    }


}