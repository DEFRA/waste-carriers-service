/**
 * 
 */
package uk.gov.ea.wastecarrier.services.mongoDb;

import java.util.logging.Logger;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import uk.gov.ea.wastecarrier.services.core.Registration;

/**
 * 
 * Data Access Object for registrations in MongoDB.
 * @author gmueller
 *
 */
public class RegistrationsMongoDao {

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
