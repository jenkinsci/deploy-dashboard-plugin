package org.jenkinsci.plugins.environmentdashboard;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import java.io.IOException;
import jenkins.model.RunAction2;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public class Deployment extends Builder implements SimpleBuildStep {

    private final String env;
    private final String buildNumber;

    @DataBoundConstructor
    public Deployment(String env, String buildNumber) {
        this.env = env;
        this.buildNumber = buildNumber;
    }

    public String getEnv() {
        return env;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    @Override
    public void perform(
            @NonNull Run<?, ?> run,
            @NonNull FilePath workspace,
            @NonNull EnvVars environment,
            @NonNull Launcher launcher,
            @NonNull TaskListener listener
    ) throws InterruptedException, IOException {
        run.addAction(new DeploymentAction(
                env,
                buildNumber
        ));
    }

    @Extension
    @Symbol("addDeployToDashboard")
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {
        @Override
        @NonNull
        public String getDisplayName() {
            return "Deployment";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> t) {
            return true;
        }
    }

    public static final class DeploymentAction implements RunAction2 {

        private Run run;
        private String env;
        private String buildNumber;

        public DeploymentAction(String env, String buildNumber) {
            this.env = env;
            this.buildNumber = buildNumber;
        }

        @Override
        public String getIconFileName() {
            return null;
        }

        @Override
        public String getDisplayName() {
            return String.format(
                    "Deployment %s to %s",
                    buildNumber,
                    env
            );
        }

        @Override
        public String getUrlName() {
            return null;
        }

        public String getBuildNumber() {
            return buildNumber;
        }

        public String getEnv() {
            return env;
        }

        public Run getRun() {
            return run;
        }

        @Override
        public void onLoad(Run<?, ?> r) {
            this.run = r;
        }

        @Override
        public void onAttached(Run<?, ?> r) {
            this.run = r;
        }
    }
}
