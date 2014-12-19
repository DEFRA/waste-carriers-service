package uk.gov.ea.wastecarrier.services.tasks;

import static com.yammer.dropwizard.testing.JsonHelpers.asJson;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import net.vz.mongodb.jackson.DBCursor;
import net.vz.mongodb.jackson.JacksonDBCollection;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.ElasticSearchConfiguration;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.elasticsearch.ElasticSearchUtils;
import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;

import com.google.common.collect.ImmutableMultimap;
import com.mongodb.DB;
import com.yammer.dropwizard.tasks.Task;

/**
 * The Indexer class enables a reset/refresh of the data contained with the Elastic search records, it does this by
 * performing a entire purge of the registration data and recreating it from the mongoDB
 * 
 * E.g. curl -X POST http://localhost:9091/tasks/indexer 
 * Which performs a partial delete/recreate for entries currently
 * in the database
 * 
 * Also can use Optional parameters of -d 'all' 
 * E.g. curl -X POST http://localhost:9091/tasks/indexer -d 'all' 
 * Which performs a full system wipe and recreate
 * 
 * @author Steve
 * 
 */
public class Indexer extends Task
{
	private final DatabaseHelper databaseHelper;

	private final ElasticSearchConfiguration elasticSearch;
	
	/**
	 * The ElasticSearch Client (TransportClient to connect to the cluster; shared, singleton)
	 */
	//private final Client esClient;
	
	private static Logger log = Logger.getLogger(Indexer.class.getName());

	public Indexer(String name, DatabaseConfiguration database, ElasticSearchConfiguration elasticSearch, Client esClient)
	{
		super(name);
		this.databaseHelper = new DatabaseHelper(database);
		this.elasticSearch = elasticSearch;
		//this.esClient = esClient;
	}

	/**
	 * Performs the ElasticSearch Indexing operation Used Via the administration ports to index all of the registrations
	 * found in the mongo database
	 * 
	 * Usage: 
	 * curl -X POST http://[SERVER]:[ADMINPORT]/tasks/[this.getName()] [OPTIONAL -d 'all']
	 * 
	 * E.g. curl -X POST http://localhost:9091/tasks/indexer -d 'all'
	 * 
	 * -d 'all' - Performs a delete all record operation, if not provided only the records currently 
	 * found will be updated.
	 * 
	 */
	@Override
	public void execute(ImmutableMultimap<String, String> arg0, PrintWriter out) throws Exception
	{
		out.append("Running Complete re-Indexing operation of Elastic Search records...\n");

		// Determine if Delete All is required
		boolean deleteAll = false;
		// Determine if re-index is required
		boolean reIndex = true;
		/*
		 * Use: curl -X POST http://localhost:9091/tasks/indexer -d 'all' to delete all records.
		 */
		for (String s : arg0.keys())
		{
			if (s.equals("all"))
			{
				deleteAll = true;
				log.info("Performing Delete All operation");
				out.append("Performing Delete All operation\n");
			}
			else if (s.equals("deleteAll"))
			{
				deleteAll = true;
				reIndex = false;
				log.info("Only performing Delete All operation");
				out.append("Only performing Delete All operation\n");
			}
		}

		// Get All Registration records from the database
		DB db = this.databaseHelper.getConnection();
		if (db != null)
		{
			if (!db.isAuthenticated())
			{
				throw new RuntimeException("Error: Could not authenticate user");
			}

			TransportClient newClient = ElasticSearchUtils.getNewTransportClient(elasticSearch);
			// If requested, Delete all Registration indexes
			if (deleteAll)
			{
				DeleteIndexResponse delete = newClient.admin().indices()
						.delete(new DeleteIndexRequest(Registration.COLLECTION_NAME)).actionGet();
				if (!delete.isAcknowledged())
				{
					log.severe("Index wasn't deleted");
					out.append("Error: Index wasn't deleted\n");
				}
			}

			// Create MONGOJACK connection to the database
			JacksonDBCollection<Registration, String> registrations = JacksonDBCollection.wrap(
					db.getCollection(Registration.COLLECTION_NAME), Registration.class, String.class);

			// Attempt to retrieve all registrations
			DBCursor<Registration> dbcur = registrations.find();
			for (Registration r : dbcur)
			{
				// Update records only if not doing all records
				if (!deleteAll)
				{
					deleteElasticSearchIndex(elasticSearch, r);
					out.append("deleted reg: " + r.getId() + "\n");
				}
				// Update records if reIndex is true
				if (reIndex) 
				{
					BulkResponse bulkResponse = createElasticSearchIndex(newClient, r);
					if (bulkResponse.hasFailures())
					{
						// process failures by iterating through each bulk response item
						log.severe(bulkResponse.buildFailureMessage());
						throw new RuntimeException("Error: Could not create index in ElasticSearch: "
								+ bulkResponse.buildFailureMessage());
					}
					else
					{
						log.info("createdIndex for: " + r.getId());
					}
					out.append("indexed new: " + r.getId() + "\n");
				}
			}

			// Supposed to do this after records re-added
			if (deleteAll && reIndex)
			{
				//Note: As of version 0.90.5, flushing should be performed separately from refreshing. 
				//See https://github.com/elasticsearch/elasticsearch/issues/3689
				log.info("Delete and Re-Index: Flushing the ElasticSearch registrations index.");
				newClient.admin().indices().flush(new FlushRequest(Registration.COLLECTION_NAME)).actionGet();
				log.info("Flushed the index. Now refreshing the index.");
				newClient.admin().indices().refresh(new RefreshRequest(Registration.COLLECTION_NAME)).actionGet();
				log.info("The index has been refreshed.");
			}
			
			log.info("Closing the ElasticSearch Client after use.");
			newClient.close();
		} else {
			//No database connection...
			log.severe("No MongoDB database connection available - could not index!");
		}
		out.append("Done\n");
	}

