package org.jenkinsci.plugins.environmentdashboard;

import hudson.Extension;
import hudson.Util;
import hudson.model.Job;
import hudson.model.ListView;
import hudson.model.TopLevelItem;
import hudson.model.ViewDescriptor;
import hudson.util.FormValidation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.environmentdashboard.Deployment.DeploymentAction;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class DeploymentView extends ListView {
    @DataBoundConstructor
    public DeploymentView(String name) {
        super(name);
    }

    private List<Unit.Environment> getEnvs(TopLevelItem item) {
        List<WorkflowRun> runs = Collections.emptyList();
        if (item instanceof WorkflowMultiBranchProject) {
            runs = ((WorkflowMultiBranchProject) item)
                    .getItems()
                    .stream()
                    .map(Job::getBuilds)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        } else if (item instanceof WorkflowJob) {
            runs = ((WorkflowJob) item).getBuilds();
        }

        return runs
                .stream()
                .map(run -> run.getActions(DeploymentAction.class))
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(DeploymentAction::getEnv))
                .entrySet()
                .stream()
                .map(e -> new Unit.Environment(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    public List<Unit> getUnits(List<? extends TopLevelItem> items) {
        return items
                .stream()
                .map(item -> new Unit(item, getEnvs(item)))
                .filter(unit -> !unit.getEnvironments().isEmpty())
                .collect(Collectors.toList());
    }

    @Getter
    @RequiredArgsConstructor
    public static class Unit {
        private final TopLevelItem job;
        private final List<Environment> environments;

        @Getter
        @RequiredArgsConstructor
        public static class Environment {
            private final String name;
            private final List<DeploymentAction> actions;

            public DeploymentAction getCurrentAction() {
                return actions.get(0);
            }
        }
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
