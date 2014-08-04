package uk.gov.ea.wastecarrier.services.mongoDb;

import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import net.vz.mongodb.jackson.DBUpdate;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.WriteResult;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.FinanceDetails;
import uk.gov.ea.wastecarrier.services.core.Order;
import uk.gov.ea.wastecarrier.services.core.OrderItem;
import uk.gov.ea.wastecarrier.services.core.Registration;

public class OrdersMongoDao
{
	/** logger for this class. */
	private static Logger log = Logger.getLogger(PaymentsMongoDao.class.getName());
	
	/** The database helper. */
	private DatabaseHelper databaseHelper;
	
	/**
	 * Constructor with arguments
	 * @param databaseHelper the DatabaseHelper
	 */
	public OrdersMongoDao(DatabaseConfiguration database)
	{
		log.fine("Constructing DAO with databaseHelper.");
		this.databaseHelper = new DatabaseHelper(database);
	}

	public Order addOrder(String registrationId, Order order)
	{
		log.info("Adding order to registration with id = " + registrationId);
		DB db = databaseHelper.getConnection();
		if (db != null)
		{
			if (!db.isAuthenticated())
			{
				throw new WebApplicationException(Status.FORBIDDEN);
			}
			
			/*
			 * Create MONGOJACK connection to the database
			 */
			JacksonDBCollection<Registration, String> registrations = JacksonDBCollection.wrap(
					db.getCollection(Registration.COLLECTION_NAME), Registration.class, String.class);
			
			/*
			 * Update registration with order information into the database
			 */
			WriteResult<Registration, String> result = registrations.updateById(registrationId, 
					DBUpdate.push(FinanceDetails.COLLECTION_NAME + "." + Order.COLLECTION_NAME, order));
			
			if (result.getError() == null)
			{
				return order;
			}
			else
			{
				log.severe("Error occured while updating registration with a order, " + result.getError());
				throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
			}
		}
		else
		{
			log.severe("Could not establish database connection to MongoDB! Check the database is running");
			throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
		}
	}
	
	
	public Order updateOrder(String registrationId, String orderId, Order order)
	{
		log.info("Updating order " + orderId + " of registration id = " + registrationId);
		DB db = databaseHelper.getConnection();
		if (db == null)
		{
			log.severe("Could not establish database connection to MongoDB! Check the database is running");
			throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
		}

		if (!db.isAuthenticated())
		{
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		
		/*
		 * Create MONGOJACK connection to the database
		 */
		JacksonDBCollection<Registration, String> registrations = JacksonDBCollection.wrap(
				db.getCollection(Registration.COLLECTION_NAME), Registration.class, String.class);
		
		/*
		 * Update registration with order information into the database
		 * Using the positional operator ('.$.') to update the given Order sub-document within the orders array/list of the registration
		 */
		
		Registration registration = registrations.findOneById(registrationId);
		if (registration == null)
		{
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		
		DBObject query = new BasicDBObject();
		query.put("regIdentifier", registration.getRegIdentifier());
		query.put("financeDetails.orders.orderId", orderId);
		
		WriteResult<Registration, String> result;
		
		//Note: This works with updating individual properties, but updating the whole Order is still TBD
		DBObject update = new BasicDBObject();
		DBObject updates = new BasicDBObject();
		updates.put("financeDetails.orders.$.orderCode", order.getOrderCode());
		updates.put("financeDetails.orders.$.paymentMethod", order.getPaymentMethod().toString());
		updates.put("financeDetails.orders.$.merchantId", order.getMerchantId());
		updates.put("financeDetails.orders.$.totalAmount", order.getTotalAmount());
		updates.put("financeDetails.orders.$.currency", order.getCurrency());
		// We never want to update the creation date
		//updates.put("financeDetails.orders.$.dateCreated", order.getDateCreated());
		updates.put("financeDetails.orders.$.worldPayStatus", order.getWorldPayStatus());
		updates.put("financeDetails.orders.$.dateLastUpdated", order.getDateLastUpdated());
		updates.put("financeDetails.orders.$.updatedByUser", order.getUpdatedByUser());
		updates.put("financeDetails.orders.$.description", order.getDescription());
		
		// Add Order item updating
		int orderCount = 0;
		for (OrderItem o : order.getOrderItems())
		{
			updates.put("financeDetails.orders.$.orderItems."+orderCount+".amount", o.getAmount());
			updates.put("financeDetails.orders.$.orderItems."+orderCount+".description", o.getDescription());
			updates.put("financeDetails.orders.$.orderItems."+orderCount+".currency", o.getCurrency());
			updates.put("financeDetails.orders.$.orderItems."+orderCount+".lastUpdated", o.getLastUpdated());
			updates.put("financeDetails.orders.$.orderItems."+orderCount+".reference", o.getReference());
			orderCount++;
		}
		
		update.put("$set", updates);
		result = registrations.update(query, update);
		
		
		//WriteResult<Registration, String> result = registrations.updateById(registrationId, 
		//		DBUpdate.set(FinanceDetails.COLLECTION_NAME + "." + Order.COLLECTION_NAME + ".$." + orderId, order));
		
		if (result.getError() == null)
		{
			return order;
		}
		else
		{
			log.severe("Error occured while updating registration with a order, " + result.getError());
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
	}

}
