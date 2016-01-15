package uk.gov.ea.wastecarrier.services;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.yammer.dropwizard.config.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import uk.gov.ea.wastecarrier.services.backgroundJobs.ExportJobConfiguration;

public class WasteCarrierConfiguration extends Configuration {
    @Valid
    @NotNull
    @JsonProperty
    private MessageQueueConfiguration messageQueue = new MessageQueueConfiguration();

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
    private ElasticSearchConfiguration elasticSearch = new ElasticSearchConfiguration();

    @Valid
    @NotEmpty
    @JsonProperty
    private String postcodeFilePath = "/postcodes.csv";

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
    private IRConfiguration irMigration = new IRConfiguration();

    @Valid
    @NotNull
    @JsonProperty
    private ExportJobConfiguration exportJob = new ExportJobConfiguration();
    
    public MessageQueueConfiguration getMessageQueueConfiguration() {
        return messageQueue;
    }

    public DatabaseConfiguration getDatabase() {
        return database;
    }

    public DatabaseConfiguration getUserDatabase() {
        return userDatabase;
    }

    public ElasticSearchConfiguration getElasticSearch() {
        return elasticSearch;
    }

    public String getPostcodeFilePath() {
        return postcodeFilePath;
    }

    public SettingsConfiguration getSettings() {
        return settings;
    }

    public IRConfiguration getIrRenewals()
    {
        return irRenewals;
    }
    
    public IRConfiguration getIrMigration()
    {
        return irMigration;
    }

    public ExportJobConfiguration getExportJobConfiguration() {
        return exportJob;
    }
}
