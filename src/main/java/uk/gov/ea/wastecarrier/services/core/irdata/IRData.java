package uk.gov.ea.wastecarrier.services.core.irdata;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.mongojack.Id;
import org.mongojack.ObjectId;

import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IRData
{

	/**
	 * 0 CB_REFERENCE_NUMBER, ==> originalReferenceNumber
	 * FORMAT:: CB/[A-Z]{2}[0-9]{4}[A-Z]{2}/[ARV]{1}[0-9]{3}
	 * E.g. CB/AM3280HA/A001
	 *
	 */
	
	public final static String COLLECTION_SINGULAR_NAME = "irrenewal";
	
	public final static String COLLECTION_NAME = COLLECTION_SINGULAR_NAME +"s";
	
	private String applicantType;    /*Item 3*/
	
	private Date expiryDate;         /*Item 1*/
	
	private String referenceNumber;  /*Item 0*/
	
	private String registrationType; /*Item 2*/
	
	/**
	 * This is the Key for the IR details
	 */
	@JsonProperty
	@Id
	@ObjectId
    private String id;
	
	public enum IRType {
	    COMPANY,
	    INDIVIDUAL,
	    PARTNER,
	    PUBLIC_BODY
	}
	
	public enum RegistrationType {
	    CARRIER,
	    BROKER,
	    CARRIER_AND_BROKER
	}
	
	private IRType irType;
	
	private String[] rawData;		 /*Kept as original line*/
	
	protected Logger log = Logger.getLogger(IRData.class.getName());

	/**
	 * @return the applicantType
	 */
	public String getApplicantType()
	{
		return applicantType;
	}
	
	/**
	 * @return the expiryDate
	 */
	public Date getExpiryDate()
	{
		return expiryDate;
	}

	/**
	 * @return the id
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * @return the irType
	 */
	public IRType getIrType()
	{
		return irType;
	}

	/**
	 * @return the rawData
	 */
	public String[] getRawData()
	{
		return rawData;
	}

	/**
	 * @return the referenceNumber
	 */
	public String getReferenceNumber()
	{
		return referenceNumber;
	}

	/**
	 * @return the registrationType
	 */
	public String getRegistrationType()
	{
		return registrationType;
	}

	/**
	 * @param applicantType the applicantType to set
	 */
	public void setApplicantType(String applicantType)
	{
		this.applicantType = applicantType;
	}

	/**
	 * @param expiryDate the expiryDate to set
	 */
	public void setExpiryDate(Date expiryDate)
	{
		this.expiryDate = expiryDate;
	}

	/**
	 * @param irType the irType to set
	 */
	public void setIrType(IRType irType)
	{
		this.irType = irType;
	}

	/**
	 * @param rawData the rawData to set
	 */
	public void setRawData(String rawData[])
	{
		this.rawData = rawData;
	}

	/**
	 * @param referenceNumber the referenceNumber to set
	 */
	public void setReferenceNumber(String referenceNumber)
	{
		this.referenceNumber = referenceNumber;
	}

	/**
	 * @param registrationType the registrationType to set
	 */
	public void setRegistrationType(String registrationType)
	{
		this.registrationType = registrationType;
	}
	
	/**
	 * Gets the registration type from the IR Data, and returns it as a
	 * RegistrationType object
	 *
	 * @return RegistrationType object for this IRData
	 */
	public RegistrationType getTrueRegistrationType()
	{
		if (this.getRegistrationType().equalsIgnoreCase("carrier"))
		{
			return RegistrationType.CARRIER;
		}
		else if (this.getRegistrationType().equalsIgnoreCase("broker"))
		{
			return RegistrationType.BROKER;
		}
		else if (this.getRegistrationType().equalsIgnoreCase("carrier and broker"))
		{
			return RegistrationType.CARRIER_AND_BROKER;
		}
		return null;
	}
	
	/**
	 * Helper function to create Java dates, from the date format provided in the IR data files
	 * @param dateAsString date string to convert to java.util.Date
	 * @return Date if valid date, or null if unable to convert date
	 */
	protected Date convertToDate(String dateAsString, IRType type)
	{
		Date d = null;
		try
		{
			// Date is currently in format 7/19/2014 in the data files
			// Appending a fixed time of 12:00 ensures that the date created is always within the middle of the day
			// and will therefore not be affected by daylight savings
			d = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.ENGLISH).parse(dateAsString + " 12:00");
		}
		catch (ParseException e)
		{
			
			String message = "[";
			for (String s : getRawData())
			{
				message += s + ",";
			}
			message += "]";
			log.warning("Could not parse date '" + dateAsString + "' for row: " + message + ", type : " + type);
			log.warning("It is likely that a preceding column includes an unquoted comma; please fix the CSV file.");
		}
		return d;
	}
}
