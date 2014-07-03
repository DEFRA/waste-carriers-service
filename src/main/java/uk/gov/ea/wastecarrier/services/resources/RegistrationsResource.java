package uk.gov.ea.wastecarrier.services.resources;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.ElasticSearchConfiguration;
import uk.gov.ea.wastecarrier.services.MessageQueueConfiguration;
import uk.gov.ea.wastecarrier.services.core.Location;
import uk.gov.ea.wastecarrier.services.core.MetaData;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.elasticsearch.ElasticSearchUtils;
import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;
import uk.gov.ea.wastecarrier.services.mongoDb.ReportingHelper;
import uk.gov.ea.wastecarrier.services.tasks.Indexer;
import uk.gov.ea.wastecarrier.services.tasks.PostcodeRegistry;

import com.google.common.base.Optional;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
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
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.GeoDistanceFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermFilterBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.mongojack.DBSort;

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
import java.util.Set;
import java.util.logging.Logger;

/**
 * This class represents the registration details URL (defined at @Path) and associated operations for POSTing
 * registrations, and GETting the entire registration information, as well as specific search results
 * 
 */
@Path("/"+Registration.COLLECTION_NAME+".json")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RegistrationsResource
{

	private final String template;
	private final String defaultName;
	private final MessageQueueConfiguration messageQueue;
	private final DatabaseHelper databaseHelper;
	private final ElasticSearchConfiguration elasticSearch;
	/* Note: We are not using the shared ES TransportClient for the time being,
	 * due to stale connections becoming stale and NoNodeAvailableExceptions being thrown as a result.
	 * Always using fresh new clients instead - although the ElasticSearch documentation suggests otherwise. 
	 */
	//private final Client esClient;
	private final PostcodeRegistry postcodeRegistry;

	// Standard logging declaration
	private Logger log = Logger.getLogger(RegistrationsResource.class.getName());
	
	/**
	 * 
	 * @param template
	 * @param defaultName
	 * @param mQConfig
	 * @param database
	 */
	public RegistrationsResource(
			String template, 
			String defaultName, 
			MessageQueueConfiguration mQConfig,
			DatabaseConfiguration database, 
			ElasticSearchConfiguration elasticSearch, 
			Client esClient, 
			String postcodeFilePath)
	{
		this.template = template;
		this.defaultName = defaultName;
		this.messageQueue = mQConfig;
		log.fine("> template: " + this.template);
		log.fine("> defaultName: " + this.defaultName);
		log.fine("> messageQueue: " + this.messageQueue);
		
		this.databaseHelper = new DatabaseHelper(database);
		this.elasticSearch = elasticSearch;
		this.postcodeRegistry = new PostcodeRegistry(PostcodeRegistry.POSTCODE_FROM.FILE, postcodeFilePath);
		
		//Note: We are not re-using the joint singleton ES Client due to timeout issues
		//this.esClient = esClient;
		esClient = null;
	}
	
	protected void finalize() throws Throwable {
		//esClient.close();
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
			@QueryParam("distance") Optional<String> distance, 
			@QueryParam("postcode") Optional<String> postcode, 
			@QueryParam("q") Optional<String> q,
			@QueryParam("searchWithin") Optional<String> sw, 
			@QueryParam("ac") Optional<String> account,
			@QueryParam("activeOnly") Optional<Boolean> activeOnly, 
			@QueryParam("excludeRegId") Optional<Boolean> excludeRegId,
			@QueryParam("from") Optional<String> from,
			@QueryParam("until") Optional<String> until,
			@QueryParam("route[]") Set<String> route,
			@QueryParam("status[]") Set<String> status,
			@QueryParam("businessType[]") Set<String> businessType)
	{
		log.fine("Get Method Detected at /registrations");
		ArrayList<Registration> returnlist = new ArrayList<Registration>();
		
		//TODO Re-factor, restructure and simplify this method...
		
		// TODO This was quickly added in to support reporting. Also needs to be re-factored out
		try {
			if (from.isPresent() && until.isPresent()) {
				ReportingHelper helper = new ReportingHelper(this.databaseHelper);
				helper.fromDate = from.get();
				helper.toDate = until.get();
				helper.route = route;
				helper.status = status;
				helper.businessType = businessType;
				
				List<Registration> reportResults = helper.getRegistrations();
				
				if (reportResults.size() == 0) {
					log.info("No results found - returning empty list");
				}
				return reportResults;
			}
		} catch (MongoException e) {
			log.severe("Database not found, check the database is running");
			throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
		}
		
		if (q.isPresent())
		{
			String qValue = q.get();
			if (!"".equals(qValue))
			{
				log.info("Param GET Method Detected - Return List of Registrations limited by ElasticSearch");
				log.info("The 'q' search parameter is present: searching for: " + qValue);
				
				boolean useAdvancedSearch = false;
				String swValue = null;
				if (sw.isPresent() && !"".equals(sw) && !"any".equals(sw.get()))
				{
					swValue = sw.get();
					useAdvancedSearch = true;
					log.info("Advanced Search, requested within only: " + swValue);
				}
				
				// Create a filter to only show ACTIVE records
				BoolFilterBuilder fbBoolFilter = null;
				GeoDistanceSortBuilder gsb = null;
				boolean useDistanceFilter = false;
				boolean excludeId = false;
				if (activeOnly.isPresent())
				{
					GeoDistanceFilterBuilder geoFilter = null;
					if (postcode.isPresent() && postcode.get() != ""  
							&& distance.isPresent() && distance.get() != "")
					{
						log.info("Filtered Search, postcode: " + postcode.get());
						Double[] xyCoords = postcodeRegistry.getXYCoords(postcode.get());

						if (!distance.get().equalsIgnoreCase("any"))
						{
							log.info("Using GEO FILTER search for X: " + xyCoords[0] + " Y: " + xyCoords[1]);
							// Geo Filter Search
							geoFilter = FilterBuilders.geoDistanceFilter("registration.location")
								.point(xyCoords[0], xyCoords[1])
								.distance(Double.valueOf(distance.get()), DistanceUnit.MILES)
								.optimizeBbox("memory")                    // Can be also "indexed" or "none"
								.geoDistance(GeoDistance.ARC);             // Or GeoDistance.PLANE
							
							// Add a GeoDistance sort, to enable the specific distance for each site to be returned.
							gsb = new GeoDistanceSortBuilder ("location");
							gsb.point(xyCoords[0], xyCoords[1]);
							gsb.order(SortOrder.ASC);
							gsb.unit(DistanceUnit.MILES);
							
							useDistanceFilter = true;
						}
					}
					
					boolean pValue = activeOnly.get();
					if (pValue)
					{
						TermFilterBuilder fbTermFilter = FilterBuilders.termFilter("metaData.status", "active");
						fbBoolFilter = FilterBuilders.boolFilter().must(fbTermFilter);
						if (useDistanceFilter)
						{
							fbBoolFilter = FilterBuilders.boolFilter().must(fbTermFilter).must(geoFilter);
						}
						log.info("Filtered Search, showing only active registrations: " + pValue);
					}
				}
				
				/**
				 * Search
				 * 
				 * Search has been designed to be a three tiered approach to optimize certain 
				 * business and search criteria, and has been broken down into the following rules:
				 * 
				 * 1. Exact Match to RegIdentifier
				 * 2. Exact match to any other value
				 * 3. Fuzzy match to certain fields, such as company name, postcode, first and last names
				 * 
				 * Each of these queries will run in turn and if any tier finds any number of matches, 
				 * those matches are returned.
				 * 
				 * For example if a RegIdentifier is specified and an exact match is found, only that one
				 * result is returned.
				 * If However a match is not found the search continues onto the next tier, i.e. Exact match 
				 * to any other value, and so on.
				 * 
				 */
				
				TransportClient esClient = ElasticSearchUtils.getNewTransportClient(elasticSearch);
				// First Priority - Exact Match to RegIdentifier
				QueryBuilder qb0 = QueryBuilders.matchQuery("regIdentifier", qValue);
				SearchRequestBuilder srb0 = esClient.prepareSearch(Registration.COLLECTION_NAME)
						.setTypes(Registration.COLLECTION_SINGULAR_NAME)
						.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
						.setQuery(qb0)
						.setSize(1);
				if (fbBoolFilter != null) {
					srb0.setFilter(fbBoolFilter);
				}
				
				// Second Priority - Exact match to any other value
				QueryBuilder qb1 = QueryBuilders.queryString(qValue);
				// Advanced/Alternate Second Priority - Exact Match, but limited to only within criteria specified
				if (useAdvancedSearch)
				{
					// Limit Search to Just within the specified limit
					qb1 = QueryBuilders.matchQuery(swValue, qValue);
				}
				SearchRequestBuilder srb1;
				if (gsb != null)
				{
					log.fine("Using GEO sort 1");
					srb1 = esClient.prepareSearch(Registration.COLLECTION_NAME)
							.setTypes(Registration.COLLECTION_SINGULAR_NAME)
							.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
							.setQuery(qb1)
							.setSize(this.elasticSearch.getSize())
							.addSort(gsb);
				} else {
					srb1 = esClient.prepareSearch(Registration.COLLECTION_NAME)
							.setTypes(Registration.COLLECTION_SINGULAR_NAME)
							.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
							.setQuery(qb1)
							.setSize(this.elasticSearch.getSize())
							.addSort("_score", SortOrder.DESC)
							.addSort("companyName", SortOrder.ASC);
				}
				if (fbBoolFilter != null) srb1.setFilter(fbBoolFilter);
				
				// Third Priority - Fuzzy match to certain fields
				//QueryBuilder qb2 = QueryBuilders.fuzzyQuery("companyName", qValue);	// Works as a fuzzy search but only on 1 field
				QueryBuilder qb2 = QueryBuilders.fuzzyLikeThisQuery("companyName", "postcode", "firstName", "lastName")
						.likeText(qValue)
						.maxQueryTerms(12);                             // Max num of Terms in generated queries
				// Advanced/Alternate Third Priority - Fuzzy Match, but limited to only within criteria specified
				if (useAdvancedSearch)
				{
					// Limit Search to Just within the specified limit
					qb2 = QueryBuilders.fuzzyQuery(swValue, qValue);	// Works as a fuzzy search but only on 1 field
				}
				SearchRequestBuilder srb2;
				if (gsb != null)
				{
					log.fine("Using GEO sort 2");
					srb2 = esClient.prepareSearch(Registration.COLLECTION_NAME)
							.setTypes(Registration.COLLECTION_SINGULAR_NAME)
							.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
							.setQuery(qb2)
							.setSize(this.elasticSearch.getSize())
							.addSort(gsb);
				} else {
					srb2 = esClient.prepareSearch(Registration.COLLECTION_NAME)
							.setTypes(Registration.COLLECTION_SINGULAR_NAME)
							.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
							.setQuery(qb2)
							.setSize(this.elasticSearch.getSize())
							.addSort("companyName", SortOrder.ASC);
				}
				if (fbBoolFilter != null) {
					srb2.setFilter(fbBoolFilter);
				}

				MultiSearchResponse sr = null;
				try
				{
					if (excludeRegId.isPresent() && excludeRegId.get().booleanValue())
					{
						sr = esClient.prepareMultiSearch().add(srb1).add(srb2).execute().actionGet();
					}
					else
					{
						sr = esClient.prepareMultiSearch().add(srb0).add(srb1).add(srb2).execute().actionGet();
					}
				}
				catch (NoNodeAvailableException e)
				{
					log.severe("ElasticSearch not available, please check the status of the service. Exception: " + e.getDetailedMessage());
					log.severe("Closing ElasticSearch Client");
					esClient.close();
					log.severe("Throwing WebApplicationException - Service unavailable");
					throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
				}
				
				long totalHits = 0;
				int count = 0;
				String matchType = "";
				for (MultiSearchResponse.Item item : sr.getResponses()) 
				{
					SearchResponse response = item.getResponse();
					if (response != null)
					{
						Iterator<SearchHit> hit_it = response.getHits().iterator();
						while(hit_it.hasNext()){
							SearchHit hit = hit_it.next();
							ObjectMapper mapper = new ObjectMapper();
							log.info(hit.getSourceAsString());
							Registration r;
							try {
								r = mapper.readValue(hit.getSourceAsString(), Registration.class);
								for (Object obj : hit.getSortValues()) {
									if (useDistanceFilter)
									{
										int milesToSite = Double.valueOf(obj.toString()).intValue();
										log.fine("Distance to Registration: " + milesToSite );
										MetaData rMeta = r.getMetaData();
										rMeta.setDistance(milesToSite + "");
										r.setMetaData(rMeta);
									}
								}
							} catch (JsonParseException e) {
								throw new RuntimeException(e);
							} catch (JsonMappingException e) {
								throw new RuntimeException(e);
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
							returnlist.add(r);
						}
						totalHits += response.getHits().getTotalHits();
						if (totalHits > 0)
						{
							if (count == 0)
								matchType = "RegIdentifier ";
							else if (count == 1)
								matchType = "Value ";
							else if (count == 2)
								matchType = "Fuzzy ";
							break;
						}
						count++;
					}
					else
					{
						log.severe("Error: response object from ElasticSearch was null");
					}
				}
				log.info("Found " + totalHits + " matching " + matchType 
						+ "records in ElasticSearch, but returning up to: "+this.elasticSearch.getSize());
				log.info("Closing the ElasticSearch Client after use");
				esClient.close();
				log.info("Returning the list of found registrations.");
				return returnlist;
			} // qValue not empty
		}  // q.isPresent()

		try
		{
			DB db = databaseHelper.getConnection();
			if (db != null)
			{
				if (!db.isAuthenticated())
				{
					log.info("Database not authenticated, access forbidden");
					throw new WebApplicationException(Status.UNAUTHORIZED);
				}
				
				// Database available
				if (name.isPresent() || postcode.isPresent() || account.isPresent())
				{
					log.info("Param GET Method Detected - Return List of Registrations limited by Search criteria");
					
					// Determine which/what combination of parameters have been provided
					Map <String, Optional<String>> myMap= new HashMap<String, Optional<String>>();
					myMap.put("companyName", name);
					myMap.put("postcode", postcode);
					myMap.put("accountEmail", account);
					
					Query[] queryList = createConditionalSearchParams(myMap);
					log.info("Number of search parameters provided: " + queryList.length);
	
					// Total multiple search criteria, such that if multiple parameters are provided,
					// they are combined in an AND operation
					Query totalQuery = DBQuery.and(queryList);
	
					// Create MONGOJACK connection to the database
					JacksonDBCollection<Registration, String> registrations = JacksonDBCollection.wrap(
							db.getCollection(Registration.COLLECTION_NAME), Registration.class, String.class);
	
					DBCursor<Registration> dbcur = registrations.find(totalQuery).sort(DBSort.asc("companyName"));
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
					DBCursor<Registration> dbcur = registrations.find().sort(DBSort.asc("companyName"));
					for (Registration r : dbcur)
					{
						log.fine("> search found registrations id: " + r.getId());
						returnlist.add(r);
					}
	
				}
			}
			else
			{
				//Database connection is null - not available???
				log.severe("Database not available, check the database is running");
				throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
			}
		}
		catch (MongoException e)
		{
			log.severe("Database not found, check the database is running");
			throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
		}

		if (returnlist.size() == 0)
		{
			// TODO: Any Special handling if an empty result is found, currently return an empty list\
			log.info("No results found - returning empty list");
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
			
			// Update Registration Location to include location, derived from postcode
			if (reg.getPostcode() != null)
			{
				Double[] xyCoords = postcodeRegistry.getXYCoords(reg.getPostcode());
				reg.setLocation( new Location( xyCoords[0], xyCoords[1]));
			}
			else
			{
				log.warning("Non-UK Address assumed as Postcode could not be found in the registration, Using default location of X:1, Y:1");
				reg.setLocation( new Location(1, 1));
				
				// Update MetaData to include a message to state location information set to default
				MetaData tmpMD = reg.getMetaData();
				tmpMD.setAnotherString("Non-UK Address Assumed");
				reg.setMetaData(tmpMD);
			}
			
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
			
			/*
			 * Add same record to Elastic Search
			 */
			//TODO Is there an issue with BulkRequests in 0.90.5? - Trying plain index API instead
			//BulkResponse bulkResponse = Indexer.createElasticSearchIndex(esClient, savedObject);
			//if (bulkResponse.hasFailures()) {
			//    // process failures by iterating through each bulk response item
			//	log.severe("Failure while inserting registration into ElasticSearch: " + bulkResponse.buildFailureMessage());
			//	log.severe("The registration may not be in ElasticSearch - We may need to re-index. Registration id: " + savedObject.getRegIdentifier());
			//	// Note: We are not throw an exception here, in order for the user to be able to continue, even if the registration is not inserted into ElasticSearch.
			//	//throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
			//}
			//else
			//{
			//	log.info("Created Elastic Search Index for: " + savedObject.getId());
			//}
			log.info("About to index the new registration in ElasticSearch. ID = " + savedObject.getId());
			Indexer.indexRegistration(elasticSearch, savedObject);
			log.info("Returned from indexing.");
			// Return saved object to user (returned as JSON)
			return savedObject;
		}
		else
		{
			log.severe("Could not establish database connection to MongoDB! Check the database is running");
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
			reg.setRegIdentifier(getFormattedRegIdentifier(sequentialNumber, reg.getTier()));
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
	 * @param tier RegistrationTier enum for tier
	 * @return String of the formatted registration, prefixed with Registration.REGID_PREFIX
	 */
	private String getFormattedRegIdentifier(int sequentialNumber, Registration.RegistrationTier tier)
	{
        return Registration.REGID_PREFIX + tier.getPrefix() + Integer.toString(sequentialNumber);
	}

}
