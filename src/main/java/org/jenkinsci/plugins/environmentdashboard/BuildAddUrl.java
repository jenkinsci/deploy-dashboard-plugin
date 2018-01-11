package org.jenkinsci.plugins.environmentdashboard;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.*;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;

public class BuildAddUrl extends Builder implements SimpleBuildStep {

    private final String title;
    private final String url;

    @DataBoundConstructor
    public BuildAddUrl(String title, String url) {
        this.url = url;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
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
        run.addAction(new BuildUrlAction(title, url));
    }

    @Extension
    @Symbol("buildAddUrl")
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {
        @Override
        public String getDisplayName() {
            return "";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> t) {
            return true;
        }
    }

    public class BuildUrlAction implements Action {
        final private String title;
        final private String url;

        BuildUrlAction(String title, String url) {
            this.title = title;
            this.url = url;
        }

        @Override
        public String getIconFileName() {
            return "/plugin/environment-dashboard/deploy.png";
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
