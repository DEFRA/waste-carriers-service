package uk.gov.ea.wastecarrier.services.dao;

import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;

import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;
import org.mongojack.DBQuery;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import java.util.logging.Logger;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.helper.DatabaseHelper;

public class RegistrationDao implements ICanGetCollection<Registration> {
    public static final String COLLECTION_NAME = "registrations";

    private static Logger log = Logger.getLogger(RegistrationDao.class.getName());
    private DatabaseHelper databaseHelper;

    public RegistrationDao(DatabaseConfiguration database) {
        this.databaseHelper = new DatabaseHelper(database);
    }

    public boolean checkConnection() {
        return getCollection().count() >= 0;
    }

    public Registration find(String id) {
        return find(getCollection(), id);
    }

    public Registration findByRegIdentifier(String registrationNumber) {

        JacksonDBCollection<Registration, String> collection = getCollection();

        Registration foundReg;

        DBQuery.Query paramQuery = DBQuery.is("regIdentifier", registrationNumber);

        try {
            foundReg = collection.findOne(paramQuery);
        } catch (IllegalArgumentException e) {
            log.severe("Error finding Registration " + registrationNumber + ": "  + e.getMessage());
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        if (foundReg == null) {
            log.info("Failed to find registration " + registrationNumber);
            throw new WebApplicationException(Status.NO_CONTENT);
        }

        return foundReg;
    }

    public Registration insert(Registration registration) {

        JacksonDBCollection<Registration, String> collection = getCollection();

        // Insert registration information into database
        WriteResult<Registration, String> result = collection.insert(registration);

        // Get unique ID out of response, and find inserted record
        String id = result.getSavedId();

        return find(collection, id);
    }

    public Registration update(Registration reg) {

        JacksonDBCollection<Registration, String> collection = getCollection();

        // If object found
        WriteResult<Registration, String> result = collection.updateById(reg.getId(), reg);
        if (!result.getWriteResult().wasAcknowledged()) {
            log.severe("Error updating Registration " + reg.getId() + ": Update was not acknowledged");
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        return find(collection, reg.getId());
    }

    public JacksonDBCollection<Registration, String> getCollection() {

        DB db = this.databaseHelper.getConnection();

        if (db == null) {
            log.severe("Could not establish database connection to MongoDB! Check the database is running");
            throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
        }

        return JacksonDBCollection.wrap(
                db.getCollection(COLLECTION_NAME), Registration.class, String.class);
    }

    private Registration find(JacksonDBCollection<Registration, String> collection, String id) {

        try {
            return collection.findOneById(id);
        } catch (IllegalArgumentException e) {
            log.severe("Error finding Registration ID " + id + ": " + e.getMessage());
            throw new WebApplicationException(Status.NOT_FOUND);
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
