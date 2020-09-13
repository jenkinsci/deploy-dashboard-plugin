package org.jenkinsci.plugins.environmentdashboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jenkinsci.plugins.environmentdashboard.Deployment.DeploymentAction;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;

import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TopLevelItem;
import io.jenkins.plugins.datatables.TableColumn;
import io.jenkins.plugins.datatables.TableConfiguration;
import io.jenkins.plugins.datatables.TableModel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class DeploymentTableModel extends TableModel {
    private String id;
    List<? extends TopLevelItem> items;
    
    DeploymentTableModel(String id) {
        this.id = id;
    }

    public TableModel populate(List<? extends TopLevelItem> items) {
        this.items = items;
        return this;
    }

    private String getStatusHTML(String altText, String imageUrl, String href) {
        // Issue with URL
        href = href.substring(href.indexOf("/job"));
        return new StringBuffer().append("<a href=\"")
            .append(href)
            .append("\"><img alt=\"")
            .append(altText)
            .append("\" src=\"")
            .append(imageUrl)
            .append("\" /></a>")
            .toString();
    }
    
    private String getPopupHTML(int row, int col, String envName) {
        return new StringBuffer().append("<a href=\"javascript:toggle('")
            .append(row).append("_").append(col)
            .append("');\">").append(envName).append("</a>")
            .toString();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public TableConfiguration getTableConfiguration() {
        return new DeploymentTableConfiguration();
    }

    @Override
    public List<TableColumn> getColumns() {
        List<TableColumn> columns = new ArrayList<TableColumn>();
        columns.add(new TableColumn("Job", "job"));
        columns.add(new TableColumn("Environment", "environment"));
        columns.add(new TableColumn("Release", "release"));
        columns.add(new TableColumn("Result", "result"));
        columns.add(new TableColumn("Completed", "completed"));
        return columns;
    }

    @Override
    public List<Object> getRows() {
        List<Object> rows = new ArrayList<Object>();

        int i=0;
        for (Unit unit: getUnits(items)) {
            int j=0;
            for (Unit.Environment env: unit.environments) {
                DeploymentAction action = env.getCurrentAction();
                Run run = action.getRun();
                rows.add(new DeploymentTableRow(unit.job.getName(), 
                    getPopupHTML(i, j, env.name), 
                    action.getBuildNumber(), 
                    getStatusHTML(run.getDescription(), 
                        run.getIconColor().getImageOf("32x32"), 
                        run.getUrl()),
                    run.getTimestampString() + " ago"));
                ++j;
            } 
            ++i;
        }
        return rows;
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
        return items.stream()
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
}