package org.jenkinsci.plugins.environmentdashboard;

import hudson.Extension;
import hudson.Util;
import hudson.model.*;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class DeploymentView extends ListView {
    @DataBoundConstructor
    public DeploymentView(String name) {
        super(name);
    }

    public Map<String, Deployment.DeploymentAction> getEnvironments(TopLevelItem item) {
        //org.jenkinsci.plugins.workflow.job.WorkflowJob
        //org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject
        System.out.println("Item class: " + item.getClass().toString());

        List<WorkflowRun> runs = Collections.emptyList();
        if (item instanceof WorkflowMultiBranchProject) {
            runs = ((WorkflowMultiBranchProject) item)
                    .getItems()
                    .stream()
                    .peek(job -> System.out.println(": " + job.getDisplayName()))
                    .map(Job::getBuilds)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        } else if (item instanceof WorkflowJob) {
            runs = ((WorkflowJob) item).getBuilds();
        }



//        if (!(item instanceof Job)) {
//            return new TreeMap<>();
//        }

        if (!(item instanceof Job) && !(item instanceof WorkflowMultiBranchProject)) {
            return new TreeMap<>();
        }


//        List<Run> runs = ((Job) item).getBuilds();
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
        System.out.println("Item2 class: " + item.getClass().toString());
        if (!(item instanceof Job) && !(item instanceof WorkflowMultiBranchProject)) {
            return new ArrayList<>();
        }

        List<WorkflowRun> runs = Collections.emptyList();
        if (item instanceof WorkflowMultiBranchProject) {
            runs = ((WorkflowMultiBranchProject) item)
                    .getItems()
                    .stream()
                    .peek(job -> System.out.println(": " + job.getDisplayName()))
                    .map(Job::getBuilds)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        } else if (item instanceof WorkflowJob) {
            runs = ((WorkflowJob) item).getBuilds();
        }


        System.out.println("jobbbbb");

        List<Deployment.DeploymentAction> deployments = new ArrayList<>();
//        List<Run> runs = ((Job) item).getBuilds();

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
