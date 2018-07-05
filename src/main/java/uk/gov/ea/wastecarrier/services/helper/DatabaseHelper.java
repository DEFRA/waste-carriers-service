package uk.gov.ea.wastecarrier.services.helper;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.*;
import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;

/**
 * This class is intended to make the various database connections and operations simple by handling the database 
 * configuration and setup, and returning a connection object to be operated upon.
 *
 */
public class DatabaseHelper {

    private MongoClient mongoClient;

    private DB db;

    private Logger log = Logger.getLogger(DatabaseHelper.class.getName());
    
    private DatabaseConfiguration dbConfig;

    public DatabaseHelper(DatabaseConfiguration database) {
        // Save configuration
        this.dbConfig = database;
    }

    /**
     * Gets a database connection
     *
     * @return an Active DB connection object or null if one could not be obtained
     */
    public DB getConnection() {
        log.logp(Level.FINE, DatabaseHelper.class.getName(), "getConnection", "Get connection");
        if (db != null) {
            // Use existing connection
            try {
                // Use existing connection
                log.logp(Level.FINE, DatabaseHelper.class.getName(), "getConnection", "Returning cached connection");
                return db;
            }
            catch (Exception e) {
                log.severe("Could not connect to database " + this.dbConfig.getMongoClientURI().getDatabase() + ": " + e.getMessage());
                return null;
            }
        } else {
            // Create new connection
            // Get Database Client
            MongoClient mc = getMongoClient();
            try {
                // Get Specific database
                db = mc.getDB(dbConfig.getMongoClientURI().getDatabase());
            }
            catch (Exception e) {
                log.severe("Database connection not found " + this.dbConfig.getMongoClientURI().getDatabase() + ": " + e.getMessage());
                db = null;
                return null;
            }
            log.logp(Level.FINE, DatabaseHelper.class.getName(), "getConnection", "Returning new connection");
            return db;
        }
    }

    /**
     * @return the mongoClient, or null if errors occurred
     */
    public MongoClient getMongoClient() {

        if (mongoClient != null) {
            return mongoClient;
        } else {
            this.mongoClient = new MongoClient(this.dbConfig.getMongoClientURI());

            return mongoClient;
        }
    }

    /**
     * Get the specified collection from the database.
     *
     * @param collectionName Name of collection to get
     * @return The matching collection from the database
     */
    public DBCollection getCollection(String collectionName) {
        return getConnection().getCollection(collectionName);
    }

    public DatabaseConfiguration configuration() {
        return this.dbConfig;
    }
}
