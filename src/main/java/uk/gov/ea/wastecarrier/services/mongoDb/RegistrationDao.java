package uk.gov.ea.wastecarrier.services.mongoDb;

import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;

import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.dao.IDataAccessObject;

/**
 * 
 * Data Access Object for registrations in MongoDB.
 *
 */
public class RegistrationDao implements IDataAccessObject<Registration>
{
    public static final String COLLECTION_NAME = "registrations";

    /** logger for this class. */
    private static Logger log = Logger.getLogger(RegistrationDao.class.getName());

    /** The database helper. */
    private DatabaseHelper databaseHelper;

    /**
     * Constructor with arguments
     * @param database the DatabaseConfiguration
     */
    public RegistrationDao(DatabaseConfiguration database)
    {
        log.fine("Constructing DAO with databaseHelper.");
        this.databaseHelper = new DatabaseHelper(database);
    }

    public JacksonDBCollection<Registration, String> getCollection() {

        DB db = this.databaseHelper.getConnection();

        if (db == null) {
            log.severe("Could not establish database connection to MongoDB! Check the database is running");
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
        }

        return JacksonDBCollection.wrap(
                db.getCollection(COLLECTION_NAME), Registration.class, String.class);
    }

    /**
     * Insert the registration into the database
     * @param registration
     * @return the inserted registration
     */
    public Registration insert(Registration registration)
    {

        log.info("Inserting registration into MongoDB");
        Registration savedObject = null;

        DB db = databaseHelper.getConnection();
        if (db != null)
        {
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

    public Registration findByRegIdentifier(String registrationNumber)
    {
        Registration foundReg;

        log.info("Finding registration with reg identifier = " + registrationNumber);

        DB db = databaseHelper.getConnection();
        if (db != null)
        {
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
    public Registration find(String id)
    {

        log.info("Retrieving registration  with id = " + id);

        // Use id to lookup record in database return registration details
        DB db = databaseHelper.getConnection();
        if (db != null)
        {
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

    public Registration update(Registration reg)
    {

        log.info("Updating registration  with id = " + reg.getId());

        // Use id to lookup record in database return registration details
        DB db = databaseHelper.getConnection();
        if (db != null)
        {
            // Create MONGOJACK connection to the database
            JacksonDBCollection<Registration, String> registrations = JacksonDBCollection.wrap(
                    db.getCollection(Registration.COLLECTION_NAME), Registration.class, String.class);

            // If object found
            WriteResult<Registration, String> result = registrations.updateById(reg.getId(), reg);
            log.fine("Found result: '" + result + "' " );

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
            log.severe("Update registration - Could not obtain connection to MongoDB!");
            throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
        }
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

        this.databaseHelper.getCollection(Registration.COLLECTION_NAME).createIndex(keys, options);
        log.info("Ensured registration indexes.");
    }
}
