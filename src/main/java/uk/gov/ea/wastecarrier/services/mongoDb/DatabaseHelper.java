package uk.gov.ea.wastecarrier.services.mongoDb;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.*;
import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;

import com.mongodb.client.MongoDatabase;

/**
 * This class is intended to make the various database connections and operations simple by handling the database 
 * configuration and setup, and returning a connection object to be operated upon.
 *
 */
public class DatabaseHelper
{
    private MongoClient mongoClient;

    private DB db;

    private Logger log = Logger.getLogger(DatabaseHelper.class.getName());
    
    private DatabaseConfiguration dbConfig;

    public DatabaseHelper(DatabaseConfiguration database)
    {
        // Get connection properties from environment settings
        log.logp(Level.FINE, DatabaseHelper.class.getName(), "DatabaseHelper",
                "Init DatabaseHelper using database params: " + database.getHost() +":"+ database.getPort());

        // Save configuration
        this.dbConfig = database;

    }

    /**
     * Gets a database connection
     *
     * @return an Active DB connection object or null if one could not be obtained
     */
    public DB getConnection()
    {
        log.logp(Level.FINE, DatabaseHelper.class.getName(), "getConnection", "Get connection");
        if (db != null)
        {
            // Use existing connection
            try
            {
                // Use existing connection
                log.logp(Level.FINE, DatabaseHelper.class.getName(), "getConnection", "Returning cached connection");
                return db;
            }
            catch (Exception e)
            {
                log.severe("Could not connect to database: " + e.getMessage());
                return null;
            }
        }
        else
        {
            // Create new connection
            // Get Database Client
            MongoClient mc = getMongoClient();
            try
            {
                // Get Specific database
                db = mc.getDB(dbConfig.getName());
            }
            catch (Exception e)
            {
                log.severe("Database connection not Found: " + e.getMessage());
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
    public MongoClient getMongoClient()
    {
        if (mongoClient != null)
        {
            return mongoClient;
        }
        else
        {
            MongoCredential credential = MongoCredential.createCredential(
                    dbConfig.getUsername(),
                    dbConfig.getName(),
                    dbConfig.getPassword().toCharArray()
            );
            ServerAddress server = new ServerAddress(dbConfig.getHost(), dbConfig.getPort());
            MongoClientOptions options = MongoClientOptions.builder().build();
            MongoClient client = new MongoClient(server, credential, options);

            setMongoClient(client);

            return mongoClient;
        }
    }

    public DBCollection getCollection(String collectionName) {
        return getConnection().getCollection(collectionName);
    }

    /**
     * @param mongoClient the mongoClient to set
     */
    private void setMongoClient(MongoClient mongoClient)
    {
        this.mongoClient = mongoClient;
    }
}
