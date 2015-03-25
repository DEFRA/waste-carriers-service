package uk.gov.ea.wastecarrier.services.core.irdata;

import java.util.Date;

public class CompanyIRData extends IRData
{
	/**
	 * COMPANY
	 * CB_REFERENCE_NUMBER,EXPIRY_DATE,REGISTRATION_TYPE,APPLICANT_TYPE,COMPANY_NAME,TRADING_NAME,COMPANY_NO
	 */
	
	public CompanyIRData()
	{
		//
	}
	
	public CompanyIRData(String[] rawData)
	{
		// set full raw row data as a field
		setRawData(rawData);
		
		setReferenceNumber(rawData[0]);
		
		Date d = convertToDate(rawData[1], getIrType());
		setExpiryDate(d);
		
		setRegistrationType(rawData[2]);
		
		setApplicantType(rawData[3]);
		
		setCompanyName(rawData[4]);
		
		setTradingName(rawData[5]);
		
		setCompanyNumber(rawData[6]);
		
	}
	
	private String companyName;
	
	private String tradingName;
	
	private String companyNumber;
	
	@Override
	public IRType getIrType()
	{
		return IRType.COMPANY;
		
	}

	/**
	 * @return the companyName
	 */
	public String getCompanyName()
	{
		return companyName;
	}

	/**
	 * @param companyName the companyName to set
	 */
	public void setCompanyName(String companyName)
	{
		this.companyName = companyName;
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
	 * @return the companyNumber
	 */
	public String getCompanyNumber()
	{
		return companyNumber;
	}

	/**
	 * @param companyNumber the companyNumber to set
	 */
	public void setCompanyNumber(String companyNumber)
	{
		this.companyNumber = companyNumber;
	}
}
