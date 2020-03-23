package org.jenkinsci.plugins.environmentdashboard;

import hudson.Extension;
import hudson.Util;
import hudson.model.*;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class DeploymentView extends ListView {
    @DataBoundConstructor
    public DeploymentView(String name) {
        super(name);
    }

    public Map<String, Deployment.DeploymentAction> getEnvironments(TopLevelItem item) {
        if (!(item instanceof Job)) {
            return new TreeMap<>();
        }
        List<Run> runs = ((Job) item).getBuilds();
        Map<String, Deployment.DeploymentAction> envs = new TreeMap<>();
        for (Run run : runs) {
            Deployment.DeploymentAction deployment = run.getAction(Deployment.DeploymentAction.class);

            if (deployment == null || envs.containsKey(deployment.getEnv())) {
                continue;
            }
            envs.put(deployment.getEnv(), deployment);
        }

        return envs;
    }

    public List<Deployment.DeploymentAction> getDeployments(String environment, TopLevelItem item) {
        if (!(item instanceof Job)) {
            return new ArrayList<>();
        }

        List<Deployment.DeploymentAction> deployments = new ArrayList<>();
        List<Run> runs = ((Job) item).getBuilds();

        for (Run run : runs) {
            Deployment.DeploymentAction deployment = run.getAction(Deployment.DeploymentAction.class);
            if (deployment == null || !deployment.getEnv().equals(environment)) {
                continue;
            }

            deployments.add(deployment);
        }

        return deployments;
    }

    @Extension
    public static class DeploymentViewDescriptor extends ViewDescriptor {
        public DeploymentViewDescriptor() {
            super(DeploymentView.class);
            load();
        }

        @Override
        @Nonnull
        public String getDisplayName() {
            return "Deployment View";
        }

        // Copy-n-paste from ListView$Descriptor as sadly we cannot inherit from that class
        public FormValidation doCheckIncludeRegex(@QueryParameter String value) {
            String v = Util.fixEmpty(value);
            if (v != null) {
                try {
                    Pattern.compile(v);
                } catch (PatternSyntaxException pse) {
                    return FormValidation.error(pse.getMessage());
                }
            }
            return FormValidation.ok();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            save();

            return true;
        }
    }
}
