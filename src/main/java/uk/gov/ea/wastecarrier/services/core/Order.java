package uk.gov.ea.wastecarrier.services.core;

import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents something to be paid for together either via online payment or offline payment.
 * For example, a new upper tier registration or a renewal 
 * with or without copy cards requested represents one such order. 
 * A later request for more copy cards leads to another order.
 * <p>
 * Each registration maintains a list of orders that have been made for the registration,
 * e.g. the initial registration, separately requested copy cards, registration changes 
 * (if these changes are chargeable), or renewals.
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Order
{
	public enum PaymentMethod {
		ONLINE,
		OFFLINE,
		UNKNOWN
	}
	
	@JsonProperty
	private List<OrderItem> orderItems;

	@NotBlank
	@JsonProperty
	private String orderCode;
	
	@Nonnull
	@JsonProperty
	private PaymentMethod paymentMethod;
	
	@JsonProperty
	private String merchantId;
	
	@JsonProperty
	private int totalAmount;
	
	@JsonProperty
	private String currency;
	
	@JsonProperty
	private Date dateCreated;
	
	@JsonProperty
	private String worldPayStatus;
	
	@JsonProperty
	private Date dateLastUpdated;
	
	@JsonProperty
	private String updatedByUser;
	
	@JsonProperty
	private String description;
	
	public final static String COLLECTION_SINGULAR_NAME = "order";
	public final static String COLLECTION_NAME = "orders";
	
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
	 * @return the paymentMethod
	 */
	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	/**
	 * @param paymentMethod the paymentMethod to set
	 */
	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
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
	public Date getDateCreated()
	{
		return dateCreated;
	}

	/**
	 * @param dateCreated the dateCreated to set
	 */
	public void setDateCreated(Date dateCreated)
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
	public Date getDateLastUpdated()
	{
		return dateLastUpdated;
	}

	/**
	 * @param dateLastUpdated the dateLastUpdated to set
	 */
	public void setDateLastUpdated(Date dateLastUpdated)
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
