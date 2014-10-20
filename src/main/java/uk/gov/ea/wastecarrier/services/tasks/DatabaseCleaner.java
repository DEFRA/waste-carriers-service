package uk.gov.ea.wastecarrier.services.tasks;

import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import net.vz.mongodb.jackson.DBCursor;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.WriteResult;
import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.ElasticSearchConfiguration;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;

import com.google.common.collect.ImmutableMultimap;
import com.mongodb.DB;
import com.yammer.dropwizard.tasks.Task;

/**
 * The DatabaseCleaner clears the registrations in the mongoDB database and ensure elastic search
 * also reflects this update
 * 
 * To use this service call, E.g. 
 * curl -X POST http://localhost:9091/tasks/dbcleaner 
 * Which performs a full wipe for entries currently in the database
 * 
 * @author Steve
 * 
 */
public class DatabaseCleaner extends Task
{
	private final DatabaseHelper databaseHelper;
	private final ElasticSearchConfiguration elasticSearch;
	private static Logger log = Logger.getLogger(DatabaseCleaner.class.getName());

	public DatabaseCleaner(String name, DatabaseConfiguration database, ElasticSearchConfiguration elasticSearch)
	{
		super(name);
		this.databaseHelper = new DatabaseHelper(database);
		this.elasticSearch = elasticSearch;
	}

	/**
	 * Performs the operation Used Via the administration ports to wipe all the registrations
	 * found in the mongo database
	 * 
	 * Usage: 
	 * curl -X POST http://[SERVER]:[ADMINPORT]/tasks/[this.getName()] 
	 */
	@Override
	public void execute(ImmutableMultimap<String, String> arg0, PrintWriter out) throws Exception
	{
		out.append("Running Complete wipe operation of DB and Elastic Search records...\n");
		
		// Get All Registration records from the database
		DB db = this.databaseHelper.getConnection();
		if (db != null)
		{
			if (!db.isAuthenticated())
			{
				throw new RuntimeException("Error: Could not authenticate user");
			}

			// Create MONGOJACK connection to the database
			JacksonDBCollection<Registration, String> registrations = JacksonDBCollection.wrap(
					db.getCollection(Registration.COLLECTION_NAME), Registration.class, String.class);
			
			// Go to database, get list of registrations
			DBCursor<Registration> dbcur = registrations.find();
			log.info("Found: " + dbcur.size() + " registrations to delete");
			
			// for each registration, get ID and delete
			for (Registration r : dbcur)
			{
				WriteResult<Registration, String> result = registrations.removeById(r.getId());
				
				if (String.valueOf("").equals(result.getError()))
				{
					throw new WebApplicationException(Status.NOT_MODIFIED);
				}
				else
				{
					log.fine("Deleted Registration id: " + r.getId() + " from DB");
					
					Indexer.deleteElasticSearchIndex(elasticSearch, r);
					
					log.info("Deleted reg: " + r.getId());
					out.append("Deleted reg: " + r.getId() + "\n");
				}
			}
		}
		
		out.append("Done\n");
	}
}
