package uk.gov.ea.wastecarrier.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;

public class MockConfiguration extends Configuration {

    @Valid
    @JsonProperty
    public Integer delay;
}
