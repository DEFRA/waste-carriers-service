package uk.gov.ea.wastecarrier.services.tasks;

import com.yammer.dropwizard.tasks.Task;
import static com.yammer.dropwizard.testing.JsonHelpers.asJson;

import com.google.common.collect.ImmutableMultimap;
import com.mongodb.DB;
import net.vz.mongodb.jackson.DBCursor;
import net.vz.mongodb.jackson.JacksonDBCollection;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.admin.indices.flush.FlushResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.ElasticsearchException;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.ElasticSearchConfiguration;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.elasticsearch.ElasticSearchUtils;
import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Provides a way to create an index in Elastic Search for registrations, and
 * re-index all registration data in Mongo in bulk.  Also provides a way to
 * index individual registrations when they are changed via the web interface.
 *
 * Example usage:
 * curl -X POST http://localhost:9091/tasks/indexer -d '@bin/registration_mapping.json'
 * where the JSON file (and preceding '-d' parameter) are optional, but allow
 * the object mapping in ElasticSearch to be set manually.  If this option is
 * not used then ElasticSearch's dynamic mapping will be used.
 */
public class Indexer extends Task
{
    // Configuration set at start-up.
    private final DatabaseHelper databaseHelper;
    private final ElasticSearchConfiguration elasticSearch;
    
    // Standard logger.
    private static Logger log = Logger.getLogger(Indexer.class.getName());

    // Constructor.  Initialises the task by storing the configuration it
    // needs when the task is run.
    public Indexer(String name, DatabaseConfiguration database, ElasticSearchConfiguration elasticSearch)
    {
        super(name);
        this.databaseHelper = new DatabaseHelper(database);
        this.elasticSearch = elasticSearch;
    }

    /**
     * Utility method to write a message to the log, and output the same message
     * to a PrintWriter.
     * @param logLevel The level to log the message at (severe, info, etc).
     * @param out An object allowing formatted messages to be returned to the
     * REST call originator.
     * @param message The string to log and output.
     */
    private void outputAndLogMessage(Level logLevel, PrintWriter out, String message)
    {
        log.log(logLevel, message);
        out.println(message);
    }
    
    /**
     * Drops and re-creates the Registrations index in Elastic Search, and
     * re-indexes all registrations in the Mongo database.
     *
     * Example usage:
     * curl -X POST http://localhost:9091/tasks/indexer -d '@bin/registration_mapping.json'
     */
    @Override
    public void execute(ImmutableMultimap<String, String> arg0, PrintWriter out) throws Exception
    {
        outputAndLogMessage(Level.INFO, out, "Dropping and rebuilding the Registrations index in Elastic Search");

        // Process task parameters.
        String indexMapping = null;
        for (String parameter : arg0.keys())
        {
            if (parameter.startsWith("{"))
            {
                outputAndLogMessage(Level.INFO, out, "Using string supplied by REST caller for index mapping");
                indexMapping = parameter;
            }
        }
        
        // Create a connection to Elastic Search, and make sure we dispose of it.
        TransportClient esClient = ElasticSearchUtils.getNewTransportClient(elasticSearch);
        try
        {
            boolean noErrors = true;
            
            // Check we can connect to the Mongo Database.
            DB db = this.databaseHelper.getConnection();
            if (noErrors && (db == null))
            {
                noErrors = false;
                outputAndLogMessage(Level.WARNING, out, "Error: No database connection available; aborting.");
            }
            if (noErrors && !db.isAuthenticated())
            {
                noErrors = false;
                outputAndLogMessage(Level.WARNING, out, "Error: Could not authenticate against database; aborting.");
            }
            
            // Start by completely deleting the old index.
            if (noErrors && !deleteElasticSearchRegistrationsIndex(esClient, out))
            {
                noErrors = false;
                outputAndLogMessage(Level.WARNING, out, "Aborting");
            }

            // Create the new index.
            if (noErrors && !createElasticSearchRegistrationsIndex(esClient, out, indexMapping))
            {
                noErrors = false;
                outputAndLogMessage(Level.WARNING, out, "Aborting");
            }

            // Create MONGOJACK connection to the database
            JacksonDBCollection<Registration, String> registrations = JacksonDBCollection.wrap(
                    db.getCollection(Registration.COLLECTION_NAME), Registration.class, String.class);

            // Index all registrations.
            if (noErrors)
            {
                int successCount = 0, failCount = 0;
                DBCursor<Registration> dbcur = registrations.find().snapshot();
                for (Registration registration : dbcur)
                {
                    try {
                        esClient.prepareIndex(Registration.COLLECTION_NAME, Registration.COLLECTION_SINGULAR_NAME, registration.getId())
                                .setSource(asJson(registration))
                                .execute()
                                .actionGet();
                        successCount++;
                    }
                    catch (IOException ioEx)
                    {
                        failCount++;
                        outputAndLogMessage(Level.WARNING, out, String.format("Failed to index registration %s: %s",
                                registration.getRegIdentifier(), ioEx.getMessage()));
                    }
                }
                
                outputAndLogMessage(Level.INFO, out, String.format("Successfully indexed %d registrations", successCount));
                if (failCount > 0)
                {
                    outputAndLogMessage(Level.WARNING, out, String.format("Warning: %d registrations could not be indexed", failCount));
                }
            }

            // Finish up.
            flushAndRefreshElasticSearch(esClient, out);
            outputAndLogMessage(Level.INFO, out, "Indexer finishing cleanly");
        }
        finally
        {
            log.info("Closing the ElasticSearch Client after use.");
            esClient.close();
        }
    }

