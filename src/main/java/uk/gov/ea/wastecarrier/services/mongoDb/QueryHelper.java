package uk.gov.ea.wastecarrier.services.mongoDb;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import net.vz.mongodb.jackson.JacksonDBCollection;
import uk.gov.ea.wastecarrier.services.WasteCarrierService;
import uk.gov.ea.wastecarrier.services.core.Registration;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class QueryHelper {

    private Logger log = Logger.getLogger(QueryHelper.class.getName());
    private DatabaseHelper databaseHelper;

    public QueryHelper(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public List<Registration> toRegistrationList(DBCursor cursor) {
        JacksonDBCollection<Registration, Object> jackColl = JacksonDBCollection
                .wrap(cursor.getCollection(), Registration.class);
        net.vz.mongodb.jackson.DBCursor<Registration> jackCursor = new net.vz.mongodb.jackson.DBCursor<Registration>(
                jackColl, cursor);
        return toList(jackCursor);
    }

    private <T> List<T> toList(net.vz.mongodb.jackson.DBCursor<T> cursor) {
        List<T> returnList = new LinkedList<T>();
        for (T r : cursor) {
            returnList.add(r);
        }
        return returnList;
    }

    protected void addOptionalQueryProperty(
            String propertyName,
            Collection<String> propertyValue,
            Map<String, Object> queryProps) {

        if (propertyValue == null || propertyValue.size() == 0) {
            return;
        }

        if (propertyValue.size() == 1) {
            String value = propertyValue.iterator().next();
            if (value == null || "".equals(value)) {
                return;
            }
            queryProps.put(propertyName, processQueryValue(value));
            return;
        }

        String[] processed = new String[propertyValue.size()];
        int index = 0;
        for (String value : propertyValue) {
            processed[index] = processQueryValue(value);
            index++;
        }
        queryProps.put(propertyName, processed);
    }

    private String processQueryValue(String value) {
        return "NULL".equals(value) ? null : value;
    }

    protected DBCollection getRegistrationsCollection() {
        return getDatabase().getCollection(Registration.COLLECTION_NAME);
    }

    private DB getDatabase() {
        // TODO - Replace/refactor the DatabaseHelper
        DB db = databaseHelper.getConnection();

        if (db == null) {
            // Database connection is null - not available???
            log.severe("Database not available, check the database is running");
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
        }
        if (!db.isAuthenticated()) {
            log.info("Database not authenticated, access forbidden");
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        return db;
    }
}
