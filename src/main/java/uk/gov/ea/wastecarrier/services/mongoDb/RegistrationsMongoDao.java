/**
 * 
 */
package uk.gov.ea.wastecarrier.services.mongoDb;

import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import net.vz.mongodb.jackson.DBQuery;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.WriteResult;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import uk.gov.ea.wastecarrier.services.core.Registration;

/**
 * 
 * Data Access Object for registrations in MongoDB.
 *
 */
public class RegistrationsMongoDao 
{

	/** logger for this class. */
	private static Logger log = Logger.getLogger(RegistrationsMongoDao.class.getName());

	/** The database helper. */
	private DatabaseHelper databaseHelper;
	
	/**
	 * Default constructor.
	 */
	public RegistrationsMongoDao()
	{
		log.fine("Entering empty DAO constructor");
	}
	
	/**
	 * Constructor with arguments
	 * @param databaseHelper the DatabaseHelper
	 */
	public RegistrationsMongoDao(DatabaseHelper databaseHelper)
	{
		log.fine("Constructing DAO with databaseHelper.");
		this.databaseHelper = databaseHelper;
	}
	
	
	/**
	 * Insert the registration into the database
	 * @param registration
	 * @return the inserted registration
	 */
	public Registration insertRegistration(Registration registration)
	{

		log.info("Inserting registration into MongoDB");
		Registration savedObject = null;

		DB db = databaseHelper.getConnection();
		if (db != null)
		{
			if (!db.isAuthenticated())
			{
				throw new WebApplicationException(Status.FORBIDDEN);
			}
			
			// Create MONGOJACK connection to the database
			JacksonDBCollection<Registration, String> registrations = JacksonDBCollection.wrap(
					db.getCollection(Registration.COLLECTION_NAME), Registration.class, String.class);

			// Insert registration information into database
			WriteResult<Registration, String> result = registrations.insert(registration);

			// Get unique ID out of response, and find updated record
			String id = result.getSavedId();
			savedObject = registrations.findOneById(id);

			// Return saved object to user (returned as JSON)
			return savedObject;
		}
		else
		{
			log.severe("Could not establish database connection to MongoDB! Check the database is running");
			throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
		}
	
	}

	public Registration findRegistration(String registrationNumber)
	{
		Registration foundReg;

		log.info("Finding registration with reg identifier = " + registrationNumber);

		DB db = databaseHelper.getConnection();
		if (db != null)
		{
			if (!db.isAuthenticated())
			{
				log.severe("Find registration - Could not authenticate against MongoDB!");
				throw new WebApplicationException(Status.FORBIDDEN);
			}

			// Create MONGOJACK connection to the database
			JacksonDBCollection<Registration, String> registrations = JacksonDBCollection.wrap(
					db.getCollection(Registration.COLLECTION_NAME),
					Registration.class,
					String.class
			);

			// Query to find matching reference number
            DBQuery.Query paramQuery = DBQuery.is("regIdentifier", registrationNumber);

			try
			{
				foundReg = registrations.findOne(paramQuery);
			}
			catch (IllegalArgumentException e)
			{
				log.severe("Caught exception: " + e.getMessage() + " - Cannot find Registration: " + registrationNumber);
				throw new WebApplicationException(Status.NOT_FOUND);
			}
		}
        else
        {
            log.severe("Find registration - Could not obtain connection to MongoDB!");
            throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
        }

		if (foundReg == null)
		{
			log.info("Failed to find registration with number: " + registrationNumber);
			throw new WebApplicationException(Status.NO_CONTENT);
		}
		return foundReg;
	}
	
