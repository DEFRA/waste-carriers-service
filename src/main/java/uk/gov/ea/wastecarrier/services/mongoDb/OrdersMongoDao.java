package uk.gov.ea.wastecarrier.services.mongoDb;

import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import net.vz.mongodb.jackson.DBUpdate;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.WriteResult;

import com.mongodb.DB;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.FinanceDetails;
import uk.gov.ea.wastecarrier.services.core.Order;
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
}
