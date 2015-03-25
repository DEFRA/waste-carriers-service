package uk.gov.ea.wastecarrier.services.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

import uk.gov.ea.wastecarrier.services.SettingsConfiguration;

public class Settings {
	
	public final static String COLLECTION_SINGULAR_NAME = "setting";
	public final static String COLLECTION_NAME = COLLECTION_SINGULAR_NAME +"s";
	
    @JsonProperty
    private String registrationPeriod;
    
    @JsonProperty
    private String registrationRenewPeriod;
    
    @JsonProperty
    private int registrationCost;
    
    @JsonProperty
    private int copyCardCost;
    
    @JsonIgnore
    private SettingsConfiguration settingsConfig;
    
    public Settings()
    {
    	super();
    }
    
    public Settings(SettingsConfiguration settings)
    {
    	super();
    	this.settingsConfig = settings;
    }

    /**
	 * @return the registrationPeriod
	 */
	public String getRegistrationPeriod()
	{
		return settingsConfig.getRegistrationPeriod();
	}

	/**
	 * @return the registrationRenewPeriod
	 */
	public String getRegistrationRenewPeriod()
	{
		return settingsConfig.getRegistrationRenewPeriod();
	}

	/**
	 * @return the registrationCost
	 */
	public int getRegistrationCost()
	{
		return settingsConfig.getRegistrationCost();
	}

	/**
	 * @return the copyCardCost
	 */
	public int getCopyCardCost()
	{
		return settingsConfig.getCopyCardCost();
	}
}
