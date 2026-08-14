package org.jenkinsci.plugins.environmentdashboard;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Run;
import java.util.List;
import java.util.stream.Collectors;
import jenkins.model.details.Detail;
import jenkins.model.details.DetailFactory;
import org.jenkinsci.plugins.environmentdashboard.BuildAddUrl.BuildUrlAction;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest2;

/**
 * Surfaces every {@link BuildUrlAction} in the run details bar, so the links
 * added by {@code buildAddUrl} stay reachable on the redesigned build pages
 * (core's new build page and the Pipeline Graph View pages), which no longer
 * render the classic sidebar tasks.
 */
@Extension
public class BuildUrlDetailFactory extends DetailFactory<Run> {

    @Override
    public Class<Run> type() {
        return Run.class;
    }

    @Override
    @NonNull
    public List<? extends Detail> createFor(@NonNull Run target) {
        return target.getActions(BuildUrlAction.class).stream()
                .map(action -> new BuildUrlDetail(target, action))
                .collect(Collectors.toList());
    }

    public static class BuildUrlDetail extends Detail {
        private final BuildUrlAction action;

        BuildUrlDetail(Run<?, ?> run, BuildUrlAction action) {
            super(run);
            this.action = action;
        }

        @Override
        public String getIconClassName() {
            return action.getIconClassName();
        }

        @Override
        public String getDisplayName() {
            return action.getDisplayName();
        }

        @Override
        public String getLink() {
            Run<?, ?> run = (Run<?, ?>) getObject();
            StaplerRequest2 req = Stapler.getCurrentRequest2();
            String contextPath = req != null ? req.getContextPath() : "";
            return contextPath + "/" + run.getUrl() + action.getUrlName();
        }
    }
}
