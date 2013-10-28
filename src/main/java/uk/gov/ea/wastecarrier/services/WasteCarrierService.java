package uk.gov.ea.wastecarrier.services;

import java.util.logging.Logger;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import uk.gov.ea.wastecarrier.services.health.ElasticSearchHealthCheck;
import uk.gov.ea.wastecarrier.services.health.MongoHealthCheck;
import uk.gov.ea.wastecarrier.services.health.TemplateHealthCheck;
import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;
import uk.gov.ea.wastecarrier.services.mongoDb.MongoManaged;
import uk.gov.ea.wastecarrier.services.resources.RegistrationReadEditResource;
import uk.gov.ea.wastecarrier.services.resources.RegistrationsResource;
import uk.gov.ea.wastecarrier.services.tasks.Indexer;

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
        final ElasticSearchConfiguration eSConfig = configuration.getElasticSearch();
        
        // Add Create Resource
        environment.addResource(new RegistrationsResource(template, defaultName, mQConfig, dbConfig, eSConfig));
        // Add Read Resource
        environment.addResource(new RegistrationReadEditResource(template, defaultName, mQConfig, dbConfig, eSConfig));
        
        /**
         * Note: using environment.addProvider(new RegistrationCreateResource(template, defaultName, mQConfig));
         * Seems to perform a similar feature to addResources, need to research the difference?
         */
        
        // Add Service Heath checks
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
        
        // Add Indexing functionality to clean Elastic Search Indexes and perform re-index of all data
        Indexer task = new Indexer("indexer", dbConfig, eSConfig);
		environment.addTask(task);
		
		// Add Heath Check to indexing Service
		environment.addHealthCheck(
				new ElasticSearchHealthCheck(new TransportClient().addTransportAddress(
						new InetSocketTransportAddress(eSConfig.getHost(), eSConfig.getPort()))));
        
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
            	mongoClient.close();	
            }
         });
        
    }

}