    /**
     * Deletes any existing "Registrations" index in Elastic Search.
     * @param esClient A client connection to Elastic Search.
     * @param out An object used to send text output back to the REST caller.
     * @return TRUE if successful; otherwise FALSE.
     */
    private boolean deleteElasticSearchRegistrationsIndex(Client esClient, PrintWriter out)
    {
        boolean success = true;
        
        IndicesExistsResponse ieResponse = esClient.admin().indices().prepareExists(Registration.COLLECTION_NAME).get();
        if (ieResponse.isExists())
        {        
            DeleteIndexRequest request = new DeleteIndexRequest(Registration.COLLECTION_NAME);
            DeleteIndexResponse reponse = esClient.admin().indices().delete(request).actionGet();

            if (!reponse.isAcknowledged())
            {
                success = false;
                outputAndLogMessage(Level.SEVERE, out, "Error: failed to delete the registrations index in ElasticSearch");
            }
        }
        
        return success;
    }
    
    /**
     * Creates a new "Registrations" index in Elastic Search.
     * @param esClient A client connection to Elastic Search.
     * @param out An object used to send text output back to the REST caller.
     * @return TRUE if successful; otherwise FALSE.
     */
    private boolean createElasticSearchRegistrationsIndex(Client esClient, PrintWriter out, String mapping)
    {
        boolean success = true;
        CreateIndexResponse response;
        
        if ((mapping != null) && !mapping.trim().isEmpty())
        {
            outputAndLogMessage(Level.INFO, out, "Attempting to create new Registrations index with the provided mapping");
            CreateIndexRequestBuilder request = esClient.admin().indices()
                    .prepareCreate(Registration.COLLECTION_NAME)
                    .addMapping(Registration.COLLECTION_SINGULAR_NAME, mapping);
            response = request.execute().actionGet();
        }
        else
        {
            outputAndLogMessage(Level.INFO, out, "Attempting to create new Registrations index with dynamic mapping");
            CreateIndexRequest request = new CreateIndexRequest(Registration.COLLECTION_NAME);
            response = esClient.admin().indices().create(request).actionGet();
        }
        
        if (!response.isAcknowledged())
        {
            success = false;
            outputAndLogMessage(Level.SEVERE, out, "Error: failed to create the Registrations index in ElasticSearch");
        }
        
        return success;
    }
    
    /**
     * Flushes and Refreshes the "Registrations" index in Elastic Search.
     * @param esClient A client connection to Elastic Search.
     * @param out An object used to send text output back to the REST caller.
     */
    private void flushAndRefreshElasticSearch(Client esClient, PrintWriter out)
    {
        // Flush.
        log.info("Flushing the Registrations index in ElasticSearch");
        FlushRequest flushRequest = new FlushRequest(Registration.COLLECTION_NAME);
        FlushResponse flushResult = esClient.admin().indices().flush(flushRequest).actionGet();
        if (flushResult.getFailedShards() > 0)
        {
            outputAndLogMessage(Level.WARNING, out, "Flush index operation failed on one or more shards");
        }
        else
        {
            outputAndLogMessage(Level.INFO, out, "Flush index was successful");
        }
        
        // Refresh.
        log.info("Refreshing the Registrations index in ElasticSearch");
        RefreshRequest refreshRequest = new RefreshRequest(Registration.COLLECTION_NAME);
        RefreshResponse refreshResult = esClient.admin().indices().refresh(refreshRequest).actionGet();
        if (refreshResult.getFailedShards() > 0)
        {
            outputAndLogMessage(Level.WARNING, out, "Refresh index operation failed on one or more shards");
        }
        else
        {
            outputAndLogMessage(Level.INFO, out, "Refresh index was successful");
        }
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
