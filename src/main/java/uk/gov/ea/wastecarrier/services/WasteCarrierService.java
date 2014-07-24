package uk.gov.ea.wastecarrier.services;

import java.util.logging.Logger;

import org.elasticsearch.client.Client;

import uk.gov.ea.wastecarrier.services.elasticsearch.ElasticSearchManaged;
import uk.gov.ea.wastecarrier.services.elasticsearch.ElasticSearchUtils;
import uk.gov.ea.wastecarrier.services.health.ElasticSearchHealthCheck;
import uk.gov.ea.wastecarrier.services.health.MongoHealthCheck;
import uk.gov.ea.wastecarrier.services.health.TemplateHealthCheck;
import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;
import uk.gov.ea.wastecarrier.services.mongoDb.MongoManaged;
import uk.gov.ea.wastecarrier.services.resources.IndividualConvictionCheckResource;
import uk.gov.ea.wastecarrier.services.resources.OrderResource;
import uk.gov.ea.wastecarrier.services.resources.NewPaymentResource;
import uk.gov.ea.wastecarrier.services.resources.OrdersResource;
import uk.gov.ea.wastecarrier.services.resources.OrganisationConvictionCheckResource;
import uk.gov.ea.wastecarrier.services.resources.PaymentResource;
import uk.gov.ea.wastecarrier.services.resources.RegistrationReadEditResource;
import uk.gov.ea.wastecarrier.services.resources.RegistrationVersionResource;
import uk.gov.ea.wastecarrier.services.resources.RegistrationsResource;
import uk.gov.ea.wastecarrier.services.resources.SettingsResource;
import uk.gov.ea.wastecarrier.services.tasks.Indexer;
import uk.gov.ea.wastecarrier.services.tasks.LocationPopulator;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;


/**
 * The Waste Carrier Service class provides the RESTful services available for completing operations 
 * to the waste carrier registration service. Operations in Create registration, Edit registration, 
 * various get and search operations, and cancel registration.
 *
 */
public class WasteCarrierService extends Service<WasteCarrierConfiguration> {
	
	private MongoClient mongoClient;
	
	//The client used to talk to ElasticSearch
	private Client esClient;
	
	// Standard logging declaration
	private Logger log = Logger.getLogger(WasteCarrierService.class.getName());
	
    public static void main(String[] args) throws Exception {
        new WasteCarrierService().run(args);
    }

    @Override
    public void initialize(Bootstrap<WasteCarrierConfiguration> bootstrap) {
        bootstrap.setName("wastecarrier-services");
    }

    @Override
    public void run(WasteCarrierConfiguration configuration,
                    Environment environment) {
    	final String template = configuration.getTemplate();
        final String defaultName = configuration.getDefaultName();
        final MessageQueueConfiguration mQConfig = configuration.getMessageQueueConfiguration();
        final DatabaseConfiguration dbConfig = configuration.getDatabase();
        final ElasticSearchConfiguration esConfig = configuration.getElasticSearch();
        final String postcodeFilePath = configuration.getPostcodeFilePath();
        
        //Create a singleton instance of the ElasticSearch TransportClient. Client to be closed on shutdown.
        esClient = ElasticSearchUtils.getNewTransportClient(esConfig);

        // Add Create Resource
        environment.addResource(new RegistrationsResource(template, defaultName, mQConfig, dbConfig, esConfig, esClient, postcodeFilePath));
        // Add Read Resource
        environment.addResource(new RegistrationReadEditResource(template, defaultName, mQConfig, dbConfig, esConfig, esClient));
        // Add Version Resource
        environment.addResource(new RegistrationVersionResource());
        
        // Add Payment Resource, testing new URL for get payment details
        environment.addResource(new NewPaymentResource());
        environment.addResource(new PaymentResource(dbConfig));
        // Add Order Resource
        environment.addResource(new OrderResource(dbConfig));
        environment.addResource(new OrdersResource(dbConfig));
        
        // Add Settings resource
        environment.addResource(new SettingsResource(configuration.getSettings()));
        
        //Add convictions resource
        environment.addResource(new IndividualConvictionCheckResource(esConfig));
        environment.addResource(new OrganisationConvictionCheckResource(esConfig));
        
        /**
         * Note: using environment.addProvider(new RegistrationCreateResource(template, defaultName, mQConfig));
         * Seems to perform a similar feature to addResources, need to research the difference?
         */
        
        // Add Service Health checks
        environment.addHealthCheck(new TemplateHealthCheck(template));
        
        // Add Database Heath checks
        DatabaseHelper dbHelper = new DatabaseHelper(dbConfig);
        mongoClient = dbHelper.getMongoClient();
        
        // TEST authentication
        DB db = mongoClient.getDB( dbConfig.getName());
        char[] pword = dbConfig.getPassword().toCharArray(); 
        try
        {
        	boolean auth = db.authenticate( dbConfig.getUsername(), pword);
        	log.info("Is Authenticated: " + auth);
        }
        catch (MongoException e)
        {
        	log.severe("Could not connect to Database: " + e.getMessage() + ", continuing to startup.");
        }
        environment.addHealthCheck(new MongoHealthCheck(mongoClient));
        
        // Add Database management features
        MongoManaged mongoManaged = new MongoManaged(mongoClient);
        environment.manage(mongoManaged);
        
        ElasticSearchManaged esManaged = new ElasticSearchManaged(esClient);
        environment.manage(esManaged);
        
        // Add Indexing functionality to clean Elastic Search Indexes and perform re-index of all data
        Indexer task = new Indexer("indexer", dbConfig, esConfig, esClient);
		environment.addTask(task);
		
		// Add Location Population functionality to create location indexes for all provided addresses of all data
        LocationPopulator locationPop = new LocationPopulator("location", dbConfig, postcodeFilePath);
		environment.addTask(locationPop);
		
		// Add Heath Check to indexing Service
		environment.addHealthCheck(
				new ElasticSearchHealthCheck(ElasticSearchUtils.getNewTransportClient(esConfig)));
        
        // Get and Print the Jar Version to the console for logging purposes
        Package objPackage = this.getClass().getPackage();
        if (objPackage.getImplementationTitle() != null)
        {
        	// Only print name and version if running as a Jar, otherwise these functions will not work
	        String name = objPackage.getImplementationTitle();
	        String version = objPackage.getImplementationVersion();
	        log.info("\n\nRunning: " + name + " service\nVersion: " + version + "\n");
        }
        
        /*
         * Don't know if this is needed, but seemed reasonable
         */
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() { 
            	/*
				 * my shutdown code here
				 */
            	if (mongoClient != null)
            	{ 
            		mongoClient.close();
            	}
            	if (esClient != null)
            	{
            		esClient.close();
            	}
            }
         });
        
    }

}