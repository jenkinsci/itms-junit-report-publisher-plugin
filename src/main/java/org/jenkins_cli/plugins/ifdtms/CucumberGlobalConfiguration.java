package org.jenkins_cli.plugins.ifdtms;

import hidden.jth.org.apache.http.HttpStatus;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import jenkins.model.Jenkins;

import org.jenkins_cli.plugins.ifdtms.model.AuthenticationInfo;
import org.jenkins_cli.plugins.ifdtms.rest.RequestApi;
import org.jenkins_cli.plugins.ifdtms.rest.StandardResponse;
import org.jenkins_cli.plugins.ifdtms.util.UrlValidator;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.verb.POST;

import javax.annotation.Nonnull;

import static org.jenkins_cli.plugins.ifdtms.model.ItmsConst.*;


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
        itmsServer = formData.getString(ITMS_SERVER_PARAM);
        username = Secret.fromString(formData.getString(USER_NAME_PARAM));
        token = Secret.fromString(formData.getString(TOKEN_PARAM));

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
        postData.put(USER_NAME_PARAM, username);
        postData.put(SERVICE_NAME_PARAM, SERVICE_NAME);

        RequestApi request = new RequestApi();
        StandardResponse response = request.sendAuthRequest(itmsServer, token, postData);

        if (response.getCode() != HttpStatus.SC_OK) {
            return FormValidation.error(response.getMessage());
        }

        return FormValidation.ok(response.getMessage());
    }

    @POST
    public FormValidation doTestConfiguration(@QueryParameter String itmsAddress, @QueryParameter String reportFolder,
                                              @QueryParameter String jiraProjectKey, @QueryParameter String jiraTicketKey, @QueryParameter String itmsCycleName) {

        Jenkins.get().checkPermission(Jenkins.ADMINISTER);
        if (StringUtils.isBlank(itmsAddress)) {
            return FormValidation.error("Please enter the iTMS server address");
        }

        if (!UrlValidator.isValidUrl(itmsAddress)) {
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
    
    @POST
    public FormValidation doCheckJiraConfiguration(@QueryParameter String itmsAddress,
    		@QueryParameter String jiraProjectKey, @QueryParameter String jiraTicketKey,
    		@QueryParameter String itmsCycleName) {

        Jenkins.get().checkPermission(Jenkins.ADMINISTER);
        if (StringUtils.isBlank(itmsAddress)) {
            return FormValidation.error("Please enter the iTMS server address");
        }

        if (!UrlValidator.isValidUrl(itmsAddress)) {
            return FormValidation.error("This value is not a valid url!");
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

        JSONObject postData = new JSONObject();
        postData.put(USER_NAME_PARAM, authenticationInfo.getUsername());
        postData.put(SERVICE_NAME_PARAM, SERVICE_NAME);
        postData.put(PROJECT_NAME_PARAM, jiraProjectKey);
        postData.put(TICKET_KEY_PARAM, jiraTicketKey);
        postData.put(CYCLE_NAME_PARAM, itmsCycleName);
        
        String itmsCheckUrl = itmsAddress.substring(0, itmsAddress.lastIndexOf("/"));
        itmsCheckUrl += "/validate_jenkins_job_configuration";

        RequestApi request = new RequestApi();
        StandardResponse response = request.sendPostRequestToItms(itmsCheckUrl, authenticationInfo.getToken(), postData);

        if (response.getCode() != HttpStatus.SC_OK) {
            return FormValidation.error(response.getMessage());
        }

        return FormValidation.ok(response.getMessage());
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