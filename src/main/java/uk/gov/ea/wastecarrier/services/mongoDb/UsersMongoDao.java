package uk.gov.ea.wastecarrier.services.mongoDb;

import java.util.UUID;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import net.vz.mongodb.jackson.DBCursor;
import net.vz.mongodb.jackson.DBQuery;
import net.vz.mongodb.jackson.DBUpdate;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.WriteResult;
import net.vz.mongodb.jackson.DBQuery.Query;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.FinanceDetails;
import uk.gov.ea.wastecarrier.services.core.Order;
import uk.gov.ea.wastecarrier.services.core.OrderItem;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.core.User;

public class UsersMongoDao
{
	/** logger for this class. */
	private static Logger log = Logger.getLogger(UsersMongoDao.class.getName());
	
	/** The database helper. */
	private DatabaseHelper databaseHelper;
	
	/**
	 * Constructor with arguments
	 * @param databaseHelper the DatabaseHelper
	 */
	public UsersMongoDao(DatabaseConfiguration database)
	{
		log.info("host = " + database.getHost());
		log.info("port = " + database.getPort());
		log.info("name = " + database.getName());
		log.info("username = " + database.getUsername());
		
		log.fine("Constructing DAO with databaseHelper.");
		this.databaseHelper = new DatabaseHelper(database);
	}
	
	public User getUserById(String id)
	{
		
    	log.fine("Retrieving user with id = " + id);
    	
    	// Use id to lookup record in database return registration details
    	DB db = databaseHelper.getConnection();
		if (db != null)
		{	
			if (!db.isAuthenticated())
			{
				log.severe("Get user - Could not authenticate against MongoDB!");
				throw new WebApplicationException(Status.FORBIDDEN);
			}
			
			// Create MONGOJACK connection to the database
			JacksonDBCollection<User, String> users = JacksonDBCollection.wrap(
					db.getCollection(User.COLLECTION_NAME), User.class, String.class);
			
			try
			{
				User user = users.findOneById(id);
				return user;
			}
			catch (IllegalArgumentException e)
			{
				log.warning("Caught exception: " + e.getMessage() + " - Cannot find User ID: " + id);
				throw new WebApplicationException(Status.NOT_FOUND);
			}
		}
		else
		{
			log.severe("Get user - Could not obtain connection to MongoDB!");
			throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
		}
	}
	
	public User getUserByEmail(String email)
	{
		
    	log.fine("Retrieving user with email = " + email);
    	
    	// Use id to lookup record in database return registration details
    	DB db = databaseHelper.getConnection();
		if (db != null)
		{	
			if (!db.isAuthenticated())
			{
				log.severe("Get user - Could not authenticate against MongoDB!");
				throw new WebApplicationException(Status.FORBIDDEN);
			}
			
			// Create MONGOJACK connection to the database
			JacksonDBCollection<User, String> users = JacksonDBCollection.wrap(
					db.getCollection(User.COLLECTION_NAME), User.class, String.class);
			
			try
			{
				Query paramQuery = DBQuery.is("email", email);
				DBCursor<User> dbcur = users.find(paramQuery).limit(1);
				
				User foundUser = null;
				for (User u : dbcur)
				{
					log.fine("> search found user email: " + u.getEmail());
					foundUser = u;
				}
				return foundUser;
			}
			catch (IllegalArgumentException e)
			{
				log.warning("Caught exception: " + e.getMessage() + " - Cannot find User email: " + email);
				throw new WebApplicationException(Status.NOT_FOUND);
			}
		}
		else
		{
			log.severe("Get user - Could not obtain connection to MongoDB!");
			throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
		}
	}
}
