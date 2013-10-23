package uk.gov.ea.wastecarrier.services.resources;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.ElasticSearchConfiguration;
import uk.gov.ea.wastecarrier.services.MessageQueueConfiguration;
import uk.gov.ea.wastecarrier.services.core.MetaData;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;

import com.google.common.base.Optional;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
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

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import net.vz.mongodb.jackson.DBCursor;
import net.vz.mongodb.jackson.DBQuery;
import net.vz.mongodb.jackson.DBQuery.Query;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.WriteResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
	private final ElasticSearchConfiguration elasticSearch;

	// Standard logging declaration
	private Logger log = Logger.getLogger(RegistrationsResource.class.getName());

	private Client esClient;
	
	/**
	 * 
	 * @param template
	 * @param defaultName
	 * @param mQConfig
	 * @param database
	 */
	public RegistrationsResource(String template, String defaultName, MessageQueueConfiguration mQConfig,
			DatabaseConfiguration database, ElasticSearchConfiguration elasticSearch)
	{
		this.template = template;
		this.defaultName = defaultName;
		this.messageQueue = mQConfig;
		log.fine("> template: " + this.template);
		log.fine("> defaultName: " + this.defaultName);
		log.fine("> messageQueue: " + this.messageQueue);
		
		this.databaseHelper = new DatabaseHelper(database);
		this.elasticSearch = elasticSearch;
		
		esClient = new TransportClient().addTransportAddress(new InetSocketTransportAddress(this.elasticSearch.getHost(), this.elasticSearch.getPort()));
	}
	
	protected void finalize() throws Throwable {
		esClient.close();
	};
	
	

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
			@QueryParam("postcode") Optional<String> postcode, @QueryParam("q") Optional<String> q)
	{
		log.fine("Get Method Detected at /registrations");
		ArrayList<Registration> returnlist = new ArrayList<Registration>();
		
		if(q.isPresent()){
			String qValue = q.get();
			if(!"".equals(qValue)){
				SearchResponse response = esClient.prepareSearch("registrations")
				        .setTypes("registration")
				        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				        .setQuery(QueryBuilders.queryString(qValue))             // Query
				        .execute()
				        .actionGet();
				
				Iterator<SearchHit> hit_it = response.getHits().iterator();
				while(hit_it.hasNext()){
					SearchHit hit = hit_it.next();
					ObjectMapper mapper = new ObjectMapper();
					System.out.println(hit.getSourceAsString());
					Registration r;
					try {
						r = mapper.readValue(hit.getSourceAsString(), Registration.class);
					} catch (JsonParseException e) {
						throw new RuntimeException(e);
					} catch (JsonMappingException e) {
						throw new RuntimeException(e);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					returnlist.add(r);
				}
				return returnlist;
			}
		}

		DB db = databaseHelper.getConnection();
		if (db != null)
		{
			if (!db.isAuthenticated())
			{
				throw new WebApplicationException(Status.FORBIDDEN);
			}
			
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
			if (!db.isAuthenticated())
			{
				throw new WebApplicationException(Status.FORBIDDEN);
			}
			
			/*
			 * Insert registration details into the database
			 */
			// Update Registration MetaData to include current time
			reg.setMetaData(new MetaData(MetaData.getCurrentDateTime(), "userDetailAddedAtRegistration"));
			
			// Update Registration to include sequential identifier
			updateRegistrationIdentifier(reg, db);

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

	/**
	 * Update Registration to include a unique sequential ID.
	 * Use the 'counters' collection and search for 'regid' and then increment the counter 'seq' by 1 every time
	 * 
	 * @param reg the current Registration document
	 * @param db the database connection to use
	 */
	private void updateRegistrationIdentifier(Registration reg, DB db)
	{
		DBCollection col = db.getCollection(Registration.COUNTERS_COLLECTION_NAME);
		DBObject query = DBQuery.is("_id", "regid");
		BasicDBObject incDocument = 
				new BasicDBObject().append("$inc", 
				new BasicDBObject().append("seq", 1));
		// Find the current latest sequence and update it
		DBObject dbObj = col.findAndModify(query, incDocument);
		if (dbObj == null)
		{
			// Try to Create/First Entry and then use if did not exist.
			BasicDBObject newDocument = 
                    new BasicDBObject().append("_id", "regid")
                                       .append("seq", 1);
			com.mongodb.WriteResult wr = col.insert(newDocument);
			if (wr.getError() != null)
			{
				// Counters collection cannot be found
				log.severe("Cannot create initial Counters table");
				throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
			}
			// Re-try find and modify
			dbObj = col.findAndModify(query, incDocument);
		}
		if (dbObj != null)
		{
			int sequentialNumber = (Integer) dbObj.get("seq");
			// Set the formatted identifier in the registration document
			reg.setRegIdentifier(getFormattedRegIdentifier(sequentialNumber));	
		}
		else
		{
			// Counters collection cannot be found
			log.severe("Cannot find Counters table, and or cannot update it");
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * Returns a formated unique string representing the registration identifier.
	 * NOTE: THis is NOT the ID for the registration
	 * @param sequentialNumber sequential integer of the registration counter
	 * @return String of the formatted registration, prefixed with Registration.REGID_PREFIX
	 */
	private String getFormattedRegIdentifier(int sequentialNumber)
	{
		String numberAsString = Integer.toString(sequentialNumber);
		while (numberAsString.length() < Registration.REGID_LENGTH)
		{
			numberAsString = "0" + numberAsString;
		}
		return Registration.REGID_PREFIX + numberAsString;
	}

}
