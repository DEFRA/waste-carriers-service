package uk.gov.ea.wastecarrier.services.dao;

import com.mongodb.DB;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;
import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.mocks.WorldpayOrder;
import uk.gov.ea.wastecarrier.services.helper.DatabaseHelper;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import java.util.logging.Logger;

public class MockWorldpayDao implements ICanGetCollection<WorldpayOrder> {

    public static final String COLLECTION_NAME = "mock-worldpay-orders";

    private DatabaseHelper databaseHelper;

    private static Logger log = Logger.getLogger(MockWorldpayDao.class.getName());

    public MockWorldpayDao(DatabaseConfiguration database) {
        this.databaseHelper = new DatabaseHelper(database);
    }

    public boolean checkConnection() {
        return getCollection().count() >= 0;
    }

    public WorldpayOrder find(String id) {
        return find(getCollection(), id);
    }

    public WorldpayOrder findByOrderCode(String orderCode) {

        JacksonDBCollection<WorldpayOrder, String> collection = getCollection();

        WorldpayOrder found;

        DBQuery.Query paramQuery = DBQuery.is("orderCode", orderCode);

        try {
            found = collection.findOne(paramQuery);
        } catch (IllegalArgumentException e) {
            log.severe("Error finding WorldpayOrder " + orderCode + ": "  + e.getMessage());
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        if (found == null) {
            log.info("Failed to find WorldpayOrder " + orderCode);
            throw new WebApplicationException(Status.NO_CONTENT);
        }

        return found;
    }

    public WorldpayOrder insert(WorldpayOrder entity) {

        JacksonDBCollection<WorldpayOrder, String> collection = getCollection();

        // Insert entity information into database
        WriteResult<WorldpayOrder, String> result = collection.insert(entity);

        // Get unique ID out of response, and find updated record
        String id = result.getSavedId();

        return find(collection, id);
    }

    public JacksonDBCollection<WorldpayOrder, String> getCollection() {

        DB db = this.databaseHelper.getConnection();

        if (db == null) {
            log.severe("Could not establish database connection to MongoDB! Check the database is running");
            throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
        }

        return JacksonDBCollection.wrap(
                db.getCollection(COLLECTION_NAME), WorldpayOrder.class, String.class);
    }

    private WorldpayOrder find(JacksonDBCollection<WorldpayOrder, String> collection, String id) {

        try {
            return collection.findOneById(id);
        } catch (IllegalArgumentException e) {
            log.severe("Error finding WorldpayOrder ID " + id + ": " + e.getMessage());
            throw new WebApplicationException(Status.NOT_FOUND);
        }
    }
}
