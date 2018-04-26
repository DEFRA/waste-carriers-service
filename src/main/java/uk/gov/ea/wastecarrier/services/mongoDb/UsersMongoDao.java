package uk.gov.ea.wastecarrier.services.mongoDb;
import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.User;

import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;

import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

public class UsersMongoDao
{
	/** logger for this class. */
	private static Logger log = Logger.getLogger(UsersMongoDao.class.getName());
	
	/** The database helper. */
	private DatabaseHelper databaseHelper;

	public UsersMongoDao(DatabaseConfiguration database)
	{
		log.info("host = " + database.getHost());
		log.info("port = " + database.getPort());
		log.info("name = " + database.getName());
		log.info("username = " + database.getUsername());
		
		log.fine("Constructing DAO with databaseHelper.");
		this.databaseHelper = new DatabaseHelper(database);
	}
	
	public User getUserByEmail(String email)
	{
		User foundUser;

    	log.fine("Retrieving user with email = " + email);
    	
    	// Use id to lookup record in database return registration details
    	DB db = databaseHelper.getConnection();
		if (db != null)
        {
			// Create MONGOJACK connection to the database
			JacksonDBCollection<User, String> users = JacksonDBCollection.wrap(
					db.getCollection(User.COLLECTION_NAME),
                    User.class,
                    String.class
            );

            // Query to find matching reference number
            DBQuery.Query paramQuery = DBQuery.is("email", email);
			
			try
			{
				foundUser = users.findOne(paramQuery);
			}
			catch (IllegalArgumentException e)
			{
				log.warning("Caught exception: " + e.getMessage() + " - Cannot find User email: " + email);
				throw new WebApplicationException(Status.NOT_FOUND);
			}
		}
		else {
            log.severe("Get user - Could not obtain connection to MongoDB!");
            throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
        }

        return foundUser;
	}
}
