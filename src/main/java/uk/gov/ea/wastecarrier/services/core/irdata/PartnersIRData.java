package uk.gov.ea.wastecarrier.services.core.irdata;

import java.util.Date;

public class PartnersIRData extends IRData
{
	/**
	 * PARTNERS
	 * CB_REFERENCE_NUMBER,EXPIRY_DATE,REGISTRATION_TYPE,APPLICANT_TYPE,PARTY_SUBTYPE,PARTNERSHIP_NAME,TRADING_NAME,DOB
	 */
	
	public PartnersIRData()
	{
		//
	}
	
	public PartnersIRData(String[] rawData)
	{
		// set full raw row data as a field
		setRawData(rawData);
		
        setReferenceNumber(rawData[0]);
		
        Date d = convertToDate(rawData[1]);
		setExpiryDate(d);
		
		setRegistrationType(rawData[2]);
		
		setApplicantType(rawData[3]);
		
		setPartySubType(rawData[4]);
		
		setPartnershipName(rawData[5]);
		
		setTradingName(rawData[6]);
		
		Date dob = convertToDate(rawData[7]);
		setDateOfBirth(dob);
	}
	
	@Override
	public IRType getIrType()
	{
		return IRType.PARTNER;
		
	}
	
	private String partySubType;
	
	private String partnershipName;
	
    private String tradingName;
	
	private Date dateOfBirth;
	
	/**
	 * @return the partySubType
	 */
	public String getPartySubType()
	{
		return partySubType;
	}

	/**
	 * @param partySubType the partySubType to set
	 */
	public void setPartySubType(String partySubType)
	{
		this.partySubType = partySubType;
	}

	/**
	 * @return the partnershipName
	 */
	public String getPartnershipName()
	{
		return partnershipName;
	}

	/**
	 * @param partnershipName the partnershipName to set
	 */
	public void setPartnershipName(String partnershipName)
	{
		this.partnershipName = partnershipName;
	}

	/**
	 * @return the tradingName
	 */
	public String getTradingName()
	{
		return tradingName;
	}

	/**
	 * @param tradingName the tradingName to set
	 */
	public void setTradingName(String tradingName)
	{
		this.tradingName = tradingName;
	}

	/**
	 * @return the dateOfBirth
	 */
	public Date getDateOfBirth()
	{
		return dateOfBirth;
	}

	/**
	 * @param dateOfBirth the dateOfBirth to set
	 */
	public void setDateOfBirth(Date dateOfBirth)
	{
		this.dateOfBirth = dateOfBirth;
	}
	
}
