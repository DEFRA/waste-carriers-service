package uk.gov.ea.wastecarrier.services;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.yammer.dropwizard.config.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class WasteCarrierConfiguration extends Configuration {
	
	/**
	 * @deprecated - obsolete parameter, not used anymore
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
}
