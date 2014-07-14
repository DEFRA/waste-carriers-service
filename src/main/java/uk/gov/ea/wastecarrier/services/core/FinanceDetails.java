package uk.gov.ea.wastecarrier.services.core;

import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * FinanceDetails contains all monetary details related to a registration, 
 * such as the record of orders and the record of payments received, 
 * it can also calculate a balance.
 * 
 * @author Steve
 *
 */
public class FinanceDetails
{
	@JsonProperty
	private List<Order> orders;
	
	@JsonProperty
	private List<Payment> payments;
	
	private int balance;
	
	public final static String COLLECTION_SINGULAR_NAME = "financeDetail";
	public final static String COLLECTION_NAME = COLLECTION_SINGULAR_NAME +"s";
	
	public FinanceDetails()
	{
	}

	/**
	 * @return the orders
	 */
	public List<Order> getOrders()
	{
		return orders;
	}

	/**
	 * @param orders the orders to set
	 */
	public void setOrders(List<Order> orders)
	{
		this.orders = orders;
	}

	/**
	 * @return the payments
	 */
	public List<Payment> getPayments()
	{
		return payments;
	}

	/**
	 * @param payments the payments to set
	 */
	public void setPayments(List<Payment> payments)
	{
		this.payments = payments;
	}

	/**
	 * @return the balance
	 */
	public int getBalance()
	{
		/*
		 * Calculate balance from values in orders, and deduct payment.
		 */
		int runningTotal = 0;
		if (getOrders() != null)
		{
			for (Order o : getOrders())
			{
				runningTotal += o.getTotalAmount();
			}
		}
		if (getPayments() != null)
		{
			for (Payment p : getPayments())
			{
				runningTotal -= p.getAmount();
			}
		}
		this.balance = runningTotal;
		return balance;
	}

	/**
	 * @param balance the balance to set
	 */
	public void setBalance(int balance)
	{
		this.balance = balance;
	}
	
	/**
	 * @param orderCode
	 * @return the Payment with the given orderCode, or null if it does not exist
	 */
	public Payment getPaymentForOrderCode(String orderCode)
	{
		if (orderCode == null)
		{
			return null;
		}
		for (Payment payment: getPayments())
		{
			if (orderCode.equals(payment.getOrderKey()))
			{
				return payment;
			}
		}
		return null;
	}
}
