package org.jenkinsci.plugins.environmentdashboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jenkins.plugins.datatables.TableConfiguration;

public class DeploymentTableConfiguration extends TableConfiguration{

    private final Map<String, Object> configuration = new HashMap<>();

    DeploymentTableConfiguration() {
        super();
        
        this.configuration.put("rowsGroup", new int[]{0});
        this.configuration.put("pagingType", "full_numbers");
        this.configuration.put("stateSave", true);
    }

    @Override
    public String getConfiguration() {
        try {
            return new ObjectMapper().writeValueAsString(configuration);
        }
        catch (JsonProcessingException exception) {
            throw new IllegalArgumentException(
                    String.format("Can't convert table configuration '%s' to JSON object", configuration), exception);
        }
    }
}
