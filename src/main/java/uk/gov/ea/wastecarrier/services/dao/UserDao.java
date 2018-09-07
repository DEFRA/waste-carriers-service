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

public class UserDao implements ICanGetCollection<User> {

    public static final String COLLECTION_NAME = "users";

    private static Logger log = Logger.getLogger(UserDao.class.getName());

    private DatabaseHelper databaseHelper;

    public UserDao(DatabaseConfiguration database) {
        this.databaseHelper = new DatabaseHelper(database);
    }

    public boolean checkConnection() {
        return getCollection().count() >= 0;
    }

    public User findByEmail(String email) {

        JacksonDBCollection<User, String> collection = getCollection();

        User foundUser;

        DBQuery.Query paramQuery = DBQuery.is("email", email);

        try {
            foundUser = collection.findOne(paramQuery);
        } catch(IllegalArgumentException e) {
            log.severe("Error finding User " + email + ": "  + e.getMessage());
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        return foundUser;
    }

    public JacksonDBCollection<User, String> getCollection() {

        DB db = this.databaseHelper.getConnection();

        if (db == null) {
            log.severe("Could not establish database connection to MongoDB! Check the database is running");
            throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
        }

        return JacksonDBCollection.wrap(
                db.getCollection(COLLECTION_NAME),
                User.class,
                String.class
        );
    }
}
