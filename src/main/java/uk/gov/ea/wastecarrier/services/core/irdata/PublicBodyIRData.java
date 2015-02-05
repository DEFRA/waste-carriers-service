package uk.gov.ea.wastecarrier.services.core.irdata;

import java.util.Date;

public class PublicBodyIRData extends IRData
{
	/**
	 * PUBLIC BODY
	 * CB_REFERENCE_NUMBER,EXPIRY_DATE,REGISTRATION_TYPE,APPLICANT_TYPE,PARTY_NAME,TRADING_NAME
	 */
	
	public PublicBodyIRData()
	{
		//
	}
	
	public PublicBodyIRData(String[] rawData)
	{
		// set full raw row data as a field
		setRawData(rawData);
		
        setReferenceNumber(rawData[0]);
		
        Date d = convertToDate(rawData[1]);
		setExpiryDate(d);
		
		setRegistrationType(rawData[2]);
		
		setApplicantType(rawData[3]);
		
		setPartyName(rawData[4]);
		
		if (rawData.length > 5) setTradingName(rawData[5]);
	}
	
	@Override
	public IRType getIrType()
	{
		return IRType.PUBLIC_BODY;
		
	}
	
	private String partyName;
	
	private String tradingName;

	/**
	 * @return the partyName
	 */
	public String getPartyName()
	{
		return partyName;
	}

	/**
	 * @param partyName the partyName to set
	 */
	public void setPartyName(String partyName)
	{
		this.partyName = partyName;
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

}
