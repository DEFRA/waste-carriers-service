package uk.gov.ea.wastecarrier.services;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import uk.gov.ea.wastecarrier.services.backgroundJobs.ExportJobConfiguration;
import uk.gov.ea.wastecarrier.services.backgroundJobs.RegistrationStatusJobConfiguration;

public class WasteCarrierConfiguration extends Configuration {
    @Valid
    @NotNull
    @JsonProperty
    private DatabaseConfiguration database = new DatabaseConfiguration();

    @Valid
    @NotNull
    @JsonProperty
    private DatabaseConfiguration userDatabase = new DatabaseConfiguration();

    @Valid
    @NotNull
    @JsonProperty
    private SettingsConfiguration settings = new SettingsConfiguration();

    @Valid
    @NotNull
    @JsonProperty
    private IRConfiguration irRenewals = new IRConfiguration();

    @Valid
    @NotNull
    @JsonProperty
    private EntityMatchingConfiguration entityMatching = new EntityMatchingConfiguration();
    
    @Valid
    @NotNull
    @JsonProperty
    private ExportJobConfiguration exportJob = new ExportJobConfiguration();
    
    @Valid
    @NotNull
    @JsonProperty
    private RegistrationStatusJobConfiguration registrationStatusJob = new RegistrationStatusJobConfiguration();
    
    @Valid
    @NotNull
    @JsonProperty
    private AirbrakeLogbackConfiguration airbrake = new AirbrakeLogbackConfiguration();
    
    public DatabaseConfiguration getDatabase() {
        return database;
    }

    public DatabaseConfiguration getUserDatabase() {
        return userDatabase;
    }

    public SettingsConfiguration getSettings() {
        return settings;
    }

    public IRConfiguration getIrRenewals() {
        return irRenewals;
    }

    public EntityMatchingConfiguration getEntityMatching() {
        return entityMatching;
    }
    
    public ExportJobConfiguration getExportJobConfiguration() {
        return exportJob;
    }
    
    public RegistrationStatusJobConfiguration getRegistrationStatusJobConfiguration() {
        return registrationStatusJob;
    }
    
    public AirbrakeLogbackConfiguration getAirbrakeLogbackConfiguration() {
        return airbrake;
    }
}
