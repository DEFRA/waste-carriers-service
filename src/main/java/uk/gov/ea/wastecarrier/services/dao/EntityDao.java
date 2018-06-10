package uk.gov.ea.wastecarrier.services.dao;

import com.mongodb.DB;

import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import java.util.List;
import java.util.logging.Logger;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.Entity;
import uk.gov.ea.wastecarrier.services.helper.DatabaseHelper;

public class EntityDao implements ICanGetCollection<Entity> {

    public static final String COLLECTION_NAME = "entities";

    private static Logger log = Logger.getLogger(EntityDao.class.getName());
    private DatabaseHelper databaseHelper;

    public EntityDao(DatabaseConfiguration configuration) {
        this.databaseHelper = new DatabaseHelper(configuration);
    }

    public Entity find(String id) {
        return find(getCollection(), id);
    }

    public Entity insert(Entity entity) {

        JacksonDBCollection<Entity, String> collection = getCollection();

        // Insert entity information into database
        WriteResult<Entity, String> result = collection.insert(entity);

        // Get unique ID out of response, and find updated record
        String id = result.getSavedId();

        return find(collection, id);
    }

    public JacksonDBCollection<Entity, String> getCollection() {

        DB db = this.databaseHelper.getConnection();

        if (db == null) {
            log.severe("Could not establish database connection to MongoDB! Check the database is running");
            throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
        }

        return JacksonDBCollection.wrap(
                db.getCollection(COLLECTION_NAME), Entity.class, String.class);
    }
    public void recreate(List<Entity> entities) {
        this.getCollection().drop();

        JacksonDBCollection<Entity, String> collection = getCollection();

        // Insert entity information into database
        WriteResult<Entity, String> result = collection.insert(entities);
    }

    private Entity find(JacksonDBCollection<Entity, String> collection, String id) {

        try {
            return collection.findOneById(id);
        } catch (IllegalArgumentException e) {
            log.severe("Error finding Entity ID " + id + ": " + e.getMessage());
            throw new WebApplicationException(Status.NOT_FOUND);
        }
    }
}
