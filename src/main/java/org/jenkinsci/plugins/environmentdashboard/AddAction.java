package org.jenkinsci.plugins.environmentdashboard;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.*;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Serializable;

public class AddAction extends Recorder implements SimpleBuildStep, Serializable {

    final private String title;
    final private String url;
    private static final long serialVersionUID = 42L;

    @DataBoundConstructor
    public AddAction(String title, String url) {
        this.url = url;
        this.title = title;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace, @Nonnull Launcher launcher, @Nonnull TaskListener listener) throws InterruptedException, IOException {
        run.addAction(new UrlAction(title, run.getEnvironment(listener).expand(url)));
    }

    public class UrlAction implements Action {
        final private String title;
        final private String url;

        public UrlAction(String title, String url) {
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
