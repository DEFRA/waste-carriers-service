package uk.gov.ea.wastecarrier.services;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.yammer.dropwizard.config.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class WasteCarrierConfiguration extends Configuration {
	
	/**
	 * This is an example parameter, which is not used, but has been left as an example
	 * for when genuine parameters are required
	 */
    @NotEmpty
    @JsonProperty
    private String template;

    @NotEmpty
    @JsonProperty
    private String defaultName = "Stranger";
    
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
    private IRConfiguration irrenewals = new IRConfiguration();

    public String getTemplate() {
        return template;
    }

    public String getDefaultName() {
        return defaultName;
    }
    
    public MessageQueueConfiguration getMessageQueueConfiguration() {
        return messageQueue;
    }
    
    public DatabaseConfiguration getDatabase() {
        return database;
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

	public IRConfiguration getIrenewals()
	{
		return irrenewals;
	}
}