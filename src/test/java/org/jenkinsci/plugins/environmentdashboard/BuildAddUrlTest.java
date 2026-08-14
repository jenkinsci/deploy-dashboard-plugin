package org.jenkinsci.plugins.environmentdashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.util.List;
import jenkins.model.details.Detail;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class BuildAddUrlTest {

    private static final String TARGET =
            "/job/app-deploy/parambuild/?imageRegistry=reg.example.com/app&version=1.2.3&revision=abc123&build_url=https://ci.example.com/job/app/1/";

    private static WorkflowRun runWithLink(JenkinsRule j, String url) throws Exception {
        WorkflowJob job = j.createProject(WorkflowJob.class, "app-" + Math.abs(url.hashCode()));
        job.setDefinition(new CpsFlowDefinition(
                "node { buildAddUrl(title: 'Deploy to DEV', url: '" + url + "') }", true));
        return j.buildAndAssertSuccess(job);
    }

    @Test
    void actionRedirectsToTargetWithQueryStringIntact(JenkinsRule j) throws Exception {
        WorkflowRun run = runWithLink(j, TARGET);

        BuildAddUrl.BuildUrlAction action = run.getAction(BuildAddUrl.BuildUrlAction.class);
        assertNotNull(action, "buildAddUrl should attach a BuildUrlAction to the run");
        assertEquals("Deploy to DEV", action.getDisplayName());
        assertEquals(TARGET, action.getUrl());

        // The routed path must be a clean token: query strings in action URLs
        // get mangled or dropped by some UIs, which is the bug this guards against.
        assertFalse(action.getUrlName().contains("?"), "urlName must not carry a query string");
        assertFalse(action.getUrlName().contains("&"), "urlName must not carry a query string");

        try (JenkinsRule.WebClient wc = j.createWebClient()) {
            wc.getOptions().setRedirectEnabled(false);
            wc.getOptions().setThrowExceptionOnFailingStatusCode(false);
            URL url = new URL(j.getURL(), run.getUrl() + action.getUrlName() + "/");
            WebResponse response = wc.loadWebResponse(new WebRequest(url));
            assertEquals(302, response.getStatusCode(), "the action must redirect to the configured URL");
            String location = response.getResponseHeaderValue("Location");
            assertNotNull(location);
            assertTrue(location.endsWith(TARGET),
                    "redirect must preserve the full query string, got: " + location);
        }
    }

    @Test
    void unsafeUrlIsNotRedirected(JenkinsRule j) throws Exception {
        WorkflowRun run = runWithLink(j, "javascript:alert(1)");

        BuildAddUrl.BuildUrlAction action = run.getAction(BuildAddUrl.BuildUrlAction.class);
        assertNotNull(action);
        assertFalse(action.isSafeUrl());

        try (JenkinsRule.WebClient wc = j.createWebClient()) {
            wc.getOptions().setRedirectEnabled(false);
            wc.getOptions().setThrowExceptionOnFailingStatusCode(false);
            URL url = new URL(j.getURL(), run.getUrl() + action.getUrlName() + "/");
            WebResponse response = wc.loadWebResponse(new WebRequest(url));
            assertEquals(404, response.getStatusCode(), "unsafe URL schemes must not redirect");
        }
    }

    @Test
    void protocolRelativeUrlIsNotRedirected(JenkinsRule j) throws Exception {
        WorkflowRun run = runWithLink(j, "//evil.example.com/phish");

        BuildAddUrl.BuildUrlAction action = run.getAction(BuildAddUrl.BuildUrlAction.class);
        assertNotNull(action);
        assertFalse(action.isSafeUrl());
    }

    @Test
    void detailFactoryExposesLinkOnDetailsBar(JenkinsRule j) throws Exception {
        WorkflowRun run = runWithLink(j, TARGET);
        BuildAddUrl.BuildUrlAction action = run.getAction(BuildAddUrl.BuildUrlAction.class);

        List<? extends Detail> details = new BuildUrlDetailFactory().createFor(run);
        assertEquals(1, details.size(), "each buildAddUrl call should surface one detail");
        Detail detail = details.get(0);
        assertEquals("Deploy to DEV", detail.getDisplayName());
        assertNotNull(detail.getIconClassName(), "details without an icon are not rendered by the details bar");
        assertTrue(detail.getLink().endsWith("/" + run.getUrl() + action.getUrlName()),
                "detail must link to the redirecting action, got: " + detail.getLink());
    }
}
