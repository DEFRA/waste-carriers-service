package uk.gov.ea.wastecarrier.services.core;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Simple POJO for containing individual payment information
 * @author Steve
 *
 */
public class Payment
{
	@JsonProperty
	private String orderKey;
	@JsonProperty
	private int amount;
	@JsonProperty
	private String currency;
	@JsonProperty
	private String mac_code;
	@JsonProperty
	private String dateReceived; /*Date provided on payment details*/
	@JsonProperty
	private String dateEntered;  /*Date entered into the system*/
	@JsonProperty
	private String registrationReference;
	@JsonProperty
	private String worldPayPaymentStatus;
	@JsonProperty
	private String updatedByUser;
	@JsonProperty
	private String comment;
	@JsonProperty
	private PaymentType paymentType;
	
	public final static String COLLECTION_SINGULAR_NAME = "payment";
	public final static String COLLECTION_NAME = COLLECTION_SINGULAR_NAME +"s";
	
	public Payment()
	{
	}
	
	public Payment(int amount, String currency, String dateReceived,
			String updatedByUser, String comment, PaymentType paymentType)
	{
		super();
		this.amount = amount;
		this.currency = currency;
		this.dateReceived = dateReceived;
		this.updatedByUser = updatedByUser;
		this.comment = comment;
		this.paymentType = paymentType;
	}
	
	public Payment(String orderKey, int amount, String currency, String mac_code, String dateReceived,
			String dateEntered, String registrationReference, String worldPayPaymentStatus, String updatedByUser,
			String comment, PaymentType paymentType)
	{
		super();
		this.orderKey = orderKey;
		this.amount = amount;
		this.currency = currency;
		this.mac_code = mac_code;
		this.dateReceived = dateReceived;
		this.dateEntered = dateEntered;
		this.registrationReference = registrationReference;
		this.worldPayPaymentStatus = worldPayPaymentStatus;
		this.updatedByUser = updatedByUser;
		this.comment = comment;
		this.paymentType = paymentType;
	}
	
	public enum PaymentType {
		WORLDPAY, 
		WORLDPAY_MISSED,
		WRITEOFFSMALL, 
		WRITEOFFLARGE, 
		REFUND,
		CASH,
		CHEQUE,
		POSTALORDER,
		BANKTRANSFER,
		REVERSAL
	}
	
	/**
	 * @return the orderKey
	 */
	public String getOrderKey()
	{
		return orderKey;
	}

	/**
	 * @param orderKey the orderKey to set
	 */
	public void setOrderKey(String orderKey)
	{
		this.orderKey = orderKey;
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
	 * @return the mac_code
	 */
	public String getMac_code()
	{
		return mac_code;
	}

	/**
	 * @param mac_code the mac_code to set
	 */
	public void setMac_code(String mac_code)
	{
		this.mac_code = mac_code;
	}

	/**
	 * @return the dateReceived
	 */
	public String getDateReceived()
	{
		return dateReceived;
	}

	/**
	 * @param dateReceived the dateReceived to set
	 */
	public void setDateReceived(String dateReceived)
	{
		this.dateReceived = dateReceived;
	}

	/**
	 * @return the dateEntered
	 */
	public String getDateEntered()
	{
		return dateEntered;
	}

	/**
	 * This function should be called by the services tier to automatically set when the payment
	 * was entered into the system.
	 * @param dateEntered the dateEntered to set
	 */
	public void setDateEntered(String dateEntered)
	{
		this.dateEntered = dateEntered;
	}

	/**
	 * @return the registrationReference
	 */
	public String getRegistrationReference()
	{
		return registrationReference;
	}

	/**
	 * @param registrationReference the registrationReference to set
	 */
	public void setRegistrationReference(String registrationReference)
	{
		this.registrationReference = registrationReference;
	}

	/**
	 * @return the worldPayPaymentStatus
	 */
	public String getWorldPayPaymentStatus()
	{
		return worldPayPaymentStatus;
	}

	/**
	 * @param worldPayPaymentStatus the worldPayPaymentStatus to set
	 */
	public void setWorldPayPaymentStatus(String worldPayPaymentStatus)
	{
		this.worldPayPaymentStatus = worldPayPaymentStatus;
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
	 * @return the comment
	 */
	public String getComment()
	{
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment)
	{
		this.comment = comment;
	}

	/**
	 * @return the paymentType
	 */
	public PaymentType getPaymentType()
	{
		return paymentType;
	}

	/**
	 * @param paymentType the paymentType to set
	 */
	public void setPaymentType(PaymentType paymentType)
	{
		this.paymentType = paymentType;
	}

}
