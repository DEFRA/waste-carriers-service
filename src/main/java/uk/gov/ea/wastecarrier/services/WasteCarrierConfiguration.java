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
}
