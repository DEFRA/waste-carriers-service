package uk.gov.ea.wastecarrier.services.backgroundJobs;

import com.yammer.dropwizard.config.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Stores configuration for the Export Job, which exports data for use in an
 * electronic Public Register (ePR) and for Reporting.
 * The job also updates the status of registrations which have expired since the
 * last run of this job.
 */
public class ExportJobConfiguration extends Configuration {
    @NotEmpty
    @JsonProperty
    private String eprExportFile;

    @NotEmpty
    @JsonProperty
    private String eprExportDateFormat = "yyyy-MM-dd";

    @NotEmpty
    @JsonProperty
    private String reportingExportPath;

    @NotEmpty
    @JsonProperty
    private String reportingExportDateFormat = "yyyy-MM-dd";

    @JsonProperty
    private String cronExpression;
    
    /**
     * Gets the file to write ePR export data to.
     * @return The eprExportFile.
     */
    public String getEprExportFile()
    {
        return eprExportFile;
    }

    /**
     * Gets the date format to use in the ePR export.
     * @return The eprExportDateFormat.
     */
    public String getEprExportDateFormat()
    {
        return eprExportDateFormat;
    }

    /**
     * Gets the path (directory) to write Reporting Snapshot data to.
     * @return The reportingExportPath.
     */
    public String getReportingExportPath()
    {
        return reportingExportPath;
    }

    /**
     * Gets the date format to use in the Reporting Snapshot.
     * @return The reportingExportDateFormat.
     */
    public String getReportingExportDateFormat()
    {
        return reportingExportDateFormat;
    }
    
    /**
     * Gets a cron expression defining the schedule to execute the Export job
     * at.  A null or empty definition indicates "no schedule".
     * @return String containing the 6- or 7-field cron expression for this job.
     */
    public String getCronExpression()
    {
        return cronExpression;
    }
}
