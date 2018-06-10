package uk.gov.ea.wastecarrier.services.dao;
import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.User;

import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import com.mongodb.DB;

import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import uk.gov.ea.wastecarrier.services.helper.DatabaseHelper;

public class UserDao
{
    /** logger for this class. */
    private static Logger log = Logger.getLogger(UserDao.class.getName());

    /** The database helper. */
    private DatabaseHelper databaseHelper;

    public UserDao(DatabaseConfiguration database)
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