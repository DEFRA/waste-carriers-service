package uk.gov.ea.wastecarrier.services.core;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Order
{
	@JsonProperty
	private List<OrderItem> orderItems;
	
	@JsonProperty
	private String orderCode;
	
	private String merchantId;
	@JsonProperty
	private int totalAmount;
	@JsonProperty
	private String currency;
	
	private String dateCreated;
	@JsonProperty
	private String worldPayStatus;
	
	private String dateLastUpdated;
	
	private String updatedByUser;
	@JsonProperty
	private String description;
	
	public final static String COLLECTION_SINGULAR_NAME = "order";
	public final static String COLLECTION_NAME = COLLECTION_SINGULAR_NAME +"s";
	
	public Order()
	{
		super();
	}

	/*
	public Order(List<OrderItem> orderItems, String orderCode, String merchantId, double totalAmount, String currency,
			String dateCreated, String worldPayStatus, String dateLastUpdated, String updatedByUser, String description)
	{
		super();
		this.orderItems = orderItems;
		this.orderCode = orderCode;
		this.merchantId = merchantId;
		this.totalAmount = totalAmount;
		this.currency = currency;
		this.dateCreated = dateCreated;
		this.worldPayStatus = worldPayStatus;
		this.dateLastUpdated = dateLastUpdated;
		this.updatedByUser = updatedByUser;
		this.description = description;
	}*/

	/**
	 * @return the orderCode
	 */
	public String getOrderCode()
	{
		return orderCode;
	}

	/**
	 * @param orderCode the orderCode to set
	 */
	public void setOrderCode(String orderCode)
	{
		this.orderCode = orderCode;
	}

	/**
	 * @return the merchantId
	 */
	public String getMerchantId()
	{
		return merchantId;
	}

	/**
	 * @param merchantId the merchantId to set
	 */
	public void setMerchantId(String merchantId)
	{
		this.merchantId = merchantId;
	}

	/**
	 * @return the totalAmount
	 */
	public int getTotalAmount()
	{
		return totalAmount;
	}

	/**
	 * @param totalAmount the totalAmount to set
	 */
	public void setTotalAmount(int totalAmount)
	{
		this.totalAmount = totalAmount;
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
	 * @return the dateCreated
	 */
	public String getDateCreated()
	{
		return dateCreated;
	}

	/**
	 * @param dateCreated the dateCreated to set
	 */
	public void setDateCreated(String dateCreated)
	{
		this.dateCreated = dateCreated;
	}

	/**
	 * @return the worldPayStatus
	 */
	public String getWorldPayStatus()
	{
		return worldPayStatus;
	}

	/**
	 * @param worldPayStatus the worldPayStatus to set
	 */
	public void setWorldPayStatus(String worldPayStatus)
	{
		this.worldPayStatus = worldPayStatus;
	}

	/**
	 * @return the dateLastUpdated
	 */
	public String getDateLastUpdated()
	{
		return dateLastUpdated;
	}

	/**
	 * @param dateLastUpdated the dateLastUpdated to set
	 */
	public void setDateLastUpdated(String dateLastUpdated)
	{
		this.dateLastUpdated = dateLastUpdated;
	}
	
	/**
	 * @return the updatedByUser
	 */
	public String getUpdatedByUser()
	{
		return updatedByUser;
	}

	/**
	 * @param updatedByUser the updatedByUser to set
	 */
	public void setUpdatedByUser(String updatedByUser)
	{
		this.updatedByUser = updatedByUser;
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
	 * @return the orderItems
	 */
	public List<OrderItem> getOrderItems()
	{
		return orderItems;
	}

	/**
	 * @param orderItems the orderItems to set
	 */
	public void setOrderItems(List<OrderItem> orderItems)
	{
		this.orderItems = orderItems;
	}

}