	/**
	 * Return the registration with the given id
	 * @param id
	 * @return
	 */
	public Registration getRegistration(String id)
	{
		
    	log.info("Retrieving registration  with id = " + id);
    	
    	// Use id to lookup record in database return registration details
    	DB db = databaseHelper.getConnection();
		if (db != null)
		{	
			if (!db.isAuthenticated())
			{
				log.severe("Get registration - Could not authenticate against MongoDB!");
				throw new WebApplicationException(Status.FORBIDDEN);
			}
			
			// Create MONGOJACK connection to the database
			JacksonDBCollection<Registration, String> registrations = JacksonDBCollection.wrap(
					db.getCollection(Registration.COLLECTION_NAME), Registration.class, String.class);
			
			try
			{
				Registration foundReg = registrations.findOneById(id);
				return foundReg;
			}
			catch (IllegalArgumentException e)
			{
				log.severe("Caught exception: " + e.getMessage() + " - Cannot find Registration ID: " + id);
				throw new WebApplicationException(Status.NOT_FOUND);
			}
		}
		else
		{
			log.severe("Get registration - Could not obtain connection to MongoDB!");
			throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
		}
	}

	public Registration updateRegistration(Registration reg)
	{
		
    	log.info("Updating registration  with id = " + reg.getId());
    	
    	// Use id to lookup record in database return registration details
    	DB db = databaseHelper.getConnection();
		if (db != null)
		{	
			if (!db.isAuthenticated())
			{
				log.severe("Update registration - Could not authenticate against MongoDB!");
				throw new WebApplicationException(Status.FORBIDDEN);
			}
			
			// Create MONGOJACK connection to the database
			JacksonDBCollection<Registration, String> registrations = JacksonDBCollection.wrap(
					db.getCollection(Registration.COLLECTION_NAME), Registration.class, String.class);
			
			// If object found
			WriteResult<Registration, String> result = registrations.updateById(reg.getId(), reg);
			log.fine("Found result: '" + result + "' " );
			
			if (!String.valueOf("").equals(result.getError()))
			{
				// did not error, so continue
				try
				{
					// Make a second request for the updated full registration details to be returned
					Registration savedObject = registrations.findOneById(reg.getId());
					
					return savedObject;
				}
				catch (IllegalArgumentException e)
				{
					log.severe("Caught exception: " + e.getMessage() + " - Cannot find Registration ID: " + reg.getId());
					throw new WebApplicationException(Status.NOT_FOUND);
				}
			}
			else
			{
				log.severe("Error while updating registration with ID " + reg.getId() + " in MongoDB. Result error:" + result.getError());
				throw new WebApplicationException(Status.NOT_MODIFIED);
			}
			
		}
		else
		{
			log.severe("Update registration - Could not obtain connection to MongoDB!");
			throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
		}
	}
	
	
	/**
	 * 
	 * @return the total number of registrations in the database.
	 */
	public long getNumberOfRegistrationsTotal() {
		log.info("Getting total number of registrations");
		long count = getRegistrationsCollection().getCount();
		log.info("The total number of registrations is: " + count);
		return count;
	}

	/**
	 * 
	 * @return the number of pending registrations in the database.
	 */
	public long getNumberOfRegistrationsPending() {
		log.info("Getting number of pending registrations");
		DBObject query = new BasicDBObject("metaData.status", "PENDING");
		long count = getRegistrationsCollection().getCount(query);
		log.info("The number of pending registrations is: " + count);
		return count;
	}
	
	/**
	 * Ensure that the indexes have been defined.
	 * 
	 * To prevent duplicate inserts we use a unique (but sparse) index on the uuid property.
	 * The index is sparse to support pre-existing data whose uuid has not been set upon insert.
	 * <code>
	 *   db.registrations.ensureIndex({uuid:1},{unique:true, sparse:true});
	 * </code>
	 */
	public void ensureIndexes() {
		log.info("Ensuring registration indexes...");
		DBObject keys = new BasicDBObject("uuid", 1);
		DBObject options = new BasicDBObject("unique", true).append("sparse", true);

		getRegistrationsCollection().ensureIndex(keys, options);
		log.info("Ensured registration indexes.");
	}
	
	private DB getDatabase() {
		//TODO - Replace/refactor the DatabaseHelper
		return databaseHelper.getConnection();
	}
	
	private DBCollection getRegistrationsCollection() {
		return getDatabase().getCollection(Registration.COLLECTION_NAME);
	}
}
