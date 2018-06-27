package uk.gov.ea.wastecarrier.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;

public class MockConfiguration extends Configuration {

    @Valid
    @NotEmpty
    @JsonProperty
    public String worldPayAdminCode;

    @Valid
    @NotEmpty
    @JsonProperty
    public String servicesDomain;

    @Valid
    @NotEmpty
    @JsonProperty
    public String macSecret;

    @Valid
    @JsonProperty
    public Integer delay;
}
