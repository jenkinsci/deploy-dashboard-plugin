package org.jenkinsci.plugins.environmentdashboard;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import hudson.Extension;
import hudson.Util;
import hudson.model.ListView;
import hudson.model.TopLevelItem;
import hudson.model.ViewDescriptor;
import hudson.util.FormValidation;
import io.jenkins.plugins.datatables.AsyncTableContentProvider;
import io.jenkins.plugins.datatables.TableModel;
import net.sf.json.JSONObject;

public class DeploymentView extends ListView implements AsyncTableContentProvider{
    private DeploymentTableModel model;

    @DataBoundConstructor
    public DeploymentView(final String name) {
        super(name);
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

        // Copy-n-paste from ListView$Descriptor as sadly we cannot inherit from that
        // class
        public FormValidation doCheckIncludeRegex(@QueryParameter final String value) {
            final String v = Util.fixEmpty(value);
            if (v != null) {
                try {
                    Pattern.compile(v);
                } catch (final PatternSyntaxException pse) {
                    return FormValidation.error(pse.getMessage());
                }
            }
            return FormValidation.ok();
        }

        @Override
        public boolean configure(final StaplerRequest req, final JSONObject json) throws FormException {
            save();

            return true;
        }
    }

    
    @Override
    public TableModel getTableModel(String id) {
        if (model == null) {
            model = new DeploymentTableModel(id);
        }
        return model;
    }
     
    public TableModel getTableModel(String id, List<? extends TopLevelItem> items) {
        if (model == null) {
            model = new DeploymentTableModel(id);
        }
        return model.populate(items);
    }

    @Override
    @JavaScriptMethod
    public String getTableRows(final String id) {
        return toJsonArray(getTableModel(id).getRows());
    }

    private String toJsonArray(final List<Object> rows) {
        try {
            return new ObjectMapper().writeValueAsString(rows);
        }
        catch (JsonProcessingException exception) {
            throw new IllegalArgumentException(
                    String.format("Can't convert table rows '%s' to JSON object", rows), exception);
        }
    }
}
