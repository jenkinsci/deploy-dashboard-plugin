package org.jenkinsci.plugins.environmentdashboard;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import jenkins.tasks.SimpleBuildStep;
import org.jenkins.ui.icon.IconSpec;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;

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
            @NonNull Run<?, ?> run,
            @NonNull FilePath workspace,
            @NonNull EnvVars env,
            @NonNull Launcher launcher,
            @NonNull TaskListener listener
    ) throws InterruptedException, IOException {
        run.addAction(new BuildUrlAction(title, url));
    }

    @Extension
    @Symbol("buildAddUrl")
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {
        @Override
        @NonNull
        public String getDisplayName() {
            return "Build Add Url";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> t) {
            return true;
        }
    }

    /**
     * A sidebar/app-bar entry that navigates to the configured URL.
     *
     * The target URL (which typically carries a query string, e.g. a
     * {@code parambuild} link with prefilled parameters) is deliberately NOT
     * exposed as the action's {@code urlName}: query strings in action URLs
     * are not preserved reliably by every Jenkins UI that renders actions.
     * Instead the action is bound under a stable path segment and answers it
     * with an HTTP redirect to the real target, so the query string always
     * reaches the browser intact.
     */
    public static class BuildUrlAction implements Action, IconSpec {
        private final String title;
        private final String url;

        BuildUrlAction(String title, String url) {
            this.title = title;
            this.url = url;
        }

        @Override
        public String getIconFileName() {
            // Hardcoded artifact id: getClass().getPackage().getImplementationTitle()
            // is unreliable under modern plugin classloaders (may return null).
            return "/plugin/deploy-dashboard/deploy.png";
        }

        @Override
        public String getIconClassName() {
            return "symbol-rocket-outline plugin-ionicons-api";
        }

        @Override
        public String getDisplayName() {
            return title;
        }

        @Override
        public String getUrlName() {
            return "deploy-link-" + Util.getDigestOf(title + "|" + url).substring(0, 12);
        }

        public String getUrl() {
            return url;
        }

        /**
         * Only root-relative paths and http(s) URLs may be redirected to;
         * anything else (javascript:, data:, protocol-relative) is refused.
         */
        boolean isSafeUrl() {
            String target = Util.fixEmptyAndTrim(url);
            if (target == null) {
                return false;
            }
            if (target.startsWith("/")) {
                return !target.startsWith("//");
            }
            try {
                String scheme = new URI(target).getScheme();
                if (scheme == null) {
                    return false;
                }
                scheme = scheme.toLowerCase(Locale.ROOT);
                return scheme.equals("http") || scheme.equals("https");
            } catch (URISyntaxException e) {
                return false;
            }
        }

        public void doIndex(StaplerRequest2 req, StaplerResponse2 rsp) throws IOException {
            if (!isSafeUrl()) {
                rsp.sendError(StaplerResponse2.SC_NOT_FOUND);
                return;
            }
            rsp.sendRedirect2(url);
        }
    }
}
