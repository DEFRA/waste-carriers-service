package uk.gov.ea.wastecarrier.services;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class SettingsConfiguration extends Configuration {

    @NotEmpty
    @JsonProperty
    private String registrationPeriod = "3";
    
    @NotEmpty
    @JsonProperty
    private String registrationRenewPeriod = "6";
    
    @Min(1)
    @Max(100000000)
    @JsonProperty
    private int registrationCost = 15400;
    
    @Min(1)
    @Max(100000000)
    @JsonProperty
    private int copyCardCost = 1000;

    /**
     * @return the registrationPeriod
     */
    public String getRegistrationPeriod()
    {
        return registrationPeriod + " YEARS";
    }

    /**
     * @return the registrationRenewPeriod
     */
    public String getRegistrationRenewPeriod()
    {
        return registrationRenewPeriod + " MONTHS";
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
