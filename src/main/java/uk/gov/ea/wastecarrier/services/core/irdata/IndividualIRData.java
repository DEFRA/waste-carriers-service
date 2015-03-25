package uk.gov.ea.wastecarrier.services.core.irdata;

import java.util.Date;


public class IndividualIRData extends IRData
{
	/**
	 * INDIVIDUAL DATA
	 * CB_REFERENCE_NUMBER,EXPIRY_DATE,REGISTRATION_TYPE,APPLICANT_TYPE,PERMIT_HOLDER_NAME,TRADING_NAME,DOB
	 */
	
	public IndividualIRData()
	{
		//
	}
	
	public IndividualIRData(String[] rawData)
	{
		// set full raw row data as a field
		setRawData(rawData);
		
        setReferenceNumber(rawData[0]);
		
        Date d = convertToDate(rawData[1], getIrType());
		setExpiryDate(d);
		
		setRegistrationType(rawData[2]);
		
		setApplicantType(rawData[3]);
		
		setPermitHolderName(rawData[4]);
		
		setTradingName(rawData[5]);
		
		Date dob = convertToDate(rawData[6], getIrType());
		setDateOfBirth(dob);
	}
	
	@Override
	public IRType getIrType()
	{
		return IRType.INDIVIDUAL;
		
	}
	
	private String permitHolderName;
	
	private String tradingName;
	
	private Date dateOfBirth;

	/**
	 * @return the permitHolderName
	 */
	public String getPermitHolderName()
	{
		return permitHolderName;
	}

	/**
	 * @param permitHolderName the permitHolderName to set
	 */
	public void setPermitHolderName(String permitHolderName)
	{
		this.permitHolderName = permitHolderName;
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
