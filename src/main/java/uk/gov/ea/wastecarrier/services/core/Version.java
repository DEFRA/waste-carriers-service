package uk.gov.ea.wastecarrier.services.core;

import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents the entire model of the initial Waste Carrier Registration details
 * Ideally the models details could be split into subclasses for each distinct type of details (page)
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Version
{
    /*
     * This is the version string for this service
     */
    @JsonProperty
    @NotEmpty
    private String versionDetails;

    @JsonProperty
    @NotEmpty
    private String lastBuilt;

    // Standard logging declaration
    private Logger log = Logger.getLogger(Version.class.getName());

    public final static String VERSION_SINGULAR_NAME = "version";

    /**
     * Default constructor is required for non complete objects, and as alternative to
     * defining specific JSON names per property
     */
    public Version ()
    {
        log.fine("Initialize empty Version object");
        this.setVersionDetails("DEFAULT");
        this.setLastBuilt("DEFAULT-2013-12-04");
    }

    /**
     * @return the versionDetails
     */
    public String getVersionDetails()
    {
        return versionDetails;
    }

    /**
     * @return the lastBuilt
     */
    public String getLastBuilt()
    {
        return lastBuilt;
    }

    /**
     * @param versionDetails the versionDetails to set
     */
    public void setVersionDetails(String versionDetails)
    {
        this.versionDetails = versionDetails;
    }

    /**
     * @param lastBuilt the lastBuilt to set
     */
    public void setLastBuilt(String lastBuilt)
    {
        this.lastBuilt = lastBuilt;
    }

}
