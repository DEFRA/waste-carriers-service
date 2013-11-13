package uk.gov.ea.wastecarrier.services.core;

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
public class MetaData {
	
    private String dateRegistered;
	
    private String anotherString;

	private String lastModified;
	
	private RegistrationStatus status;
	
	private String revokedReason;

	public enum RegistrationStatus {
	    ACTIVE, REVOKED, DELETED
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

    public MetaData(String dateRegistered, String anotherString) 
    {
        this.dateRegistered = dateRegistered;
        this.anotherString = anotherString;
        this.lastModified = dateRegistered;
        this.status = RegistrationStatus.ACTIVE;
    }

    public String getDateRegistered() {
        return dateRegistered;
    }
    
    public String getAnotherString() {
        return anotherString;
    }

	/**
	 * @return the lastModified
	 */
	public String getLastModified()
	{
		return lastModified;
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
	
	public static String getCurrentDateTime()
    {
    	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
    }
	
	/**
	 * @param dateRegistered the dateRegistered to set
	 */
	public void setDateRegistered(String dateRegistered)
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
	public void setLastModified(String lastModified)
	{
		this.lastModified = lastModified;
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

}
