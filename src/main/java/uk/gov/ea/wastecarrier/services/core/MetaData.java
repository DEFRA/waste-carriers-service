package uk.gov.ea.wastecarrier.services.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The MetaData class represents the various operational information 
 * related to a registration but is not information that a user would 
 * provide on screen. This information is intended to be used for 
 * Audit trail and monitoring purposes
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetaData {

    private Date dateRegistered;

    private String anotherString;

    private Date lastModified;

    private Date dateActivated;

    private RegistrationStatus status;

    private String revokedReason;

    private RouteType route;

    private String distance = "n/a";

    /**
     * Statuses and their meaning:
     *
     * PENDING: Initial State
     * ACTIVATE:
     * ACTIVE: Registration has been Verified and should appear on the register
     * REVOKED: Registration has been removed from the register by the EA
     * INACTIVE: Registration has been deleted from the register by the registrant
     *
     */
        
        // TODO in next CI: Remove the "ACTIVATE" item; it is unused.
    public enum RegistrationStatus {
        PENDING, ACTIVATE, ACTIVE, REVOKED, EXPIRED, INACTIVE, REFUSED
    }

    public enum RouteType {
        DIGITAL, ASSISTED_DIGITAL
    }
    
    /**
     * Other possible meta data values include the following:
     * 
     * long versionNumber   -- version number to represent the version of the registration model used
     * 
     * list<Registration> archiveList -- historical list of all version changes; 
     * 									 when a change is made the full details are stored here prior to change
     * 
     * 
     * String modifiedBy	-- user whom modified registration last, e.g. initiator, staff. 
     *  					By default its initiator unless login mechanic provided
     * 
     */

    /**
     * This empty default constructor is needed for JSON to be happy. The alternative is add
     * "@JsonProperty("id")" in-front of the "long id" definition in the fully qualified constructor
     */
    public MetaData()
    {
    }

    public MetaData(Date dateRegistered, String anotherString, RouteType route)
    {
        this.dateRegistered = dateRegistered;
        this.anotherString = anotherString;
        this.lastModified = dateRegistered;
        this.dateActivated = null;
        //The initial status is PENDING for most new registrations.
        this.status = RegistrationStatus.PENDING;
        this.route = route;
    }

    public Date getDateRegistered() {
        return dateRegistered;
    }
    
    public String getAnotherString() {
        return anotherString;
    }

    /**
     * @return the lastModified
     */
    public Date getLastModified()
    {
        return lastModified;
    }

    /**
     * @return the dateActivated
     */
    public Date getDateActivated()
    {
        return dateActivated;
    }

    /**
     * @return the status
     */
    public RegistrationStatus getStatus()
    {
        return status;
    }

    /**
     * @return the revokedReason
     */
    public String getRevokedReason()
    {
        return revokedReason;
    }

    /**
     * @return the route
     */
    public RouteType getRoute()
    {
        return route;
    }

    /**
     * @return the distance
     */
    public String getDistance()
    {
        return distance;
    }

    public static Date getCurrentDateTime()
    {
        //@SamGriffiths [20150127]- changed the formats of all Dates to Date rather than String
        Date date = new Date();
        return date;
    }

    /**
     * @param dateRegistered the dateRegistered to set
     */
    public void setDateRegistered(Date dateRegistered)
    {
        this.dateRegistered = dateRegistered;
    }

    /**
     * @param anotherString the anotherString to set
     */
    public void setAnotherString(String anotherString)
    {
        this.anotherString = anotherString;
    }

    /**
     * @param lastModified the lastModified to set
     */
    public void setLastModified(Date lastModified)
    {
        this.lastModified = lastModified;
    }

    /**
     * @param dateActivated the dateActivated to set
     */
    public void setDateActivated(Date dateActivated)
    {
        this.dateActivated = dateActivated;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(RegistrationStatus status)
    {
        this.status = status;
    }

    /**
     * @param revokedReason the revokedReason to set
     */
    public void setRevokedReason(String revokedReason)
    {
        this.revokedReason = revokedReason;
    }

    /**
     * @param route the route to set
     */
    public void setRoute(RouteType route)
    {
        this.route = route;
    }

    /**
     * @param distance the distance to set
     */
    public void setDistance(String distance)
    {
        this.distance = distance;
    }

}
