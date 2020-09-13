package org.jenkinsci.plugins.environmentdashboard;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DeploymentTableRow {
    private String job;
    private String environment;
    private String release;
    private String result;
    private String completed;

    DeploymentTableRow(String job, String environment, String release, String result, String completed) {
        this.job = job;
        this.environment = environment;
        this.release = release;
        this.result = result;
        this.completed = completed;
    }
}
