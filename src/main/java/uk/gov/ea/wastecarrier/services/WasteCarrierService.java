package uk.gov.ea.wastecarrier.services;

import java.util.logging.Logger;

import uk.gov.ea.wastecarrier.services.health.MongoHealthCheck;
import uk.gov.ea.wastecarrier.services.health.TemplateHealthCheck;
import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;
import uk.gov.ea.wastecarrier.services.mongoDb.MongoManaged;
import uk.gov.ea.wastecarrier.services.resources.RegistrationReadEditResource;
import uk.gov.ea.wastecarrier.services.resources.RegistrationsResource;

import com.mongodb.MongoClient;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;


/**
 * The Waste Carrier Service class provides the RESTful services available for completing operations 
 * to the waste carrier registration service. Operations in Create registration, Edit registration, 
 * various get and search operations, and cancel registration.
 * 
 * @author Steve stevenr@aptosolutions.co.uk
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
        
        // Add Create Resource
        environment.addResource(new RegistrationsResource(template, defaultName, mQConfig, dbConfig));
        // Add Read Resource
        environment.addResource(new RegistrationReadEditResource(template, defaultName, mQConfig, dbConfig));
        
        /**
         * Note: using environment.addProvider(new RegistrationCreateResource(template, defaultName, mQConfig));
         * Seems to perform a similar feature to addResources, need to research the difference?
         */
        
        // Add Service Heath checks
        environment.addHealthCheck(new TemplateHealthCheck(template));
        
        // Add Database Heath checks
        DatabaseHelper dbHelper = new DatabaseHelper(dbConfig);
        mongoClient = dbHelper.getMongoClient();
        environment.addHealthCheck(new MongoHealthCheck(mongoClient));
        
        // Add Database management features
        MongoManaged mongoManaged = new MongoManaged(mongoClient);
        environment.manage(mongoManaged);
        
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