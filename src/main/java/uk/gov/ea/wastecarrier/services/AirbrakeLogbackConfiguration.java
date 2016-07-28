package uk.gov.ea.wastecarrier.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class AirbrakeLogbackConfiguration {
    @JsonProperty
    @NotEmpty
    private String url;

    @JsonProperty
    @NotEmpty
    private String apiKey;
    
    @JsonProperty
    @NotEmpty
    private String environmentName;
    
    @JsonProperty
    private Boolean enabled = true;
    
    @JsonProperty
    private Boolean exceptionsOnly = false;
    
    @JsonProperty
    String threshold = "ERROR";

    public String getUrl() {
        return url;
    }

    public String getApiKey() {
        return apiKey;
    }
    
    public String getEnvironmentName() {
        return environmentName;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public Boolean getExceptionsOnly() {
        return exceptionsOnly;
    }
    
    public String getThreshold() {
        return ch.qos.logback.classic.Level.toLevel(threshold, ch.qos.logback.classic.Level.ERROR).toString();
    }
}
