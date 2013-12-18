package uk.gov.ea.wastecarrier.services.tasks;

import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import net.vz.mongodb.jackson.DBCursor;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.WriteResult;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.Location;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;

import com.google.common.collect.ImmutableMultimap;
import com.mongodb.DB;
import com.yammer.dropwizard.tasks.Task;

/**
 * The Location Populator updates the records in the mongoDB database with XY Coordinates based on the 
 * postcode value that exists for that record. (If an invalid postcode has been provided, X:1, Y:1 are used)
 * 
 * To use this service call, E.g. 
 * curl -X POST http://localhost:9091/tasks/location 
 * Which performs a full location population for entries currently
 * in the database
 * 
 * @author Steve
 * 
 */
public class LocationPopulator extends Task
{
	private final DatabaseHelper databaseHelper;
	private static Logger log = Logger.getLogger(LocationPopulator.class.getName());
	private String pathToPostcodeFile;

	public LocationPopulator(String name, DatabaseConfiguration database, String pathToPostcodeFile)
	{
		super(name);
		this.databaseHelper = new DatabaseHelper(database);
		this.pathToPostcodeFile = pathToPostcodeFile;
	}

	/**
	 * Performs the  operation Used Via the administration ports to recreate the location coordinates
	 * all of the registrations found in the mongo database
	 * 
	 * Usage: 
	 * curl -X POST http://[SERVER]:[ADMINPORT]/tasks/[this.getName()] 
	 */
	@Override
	public void execute(ImmutableMultimap<String, String> arg0, PrintWriter out) throws Exception
	{
		out.append("Running Complete Location Population operation of Elastic Search records...\n");
		
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
			log.info("Found: " + dbcur.size() + " Matching criteria");
			
			PostcodeRegistry pr = new PostcodeRegistry(PostcodeRegistry.POSTCODE_FROM.FILE, pathToPostcodeFile);
			
			// for each registration, get out postcode
			for (Registration r : dbcur)
			{
				// Get XY Coordinates from postcode
				Double[] xyCoords = pr.getXYCoords(r.getPostcode());
				
				// Update location
				Location l = r.getLocation();
				l.setLat(xyCoords[0]);
				l.setLon(xyCoords[1]);
				r.setLocation(l);

				// Update database with XY information
				WriteResult<Registration, String> result = registrations.updateById(r.getId(), r);
				
				if (String.valueOf("").equals(result.getError()))
				{
					throw new WebApplicationException(Status.NOT_MODIFIED);
				}
				else
				{
					log.info("> Updated Registration id: " + r.getId());
				}
			}
		}
		
		out.append("Done\n");
	}
}
