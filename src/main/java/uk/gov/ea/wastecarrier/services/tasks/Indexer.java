package uk.gov.ea.wastecarrier.services.tasks;

import static com.yammer.dropwizard.testing.JsonHelpers.asJson;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import net.vz.mongodb.jackson.DBCursor;
import net.vz.mongodb.jackson.JacksonDBCollection;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.ElasticSearchConfiguration;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;

import com.google.common.collect.ImmutableMultimap;
import com.mongodb.DB;
import com.yammer.dropwizard.tasks.Task;

/**
 * The Indexer class enables a reset/refresh of the data contained with the Elastic search records, it does this by
 * performing a entire purge of the registration data and recreating it from the mongoDB
 * 
 * E.g. curl -X POST http://localhost:9091/tasks/reindex 
 * Which performs a partial delete/recreate for entries currently
 * in the database
 * 
 * Also can use Optional parameters of -d 'all' 
 * E.g. curl -X POST http://localhost:9091/tasks/reindex -d 'all' 
 * Which performs a full system wipe and recreate
 * 
 * @author Steve
 * 
 */
public class Indexer extends Task
{
	private final DatabaseHelper databaseHelper;
	private final ElasticSearchConfiguration elasticSearch;
	private Client esClient;
	private static Logger log = Logger.getLogger(Indexer.class.getName());

	public Indexer(String name, DatabaseConfiguration database, ElasticSearchConfiguration elasticSearch)
	{
		super(name);
		this.databaseHelper = new DatabaseHelper(database);
		this.elasticSearch = elasticSearch;
	}

	@Override
	public void execute(ImmutableMultimap<String, String> arg0, PrintWriter out) throws Exception
	{
		out.append("Running Complete re-Indexing operation of Elastic Search records...\n");

		// Determine if Delete All is required
		boolean deleteAll = false;
		/*
		 * Use: curl -X POST http://localhost:9091/tasks/reindex -d 'all' to delete all records.
		 */
		for (String s : arg0.keys())
		{
			if (s.equals("all"))
			{
				deleteAll = true;
				log.info("Performing Delete All operation");
				out.append("Performing Delete All operation\n");
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

			// Create Elastic Search Connection
			esClient = new TransportClient().addTransportAddress(new InetSocketTransportAddress(this.elasticSearch
					.getHost(), this.elasticSearch.getPort()));

			// If requested, Delete all Registration indexes
			if (deleteAll)
			{
				DeleteIndexResponse delete = esClient.admin().indices()
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
					deleteElasticSearchIndex(r);
					out.append("deleted reg: " + r.getId() + "\n");
				}
				BulkResponse bulkResponse = createElasticSearchIndex(esClient, r);
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

			// Supposed to do this after records re-added
			if (deleteAll)
			{
				esClient.admin().indices().flush(new FlushRequest(Registration.COLLECTION_NAME).refresh(true))
						.actionGet();
			}
		}
		out.append("Done\n");
	}

	public static BulkResponse createElasticSearchIndex(Client client, Registration reg)
	{
		BulkRequestBuilder bulkRequest = client.prepareBulk();

		// either use client#prepare, or use Requests# to directly build index/delete requests
		try
		{
			bulkRequest.add(client.prepareIndex(Registration.COLLECTION_NAME, "registration", reg.getId()).setSource(
					asJson(reg)));
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
			log.severe("Error in creating reg from object: " + e1.getMessage());
		}

		BulkResponse bulkResponse = bulkRequest.execute().actionGet();
		return bulkResponse;
	}

	private void deleteElasticSearchIndex(Registration reg)
	{
		// Delete Index after creation
		DeleteResponse deleteResponse = esClient
				.prepareDelete(Registration.COLLECTION_NAME, "registration", reg.getId()).setOperationThreaded(false)
				.execute().actionGet();

		log.info("deleted: " + deleteResponse.getId());
	}
}
