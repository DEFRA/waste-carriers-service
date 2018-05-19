package uk.gov.ea.wastecarrier.services.dao;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;
import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.Entity;
import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

public class EntityMatchingDao {

    public static final String COLLECTION_NAME = "convictions";

    private static Logger log = Logger.getLogger(EntityMatchingDao.class.getName());
    private DatabaseHelper databaseHelper;

    public EntityMatchingDao(DatabaseConfiguration configuration) {
        this.databaseHelper = new DatabaseHelper(configuration);
    }

    public Entity find(String id) {

        JacksonDBCollection<Entity, String> collection = getCollection();

        try {
            return collection.findOneById(id);
        } catch (IllegalArgumentException e) {
            log.severe("Caught exception: Cannot find Entity ID " + id + ". " + e.getMessage());
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    public Entity insert(Entity entity) {

        Entity savedInstance = null;
        JacksonDBCollection<Entity, String> collection = getCollection();

        // Insert registration information into database
        WriteResult<Entity, String> result = collection.insert(entity);

        // Get unique ID out of response, and find updated record
        String id = result.getSavedId();
        savedInstance = collection.findOneById(id);

        return savedInstance;
    }

    public JacksonDBCollection<Entity, String> getCollection() {

        DB db = this.databaseHelper.getConnection();

        if (db == null) {
            log.severe("Could not establish database connection to MongoDB! Check the database is running");
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
        }

        return JacksonDBCollection.wrap(
                db.getCollection(COLLECTION_NAME), Entity.class, String.class);
    }
}
