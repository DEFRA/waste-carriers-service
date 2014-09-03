package uk.gov.ea.wastecarrier.services.core;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.validation.Valid;

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
	@Valid
	@JsonProperty
	private String currency;
	private Date lastUpdated;
	@Valid
	@JsonProperty
	private String description;
	@JsonProperty
	private String reference;
	@Nonnull
	@JsonProperty
	private OrderItemType type;
	
	public final static String COLLECTION_SINGULAR_NAME = "orderItem";
	public final static String COLLECTION_NAME = COLLECTION_SINGULAR_NAME +"s";
	
	public enum OrderItemType {
		NEW,
		EDIT,
		RENEW,
		IRRENEW,	/* The IRRENEW might not be needed? */
		COPY_CARDS
	}
	
	public OrderItem()
	{
	}
	
	public OrderItem(int amount, String currency, Date lastUpdated, String description, 
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
	public Date getLastUpdated()
	{
		return lastUpdated;
	}
	/**
	 * @param lastUpdated the lastUpdated to set
	 */
	public void setLastUpdated(Date lastUpdated)
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
	
	/**
	 * @return the type
	 */
	public OrderItemType getType()
	{
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(OrderItemType type)
	{
		this.type = type;
	}

}
