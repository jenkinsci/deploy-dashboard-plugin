package org.jenkinsci.plugins.environmentdashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.htmlunit.html.HtmlPage;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class DeploymentViewTest {

    @Test
    void viewRendersDeploymentsWithoutInlineJavascript(JenkinsRule j) throws Exception {
        WorkflowJob job = j.createProject(WorkflowJob.class, "app");
        job.setDefinition(new CpsFlowDefinition(
                "node { addDeployToDashboard(env: 'production', buildNumber: '1.2.3') }", true));
        WorkflowRun run = j.buildAndAssertSuccess(job);

        Deployment.DeploymentAction action = run.getAction(Deployment.DeploymentAction.class);
        assertNotNull(action);
        assertEquals("production", action.getEnv());
        assertEquals("1.2.3", action.getBuildNumber());

        DeploymentView view = new DeploymentView("deployments");
        j.jenkins.addView(view);
        view.setIncludeRegex(".*");
        view.save();

        List<DeploymentView.Unit> units = view.getUnits(view.getItems());
        assertEquals(1, units.size());
        assertEquals("production", units.get(0).getEnvironments().get(0).getName());
        assertEquals("1.2.3", units.get(0).getEnvironments().get(0).getCurrentAction().getBuildNumber());

        try (JenkinsRule.WebClient wc = j.createWebClient()) {
            HtmlPage page = wc.goTo("view/deployments/");
            String html = page.getWebResponse().getContentAsString();
            assertTrue(html.contains("1.2.3"), "release version must be shown on the dashboard");
            assertTrue(html.contains("edb-popup-toggle"), "environment link must use the CSP-safe toggle");
            assertFalse(html.contains("javascript:toggle"),
                    "inline javascript: URLs must be gone (JENKINS-74429)");
        }
    }
}
