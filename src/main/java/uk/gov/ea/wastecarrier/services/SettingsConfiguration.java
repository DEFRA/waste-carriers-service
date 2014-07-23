package uk.gov.ea.wastecarrier.services;

import com.yammer.dropwizard.config.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class SettingsConfiguration extends Configuration {
	
    @NotEmpty
    @JsonProperty
    private String registrationPeriod = "3 YEARS";
    
    @NotEmpty
    @JsonProperty
    private String registrationRenewPeriod = "6 MONTHS";
    
    @NotEmpty
    @JsonProperty
    private int registrationCost = 15400;
    
    @NotEmpty
    @JsonProperty
    private int copyCardCost = 1000;

    /**
	 * @return the registrationPeriod
	 */
	public String getRegistrationPeriod()
	{
		return registrationPeriod;
	}

	/**
	 * @return the registrationRenewPeriod
	 */
	public String getRegistrationRenewPeriod()
	{
		return registrationRenewPeriod;
	}

	/**
	 * @return the registrationCost
	 */
	public int getRegistrationCost()
	{
		return registrationCost;
	}

	/**
	 * @return the copyCardCost
	 */
	public int getCopyCardCost()
	{
		return copyCardCost;
	}
}
