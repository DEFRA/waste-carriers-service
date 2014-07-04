package uk.gov.ea.wastecarrier.services.core;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Simple POJO for containing individual purchase information
 * @author Steve
 *
 */
public class OrderItem
{
	@JsonProperty
	private int amount;
	@JsonProperty
	private String currency;
	private String lastUpdated;
	private String description;
	private String reference;
	
	public final static String COLLECTION_SINGULAR_NAME = "orderItem";
	public final static String COLLECTION_NAME = COLLECTION_SINGULAR_NAME +"s";
	
	public OrderItem()
	{
	}
	
	public OrderItem(int amount, String currency, String lastUpdated, String description, 
			String reference)
	{
		super();
		this.amount = amount;
		this.currency = currency;
		this.lastUpdated = lastUpdated;
		this.description = description;
		this.reference = reference;
	}
	
	/**
	 * @return the amount
	 */
	public int getAmount()
	{
		return amount;
	}
	/**
	 * @param amount the amount to set
	 */
	public void setAmount(int amount)
	{
		this.amount = amount;
	}
	/**
	 * @return the currency
	 */
	public String getCurrency()
	{
		return currency;
	}
	/**
	 * @param currency the currency to set
	 */
	public void setCurrency(String currency)
	{
		this.currency = currency;
	}
	/**
	 * @return the lastUpdated
	 */
	public String getLastUpdated()
	{
		return lastUpdated;
	}
	/**
	 * @param lastUpdated the lastUpdated to set
	 */
	public void setLastUpdated(String lastUpdated)
	{
		this.lastUpdated = lastUpdated;
	}
	/**
	 * @return the description
	 */
	public String getDescription()
	{
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * @return the reference
	 */
	public String getReference()
	{
		return reference;
	}

	/**
	 * @param reference the reference to set
	 */
	public void setReference(String reference)
	{
		this.reference = reference;
	}

}
