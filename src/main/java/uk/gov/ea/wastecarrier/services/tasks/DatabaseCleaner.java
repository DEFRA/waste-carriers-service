package uk.gov.ea.wastecarrier.services.tasks;

import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import io.dropwizard.servlets.tasks.Task;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;
import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.ElasticSearchConfiguration;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.elasticsearch.ElasticSearchUtils;
import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;

import com.google.common.collect.ImmutableMultimap;
import com.mongodb.DB;

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

	public DatabaseCleaner(String name, DatabaseConfiguration database, ElasticSearchConfiguration elasticSearch, 
			Client esClient)
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
			// Create MONGOJACK connection to the database
			JacksonDBCollection<Registration, String> registrations = JacksonDBCollection.wrap(
					db.getCollection(Registration.COLLECTION_NAME), Registration.class, String.class);
			
			// Go to database, get list of registrations
			DBCursor<Registration> dbcur = registrations.find();
			log.info("Found: " + dbcur.size() + " registrations to delete from DB");
			
			/**
			 * Checks and finds any registrations in ES and deletes them even if they are not in DB
			 * 
			 */
			TransportClient client = ElasticSearchUtils.getNewTransportClient(elasticSearch);
			
			// for each registration, get ID and delete
			for (Registration r : dbcur)
			{
				try {
					registrations.removeById(r.getId());
				} catch (Exception e) {
					throw new WebApplicationException(Status.NOT_MODIFIED);
				}

				log.fine("Deleted Registration id: " + r.getId() + " from DB");

				DeleteResponse deleteResponse = null;
				try {
					// Delete Index after creation
					deleteResponse = client
							.prepareDelete(Registration.COLLECTION_NAME, Registration.COLLECTION_SINGULAR_NAME, r.getId()).setOperationThreaded(false)
							.execute().actionGet();
					log.fine("deleted: " + deleteResponse.getId());
					log.info("Deleted reg: " + r.getId() + " from DB");
					out.append("Deleted reg: " + r.getId() + " from DB\n");
				} catch (ElasticsearchException e) {
					log.severe("Encountered Exception while deleting from ElasticSearch: " + e.getDetailedMessage());
				} finally {
					log.info("Closing ElasticSearchClient after use");
				}
			}
				
			// Create a Search by all registrations
			QueryBuilder qb = QueryBuilders.matchAllQuery();
			SearchResponse response = client.prepareSearch(Registration.COLLECTION_NAME)
			        .setTypes(Registration.COLLECTION_SINGULAR_NAME)
			        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
			        .setQuery(qb)
			        .setFrom(0).setSize(200).setExplain(true) // Limit to 200
			        .execute()
			        .actionGet();
			if (response.getHits().getTotalHits() > 0)
			{
				log.info("Found: " + response.getHits().getTotalHits() + " registrations to delete from ES");
				
				for (SearchHit sh : response.getHits())
				{
					log.fine("Id of ES registration to remove: " + sh.getId());
					// Remove Registration from Elastic search
					Registration tmpReg = new Registration();
					tmpReg.setId(sh.getId());
					DeleteResponse deleteResponse = null;
					try {
						// Delete Index after creation
						deleteResponse = client
								.prepareDelete(Registration.COLLECTION_NAME, Registration.COLLECTION_SINGULAR_NAME, sh.getId()).setOperationThreaded(false)
								.execute().actionGet();
						
						log.fine("deleted: " + deleteResponse.getId());
						log.info("Deleted reg: " + sh.getId() + " from ES");
						out.append("Deleted reg: " + sh.getId() + " from just ES\n");
					} catch (ElasticsearchException e) {
						log.severe("Encountered Exception while deleting from ElasticSearch: " + e.getDetailedMessage());
					} finally {
						log.info("Closing ElasticSearchClient after use");
						//client.close();
					}
				}
			}
			client.close();
		}
		
		out.append("Done\n");
	}
}
