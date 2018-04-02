package uk.gov.ea.wastecarrier.services;

import java.io.PrintWriter;
import java.util.logging.Logger;

import org.elasticsearch.client.Client;

import uk.gov.ea.wastecarrier.services.cli.IRImporter;
import uk.gov.ea.wastecarrier.services.elasticsearch.ElasticSearchManaged;
import uk.gov.ea.wastecarrier.services.elasticsearch.ElasticSearchUtils;
import uk.gov.ea.wastecarrier.services.health.ElasticSearchHealthCheck;
import uk.gov.ea.wastecarrier.services.health.MongoHealthCheck;
import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;
import uk.gov.ea.wastecarrier.services.mongoDb.MongoManaged;
import uk.gov.ea.wastecarrier.services.resources.*;
import uk.gov.ea.wastecarrier.services.tasks.DatabaseCleaner;
import uk.gov.ea.wastecarrier.services.tasks.IRRenewalPopulator;
import uk.gov.ea.wastecarrier.services.mongoDb.RegistrationsMongoDao;
import uk.gov.ea.wastecarrier.services.tasks.EnsureDatabaseIndexesTask;
import uk.gov.ea.wastecarrier.services.tasks.Indexer;
import uk.gov.ea.wastecarrier.services.tasks.LocationPopulator;
import uk.gov.ea.wastecarrier.services.tasks.ExceptionTester;
import uk.gov.ea.wastecarrier.services.backgroundJobs.BackgroundJobScheduler;
import uk.gov.ea.wastecarrier.services.backgroundJobs.ExportJobStarter;
import uk.gov.ea.wastecarrier.services.backgroundJobs.RegistrationStatusJobStarter;
import uk.gov.ea.wastecarrier.services.backgroundJobs.BackgroundJobMetricsReporter;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

import ch.qos.logback.classic.LoggerContext;
import net.anthavio.airbrake.AirbrakeLogbackAppender;

/**
 * The Waste Carrier Service class provides the RESTful services available for completing operations
 * to the waste carrier registration service. Operations in Create registration, Edit registration,
 * various get and search operations, and cancel registration.
 *
 */
public class WasteCarrierService extends Service<WasteCarrierConfiguration>
{
    private MongoClient mongoClient;

    // The client used to talk to ElasticSearch.
    private Client esClient;

    // Standard logging declaration.
    private Logger log = Logger.getLogger(WasteCarrierService.class.getName());
    
    // Logback appender to allow integration with Airbrake.
    private AirbrakeLogbackAppender airbrakeAppender;

    public static void main(String[] args) throws Exception
    {
        new WasteCarrierService().run(args);
    }

    @Override
    public void initialize(Bootstrap<WasteCarrierConfiguration> bootstrap)
    {
        bootstrap.setName("wastecarrier-services");
        
        // Add a command to import IR registrations.
        // This can only be performed when the server is NOT running.
        bootstrap.addCommand(new IRImporter());
    }

