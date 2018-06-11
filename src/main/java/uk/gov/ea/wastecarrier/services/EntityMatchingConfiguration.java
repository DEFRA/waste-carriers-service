package uk.gov.ea.wastecarrier.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class EntityMatchingConfiguration extends Configuration {

    @Valid
    @NotEmpty
    @JsonProperty
    public String entitiesFilePath;
}
