package uk.gov.ea.wastecarrier.services.backgroundJobs;

import com.yammer.dropwizard.config.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stores configuration for the Registration Status job, which updates the
 * status of registrations.  At present, the only change is to mark a
 * Registration as "expired" at the appropriate time.
 */
public class RegistrationStatusJobConfiguration extends Configuration
{
    @JsonProperty
    private String cronExpression;
    
    /**
     * Gets a cron expression defining the schedule to execute the Registration
     * Status job at.  A null or empty definition indicates "no schedule".
     * @return String containing the 6- or 7-field cron expression for this job.
     */
    public String getCronExpression()
    {
        return cronExpression;
    }
}
