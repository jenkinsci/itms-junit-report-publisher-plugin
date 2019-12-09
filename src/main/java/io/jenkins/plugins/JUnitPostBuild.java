package io.jenkins.plugins;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import io.jenkins.plugins.model.AuthenticationInfo;
import io.jenkins.plugins.rest.RequestAPI;
import io.jenkins.plugins.rest.StandardResponse;

import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.jenkins.plugins.model.ITMSConst.*;


public class JUnitPostBuild extends Notifier {

    private final String itmsAddress;
    private final String reportFolder;
    private final String jiraProjectKey;
    private final String jiraTicketKey;
    private final String itmsCycleName;

    @DataBoundConstructor
    public JUnitPostBuild(final String itmsAddress, final String reportFolder,
                          final String jiraProjectKey, final String jiraTicketKey, final String itmsCycleName) {
        this.itmsAddress = itmsAddress.trim();
        this.reportFolder = reportFolder.trim();
        this.jiraProjectKey = jiraProjectKey.trim();
        this.jiraTicketKey = jiraTicketKey.trim();
        this.itmsCycleName = itmsCycleName.trim();
    }

    @Override
    public boolean perform(final AbstractBuild build, final Launcher launcher, final BuildListener listener) {
        int counter = 0;
        try {
            listener.getLogger().println("Starting " + PLUGIN_NAME + " post build action..");

            File folder = new File(build.getWorkspace() + reportFolder);
            listener.getLogger().println("Report folder: " + folder.getPath());
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    if (file.getName().toLowerCase().endsWith(".xml")) {
                        counter++;
                        listener.getLogger().println("Read report file: " + file.getName());
                        listener.getLogger().println(sendReportContent(file, build));
                    }
                }

                if (counter < 1) {
                    listener.getLogger().println("Report file not found! Check your report folder and format type");
                }
            } else {
                listener.getLogger().println("Folder is empty!");
            }

        } catch (Exception e) {
            listener.getLogger().printf("Error Occurred : %s ", e);
        }
        listener.getLogger().println("Finished " + PLUGIN_NAME + " post build action");
        return true;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public JUnitGlobalConfiguration getDescriptor() {
        return (JUnitGlobalConfiguration) super.getDescriptor();
    }


    private StandardResponse prepareRequestContent(File file, AbstractBuild build) {

//        Cause cause = (Cause) build.getCauses().get(0);
//        String userCause = ((Cause.UserIdCause) cause).getUserId();
        AuthenticationInfo authenticationInfo = getDescriptor().getAuthenticationInfo();

        Map<String, String> postData = new HashMap<>();
        postData.put(USER_NAME_PARAM, authenticationInfo.getUsername());
        postData.put(SERVICE_NAME_PARAM, SERVICE_NAME);
        postData.put(PROJECT_NAME_PARAM, jiraProjectKey);
        postData.put(ATTRIBUTE_BUILD_NUM_PARAM, String.valueOf(build.number));
        postData.put(ATTRIBUTE_BUILD_STATUS_PARAM, Objects.requireNonNull(build.getResult()).toString().toLowerCase());
        postData.put(ATTRIBUTE_USER_PARAM, authenticationInfo.getUsername());
        postData.put(ATTRIBUTE_REPORT_TYPE_PARAM, JUNIT_REPORT_TYPE);
        postData.put(TICKET_KEY_PARAM, jiraTicketKey);
        postData.put(CYCLE_NAME_PARAM, itmsCycleName);
        postData.put(IS_JSON_PARAM, String.valueOf(false));

        RequestAPI requestAPI = new RequestAPI();
        return requestAPI.sendReportToITMS(itmsAddress, authenticationInfo.getToken(), postData, file, false);
    }

    private String sendReportContent(File file, AbstractBuild build) {
        if (file.length() > 0) {
            StandardResponse response = prepareRequestContent(file, build);
            return PLUGIN_NAME + " response: " + response.getMessage();
        }
        return file.getName() + " is empty!";
    }

    public String getItmsAddress() {
        return itmsAddress;
    }

    public String getReportFolder() {
        return reportFolder;
    }

    public String getJiraTicketKey() {
        return jiraTicketKey;
    }

    public String getItmsCycleName() {
        return itmsCycleName;
    }

    public String getJiraProjectKey() {
        return jiraProjectKey;
    }

}
