package uk.gov.ea.wastecarrier.services.mongoDb;

import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;

import com.mongodb.DB;
import com.mongodb.MongoClient;

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
				// Check database status
				//getMongoClient().getDatabaseNames();
				// Use existing connection
				log.logp(Level.FINE, DatabaseHelper.class.getName(), "getConnection", "Returning cachced connection");
				return db;
			}
			catch (Exception e)
			{
				log.severe("Could not connect to database: " + e.getMessage() + "\n\nStackTrace:");
				for (StackTraceElement k : e.getStackTrace())
				{
					log.severe(">>" + k.toString());
				}
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
				db = mc.getDB( dbConfig.getName() );
				// Authenticate connection
				db.authenticate(dbConfig.getUsername(), dbConfig.getPassword().toCharArray());
			}
			catch (Exception e)
			{
				log.severe("Database connection not Found: " + e.getMessage() + "\n\nStackTrace:");
				for (StackTraceElement k : e.getStackTrace())
				{
					log.info("! " + k.toString());
				}
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
			try
			{
				setMongoClient(new MongoClient( dbConfig.getHost() , dbConfig.getPort() ));
			}
			catch (UnknownHostException e)
			{
				log.severe("Cannot find Host: " + e.getMessage() + "\n\nStackTrace:");
				for (StackTraceElement k : e.getStackTrace())
				{
					log.severe(">>" + k.toString());
				}
				mongoClient = null;
			}
			return mongoClient;
		}
	}

	/**
	 * @param mongoClient the mongoClient to set
	 */
	public void setMongoClient(MongoClient mongoClient)
	{
		this.mongoClient = mongoClient;
	}
	
	/**
	 * @return the db
	 */
	public DB getDb()
	{
		return db;
	}

	/**
	 * @param db the db to set
	 */
	public void setDb(DB db)
	{
		this.db = db;
	}

}