    @Override
    public void run(WasteCarrierConfiguration configuration, Environment environment)
    {
        final DatabaseConfiguration dbConfig = configuration.getDatabase();
        final DatabaseConfiguration userDbConfig = configuration.getUserDatabase();
        final ElasticSearchConfiguration esConfig = configuration.getElasticSearch();
        final String postcodeFilePath = configuration.getPostcodeFilePath();
        final SettingsConfiguration sConfig = configuration.getSettings();
        
        // Initialise Airbrake integration.
        if (configuration.getAirbrakeLogbackConfiguration() != null)
        {
            initialiseAirbrakeIntegration(configuration.getAirbrakeLogbackConfiguration());
        }
        else
        {
            log.info("No Airbrake configuration found; skipping Airbrake integration.");
        }
        environment.addTask(new ExceptionTester("generateTestException"));

        // Create a singleton instance of the ElasticSearch TransportClient. Client to be closed on shutdown.
        esClient = ElasticSearchUtils.getNewTransportClient(esConfig);

        // Add Create Resource.
        environment.addResource(new RegistrationsResource(dbConfig, esConfig, esClient, postcodeFilePath));
        // Add Read Resource.
        environment.addResource(new RegistrationReadEditResource(dbConfig, userDbConfig, esConfig, esClient, sConfig));
        // Add Version Resource.
        environment.addResource(new RegistrationVersionResource());

        // Add Payment Resource, testing new URL for get payment details.
        environment.addResource(new NewPaymentResource());
        environment.addResource(new PaymentResource(dbConfig, userDbConfig, configuration.getSettings(), esConfig));
        
        // Add Order Resource.
        environment.addResource(new OrderResource(dbConfig));
        environment.addResource(new OrdersResource(dbConfig, userDbConfig, configuration.getSettings(), esConfig));

        // Add Settings resource.
        environment.addResource(new SettingsResource(sConfig));

        // Add query resource.
        environment.addResource(new QueryResource(dbConfig));

        // Add IR Renewals resource.
        environment.addResource(new IRRenewalResource(dbConfig));

        /**
         * Note: using environment.addProvider(new RegistrationCreateResource(template, defaultName, mQConfig));
         * Seems to perform a similar feature to addResources, need to research the difference?
         */

        // Add Database Heath checks.
        RegistrationsMongoDao dao = new RegistrationsMongoDao(dbConfig);

        DatabaseHelper dbHelper = new DatabaseHelper(dbConfig);
        mongoClient = dbHelper.getMongoClient();

        // Test authentication.
        DB db = mongoClient.getDB(dbConfig.getName());
        char[] pword = dbConfig.getPassword().toCharArray();
        try
        {
            boolean auth = db.authenticate(dbConfig.getUsername(), pword);
            log.info("Is Authenticated: " + auth);
        }
        catch (MongoException e)
        {
            log.severe("Could not connect to Database: " + e.getMessage() + ", continuing to startup.");
        }
        environment.addHealthCheck(new MongoHealthCheck(mongoClient));

        // Add Database management features.
        environment.manage(new MongoManaged(mongoClient));
        environment.manage(new ElasticSearchManaged(esClient));

        // Add Indexing functionality to clean Elastic Search Indexes and perform re-index of all data.
        environment.addTask(new Indexer("indexer", dbConfig, esConfig));

        // Add managed component and tasks for Background Scheduled Jobs.
        BackgroundJobScheduler dailyJobScheduler = BackgroundJobScheduler.getInstance();
        dailyJobScheduler.setDatabaseConfiguration(dbConfig);
        dailyJobScheduler.setExportJobConfiguration(configuration.getExportJobConfiguration());
        dailyJobScheduler.setRegistrationStatusJobConfiguration(configuration.getRegistrationStatusJobConfiguration());
        environment.manage(dailyJobScheduler);
        
        environment.addTask(new BackgroundJobMetricsReporter("get-jobMetrics"));
        environment.addTask(new ExportJobStarter("start-exportJob"));
        environment.addTask(new RegistrationStatusJobStarter("start-registrationStatusJob"));
        
        //Add a task to ensure that indexes have been defined in the database.
        EnsureDatabaseIndexesTask ensureDbIndexesTask = new EnsureDatabaseIndexesTask("EnsureDatabaseIndexes", dao);
        environment.addTask(ensureDbIndexesTask);

        try
        {
            ensureDbIndexesTask.execute(null, new PrintWriter(System.out));
        }
        catch (Exception e)
        {
            log.severe("Could not ensure indexes at startup: " + e.getMessage());
        }


        // Add Location Population functionality to create location indexes for all provided addresses of all data.
        environment.addTask(new LocationPopulator("location", dbConfig, postcodeFilePath));

        // Add tasks related to IR data.
        environment.addTask(new IRRenewalPopulator("ir-repopulate", dbConfig, configuration.getIrRenewals()));

        // Add Task for Database cleaner.
        environment.addTask(new DatabaseCleaner("dbcleaner", dbConfig, esConfig, esClient));

        // Add Heath Check to indexing Service.
        environment.addHealthCheck(new ElasticSearchHealthCheck(ElasticSearchUtils.getNewTransportClient(esConfig)));

        // Get and Print the Jar Version to the console for logging purposes.
        Package objPackage = this.getClass().getPackage();
        if (objPackage.getImplementationTitle() != null)
        {
            // Only print name and version if running as a Jar, otherwise these functions will not work.
            String name = objPackage.getImplementationTitle();
            String version = objPackage.getImplementationVersion();
            log.info("\n\nRunning: " + name + " service\nVersion: " + version + "\n");
        }

        // Last-ditch cleanup.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                // My shutdown code here.
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
    
    private void initialiseAirbrakeIntegration(AirbrakeLogbackConfiguration config)
    {
        // Make sure we only initialise the Airbrake integration once.
        if (airbrakeAppender != null)
        {
            log.warning("Airbrake log appender already initialised; not initialising again");
            return;
        }
        
        // Get Logback logging context.
        LoggerContext loggerContext = (LoggerContext)org.slf4j.LoggerFactory.getILoggerFactory();
        if (loggerContext == null)
        {
            log.warning("Cannot obtain Logback LoggerContext; Airbrake integration will be unavailable.");
            return;
        }
        
        // Get root logger.
        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        if (rootLogger == null)
        {
            log.warning("Cannot get root logger; Airbrake integration will be unavailable.");
            return;
        }
        
        // Create and configure the Airbrake appender.
        airbrakeAppender = new AirbrakeLogbackAppender();
        airbrakeAppender.setName("Airbrake");
        airbrakeAppender.setContext(loggerContext);  // This is a guess; is this necessary?
        airbrakeAppender.setUrl(config.getUrl());
        airbrakeAppender.setApiKey(config.getApiKey());
        airbrakeAppender.setEnv(config.getEnvironmentName());
        airbrakeAppender.setEnabled(config.getEnabled());
        airbrakeAppender.setNotify(config.getExceptionsOnly() ?
                net.anthavio.airbrake.AirbrakeLogbackAppender.Notify.EXCEPTIONS :
                net.anthavio.airbrake.AirbrakeLogbackAppender.Notify.ALL);
        
        // Add a filter so that we only send messages above a certain severity
        // level to Airbrake.
        ch.qos.logback.classic.filter.ThresholdFilter filter = new ch.qos.logback.classic.filter.ThresholdFilter();
        filter.setLevel(config.getThreshold());
        airbrakeAppender.addFilter(filter);
        filter.start();
        
        // Add the appender to the Logback config.
        rootLogger.addAppender(airbrakeAppender);
        airbrakeAppender.start();
        
        // Done.
        log.info("Added Airbrake appender to logging configuration.");
    }

}
