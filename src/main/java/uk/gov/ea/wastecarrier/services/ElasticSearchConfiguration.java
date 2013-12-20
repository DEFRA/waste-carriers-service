package uk.gov.ea.wastecarrier.services;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class ElasticSearchConfiguration {
    @NotEmpty
    @JsonProperty
    private String host;

    @Min(1)
    @Max(65535)
    @JsonProperty
    private int port = 5672;
    
    @Min(1)
    @Max(250)
    @JsonProperty
    private int size = 100;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
    
    public int getSize() {
        return size;
    }
}