	/**
	 * Performs a create index operation on the Elastic Search records for the provided registration
	 * 
	 * @param client, the ElasticSearch Client to connect with
	 * @param reg, the Registration object to add to the records
	 * @return a BulkResponse object, which contains the response from the server. This is returned directly 
	 * as to enable different behavior to each calling client
	 */
	public static BulkResponse createElasticSearchIndex(Client client, Registration reg)
	{
		log.info("Entering createElasticSearchIndex() - preparing bulk request. Registration ID = " + reg.getId());
		BulkRequestBuilder bulkRequest = client.prepareBulk();
		
		// either use client#prepare, or use Requests# to directly build index/delete requests
		try
		{
			log.info("Adding a prepareIndex request.");
			bulkRequest.add(client.prepareIndex(Registration.COLLECTION_NAME, Registration.COLLECTION_SINGULAR_NAME, reg.getId()).setSource(
					asJson(reg)));
		}
		catch (IOException e1)
		{
			log.severe("Caught IOException while adding to bulk request: " + e1.getMessage());
			e1.printStackTrace();
			log.severe("Error in creating reg from object: " + e1.getMessage());
		}

		log.info("Executing the bulk request.");
		BulkResponse bulkResponse = bulkRequest.execute().actionGet();
		log.info("Returning the bulk response.");
		return bulkResponse;
	}
	
	/**
	 * Index (i.e. insert or update) the registration into ElasticSearch,
	 * using a fresh new ElasticSearch Client (TransportClient)
	 * @param esConfig the ElasticSearchConfiguration
	 * @param reg the Registration
	 */
	public static void indexRegistration(ElasticSearchConfiguration esConfig, Registration reg) {
		TransportClient newClient = null;
		try {
			log.info("Creating new ElasticSearch TransportClient for indexing.");
			newClient = ElasticSearchUtils.getNewTransportClient(esConfig);
			indexRegistration(esConfig, newClient, reg);
		} catch (ElasticsearchException e) {
			log.severe("Encountered ElasticSearch Exception while indexing: " + e.getDetailedMessage());
		}
		finally {
			log.info("Closing the ElasticSearch Client after use.");
			newClient.close();
		}
	}


	/**
	 * Index (i.e. insert) the registration into ElasticSearch, using the given ElasticSearch Client (TransportClient)
	 * @param esConfig the ElasticSearch Configuration
	 * @param client the ElasticSearch Client (TransportClient)
	 * @param reg the Registration
	 */
	private static void indexRegistration(ElasticSearchConfiguration esConfig, Client client, Registration reg) {
		log.info("Entering indexRegistration: Registration id = " + reg.getId());
		
		IndexResponse indexResponse = null;
		try {
			indexResponse = client.prepareIndex(Registration.COLLECTION_NAME, Registration.COLLECTION_SINGULAR_NAME, reg.getId())
					.setSource(asJson(reg)).execute().actionGet();
			log.info("indexResponse: id = " + indexResponse.getId());
			log.info("indexResponse: version = " + indexResponse.getVersion());
		} catch (ElasticsearchException e) {
			log.severe("Encountered ElasticSearch exception while indexing registration: " + e.getDetailedMessage());
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			log.severe("Encountered exception while indexing registration: " + e.getMessage());
			e.printStackTrace();
		}
		log.info("Index request completed: " + indexResponse);
	}

	
	/**
	 * Performs a delete index operation on the Elastic Search records for the provided registration
	 * @param reg
	 */
	public static DeleteResponse deleteElasticSearchIndex(ElasticSearchConfiguration esConfig, Registration reg)
	{
		TransportClient client = null;
		DeleteResponse deleteResponse = null;
		try {
			client = ElasticSearchUtils.getNewTransportClient(esConfig);
			// Delete Index after creation
			deleteResponse = client
					.prepareDelete(Registration.COLLECTION_NAME, Registration.COLLECTION_SINGULAR_NAME, reg.getId()).setOperationThreaded(false)
					.execute().actionGet();
			log.info("deleted: " + deleteResponse.getId());
		} catch (ElasticsearchException e) {
			log.severe("Encountered Exception while deleting from ElasticSearch: " + e.getDetailedMessage());
		} finally {
			log.info("Closing ElasticSearchClient after use");
			client.close();
		}
		
		return deleteResponse;
	}
}
