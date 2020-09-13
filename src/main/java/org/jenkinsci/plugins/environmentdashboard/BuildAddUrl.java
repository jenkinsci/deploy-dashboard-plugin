package org.jenkinsci.plugins.environmentdashboard;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.*;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class BuildAddUrl extends Builder implements SimpleBuildStep {

    private final String title;
    private final String url;
    private String users;
    private String groups;
    
    @DataBoundConstructor
    public BuildAddUrl(String title, String url) {
        this.url = url;
        this.title = title;
    }
    
    @DataBoundSetter
    public void setUsers(String users) {
        this.users = users;
    }

    @DataBoundSetter
    public void setGroups(String groups) {
        this.groups = groups;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getUsers() {
        return users;
    }

    public String getGroups() {
        return groups;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public void perform(
            @Nonnull Run<?, ?> run,
            @Nonnull FilePath workspace,
            @Nonnull Launcher launcher,
            @Nonnull TaskListener listener
    ) throws InterruptedException, IOException {
        run.addAction(new BuildUrlAction(title, url, users, groups));
    }

    @Extension
    @Symbol("buildAddUrl")
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {
        @Override
        @Nonnull
        public String getDisplayName() {
            return "Build Add Url";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> t) {
            return true;
        }
    }

    public static class BuildUrlAction implements Action {
        private final String title;
        private final String url;
        private final ArrayList<String> userList = new ArrayList<String>();
        private final ArrayList<String> groupList = new ArrayList<String>();

        BuildUrlAction(String title, String url, String users, String groups) {
            this.title = title;
            this.url = url;
            if (users != null && !"".equals(users)) {
                this.userList.addAll(Arrays.asList(users.split(",")));
            } 
            if (groups != null && !"".equals(groups)) {
                this.groupList.addAll(Arrays.asList(groups.split(",")));
            }
        }

        @Override
        public String getIconFileName() {
            String iconFileName = String.format("/plugin/%s/deploy.png", getClass().getPackage().getImplementationTitle());

            User currentUser = User.current();
            if (currentUser == null) return null;

            String currentUserId = currentUser.getId();
            List<String> currentUserGroups = currentUser.getAuthorities();

            if (!checkPermissions() || isUserInList(currentUserId) || isUserInGroup(currentUserGroups)) 
                return iconFileName;

            return null;
        }

        private boolean checkPermissions() {
            return userList.size() > 0 || groupList.size() > 0;
        }

        private boolean isUserInList(String userId) {
            return userList.contains(userId);
        }

        private boolean isUserInGroup(List<String> userGroups) {
            for(String userGroup: userGroups) 
                if (groupList.contains(userGroup))
                    return true;
            return false;
        }

        @Override
        public String getDisplayName() {
            return title;
        }

        @Override
        public String getUrlName() {
            return url;
        }
    }
}
