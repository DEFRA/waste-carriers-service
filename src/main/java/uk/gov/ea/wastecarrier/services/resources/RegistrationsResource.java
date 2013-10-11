package uk.gov.ea.wastecarrier.services.resources;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.MessageQueueConfiguration;
import uk.gov.ea.wastecarrier.services.core.MetaData;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;

import com.google.common.base.Optional;
import com.mongodb.DB;
import com.yammer.metrics.annotation.Timed;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import net.vz.mongodb.jackson.DBCursor;
import net.vz.mongodb.jackson.DBQuery;
import net.vz.mongodb.jackson.DBQuery.Query;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.WriteResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This class represents the registration details URL (defined at @Path) and associated operations for POSTing
 * registrations, and GETting the entire registration information, as well as specific search results
 * 
 */
@Path("/registrations.json")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RegistrationsResource
{

	private final String template;
	private final String defaultName;
	private final MessageQueueConfiguration messageQueue;
	private final DatabaseHelper databaseHelper;

	// Standard logging declaration
	private Logger log = Logger.getLogger(RegistrationsResource.class.getName());

	/**
	 * 
	 * @param template
	 * @param defaultName
	 * @param mQConfig
	 * @param database
	 */
	public RegistrationsResource(String template, String defaultName, MessageQueueConfiguration mQConfig,
			DatabaseConfiguration database)
	{
		this.template = template;
		this.defaultName = defaultName;
		this.messageQueue = mQConfig;
		log.fine("> template: " + this.template);
		log.fine("> defaultName: " + this.defaultName);
		log.fine("> messageQueue: " + this.messageQueue);
		
		this.databaseHelper = new DatabaseHelper(database);
	}

	/**
	 * Gets a list of registrations. If a YYY is provided then a list limited by YYY is returned, otherwise the entire
	 * registration details are returned.
	 * 
	 * @param name @QueryParam("name") Optional<String> name takes the URL parameter out of the URL i.e.
	 *            test/page?name=joey
	 * @return a list of Registration objects as JSON, the list is derived from the search criteria, if no matches are
	 * 		   found, and empty list is returned.
	 * @throws WebApplicationException SERVICE_UNAVAILABLE - If the database is not available
	 */
	@GET
	@Timed
	public List<Registration> getRegistrations(@QueryParam("companyName") Optional<String> name,
			@QueryParam("businessType") Optional<String> businessType, 
			@QueryParam("postcode") Optional<String> postcode)
	{
		log.fine("Get Method Detected at /registrations");
		ArrayList<Registration> returnlist = new ArrayList<Registration>();

		DB db = databaseHelper.getConnection();
		if (db != null)
		{
			// Database available
			if (name.isPresent() || businessType.isPresent() || postcode.isPresent())
			{
				log.info("Param GET Method Detected - Return List of Registrations limited by Search criteria");
				
				// Determine which/what combination of parameters have been provided
				Map <String, Optional<String>> myMap= new HashMap<String, Optional<String>>();
				myMap.put("companyName", name);
				myMap.put("businessType", businessType);
				myMap.put("postcode", postcode);
				
				Query[] queryList = createConditionalSearchParams(myMap);
				log.info("Number of search parameters provided: " + queryList.length);

				// Total multiple search criteria, such that if multiple parameters are provided,
				// they are combined in an AND operation
				Query totalQuery = DBQuery.and(queryList);

				// Create MONGOJACK connection to the database
				JacksonDBCollection<Registration, String> registrations = JacksonDBCollection.wrap(
						db.getCollection(Registration.COLLECTION_NAME), Registration.class, String.class);

				DBCursor<Registration> dbcur = registrations.find(totalQuery);
				log.info("Found: " + dbcur.size() + " Matching criteria");

				for (Registration r : dbcur)
				{
					log.fine("> search found registration id: " + r.getId());
					returnlist.add(r);
				}
			}
			else
			{
				log.info("Empty GET Method Detected - Return List of ALL Registrations (limited by max)");

				// Create MONGOJACK connection to the database
				JacksonDBCollection<Registration, String> registrations = JacksonDBCollection.wrap(
						db.getCollection(Registration.COLLECTION_NAME), Registration.class, String.class);

				// Attempt to retrieve all registrations
				DBCursor<Registration> dbcur = registrations.find();
				for (Registration r : dbcur)
				{
					log.fine("> search found registrations id: " + r.getId());
					returnlist.add(r);
				}

			}
		}
		else
		{
			throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
		}

		if (returnlist.size() == 0)
		{
			// TODO: Any Special handling if an empty result is found, currently return an empty list
		}
		return returnlist;
	}

	/**
	 * Creates an array of Query objects that represent the parameters passed in the URL, 
	 * ignoring any that are not present
	 * 
	 * @param paramMap contains the various URL parameters that have been passed into this request
	 * @return a Query[] list if available, otherwise returns an empty list
	 */
	private Query[] createConditionalSearchParams(Map<String, Optional<String>> paramMap)
	{
		log.fine("Create conditional search paramters: " + paramMap.keySet().size() );
		ArrayList<Query> newQueryList = new ArrayList<Query>();
		
		// Check if parameter exists and if so create a Database Query
		for (String keyName : paramMap.keySet())
		{
			Optional<String> param = paramMap.get(keyName);
			if (param.isPresent())
			{
				log.info( "param name: " + keyName + " value: " + param.get());
				Query paramQuery = DBQuery.is(keyName, param.get());
				newQueryList.add(paramQuery);
			}
		}
		
		// Convert the ArrayList to a Standard array[]
		Query[] queryList = new Query[newQueryList.size()];
		int counter = 0;
		for (Query q : newQueryList)
		{
			queryList[counter] = q;
			counter++;
		}		
		
		return queryList;
	}

	/**
	 * POSTs the full registration details to save registration details in the database
	 * 
	 * @param reg the Registration details to save in the database
	 * @return the updated Registration object after it was saved in the database
	 * @throws WebApplicationException SERVICE_UNAVAILABLE - If the database is not available
	 */
	@POST
	public Registration register(@Valid Registration reg)
	{
		log.info("POST METHOD detected in register() method for registration create.");
		Registration savedObject = null;

		DB db = databaseHelper.getConnection();
		if (db != null)
		{
			/*
			 * Insert registration details into the database
			 */
			// Update Registration MetaData to include current time
			reg.setMetaData(new MetaData(MetaData.getCurrentDateTime(), "userDetailAddedAtRegistration"));

			// Create MONGOJACK connection to the database
			JacksonDBCollection<Registration, String> registrations = JacksonDBCollection.wrap(
					db.getCollection(Registration.COLLECTION_NAME), Registration.class, String.class);

			// Insert registration information into database
			WriteResult<Registration, String> result = registrations.insert(reg);

			// Get unique ID out of response, and find updated record
			String id = result.getSavedId();
			savedObject = registrations.findOneById(id);

			log.info("Found savedObject: '" + savedObject.getId() );
			
			// Return saved object to user (returned as JSON)
			return savedObject;
		}
		else
		{
			throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
		}
	}

}
