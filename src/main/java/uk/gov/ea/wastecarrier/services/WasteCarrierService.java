package uk.gov.ea.wastecarrier.services;

import ch.qos.logback.classic.LoggerContext;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.anthavio.airbrake.AirbrakeLogbackAppender;
import uk.gov.ea.wastecarrier.services.backgroundJobs.*;
import uk.gov.ea.wastecarrier.services.cli.IRImporter;
import uk.gov.ea.wastecarrier.services.dao.EntityDao;
import uk.gov.ea.wastecarrier.services.dao.RegistrationDao;
import uk.gov.ea.wastecarrier.services.dao.UserDao;
import uk.gov.ea.wastecarrier.services.health.MongoHealthCheck;
import uk.gov.ea.wastecarrier.services.resources.*;
import uk.gov.ea.wastecarrier.services.tasks.*;

import java.io.PrintWriter;
import java.util.logging.Logger;

/**
 * The Waste Carrier Service class provides the RESTful services available for completing operations
 * to the waste carrier registration service. Operations in Create registration, Edit registration,
 * various get and search operations, and cancel registration.
 *
 */
public class WasteCarrierService extends Application<WasteCarrierConfiguration> {

    // Standard logging declaration.
    private Logger log = Logger.getLogger(WasteCarrierService.class.getName());
    
    // Logback appender to allow integration with Airbrake.
    private AirbrakeLogbackAppender airbrakeAppender;

    public static void main(String[] args) throws Exception {
        new WasteCarrierService().run(args);
    }

    @Override
    public String getName() {
        return "waste-carriers-service";
    }

    @Override
    public void initialize(Bootstrap<WasteCarrierConfiguration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );

        // Add a command to import IR registrations.
        // This can only be performed when the server is NOT running.
        bootstrap.addCommand(new IRImporter());
    }

    @Override
    public void run(WasteCarrierConfiguration configuration, Environment environment) {

        final DatabaseConfiguration registrationsConfig = configuration.getDatabase();
        final DatabaseConfiguration usersConfig = configuration.getUserDatabase();
        final DatabaseConfiguration entityMatchingConfig = configuration.getEntityMatchingDatabase();
        final String postcodeFilePath = configuration.getPostcodeFilePath();

        checkConnections(registrationsConfig, usersConfig, entityMatchingConfig);

        addAirbrake(configuration.getAirbrakeLogbackConfiguration());

        addHealthChecks(environment, registrationsConfig, usersConfig, entityMatchingConfig);

        addResources(
                environment,
                registrationsConfig,
                usersConfig,
                entityMatchingConfig,
                postcodeFilePath,
                configuration.getSettings()
        );

        addTasks(
                environment,
                registrationsConfig,
                entityMatchingConfig,
                configuration.getEntityMatching().entitiesFilePath,
                configuration.getIrRenewals(),
                postcodeFilePath
        );

        addBackgroundJobs(
                environment,
                registrationsConfig,
                configuration.getExportJobConfiguration(),
                configuration.getRegistrationStatusJobConfiguration()
        );

        logPackageNameAndVersion();
    }

    private void checkConnections(
            DatabaseConfiguration registrationsConfig,
            DatabaseConfiguration usersConfig,
            DatabaseConfiguration entityMatchingConfig
    ) {
        // An error will be thrown if the service cannot connect to all 3
        // databases. If that is the case no point continuing with anything
        // hence we call it first and don't handle any errors it throws
        new RegistrationDao(registrationsConfig).checkConnection();
        new UserDao(usersConfig).checkConnection();
        new EntityDao(entityMatchingConfig).checkConnection();
    }

    private void addAirbrake(AirbrakeLogbackConfiguration config) {

        if (config == null) return;

        // Make sure we only initialise the Airbrake integration once.
        if (airbrakeAppender != null) {
            log.warning("Airbrake log appender already initialised; not initialising again");
            return;
        }

        // Get Logback logging context.
        LoggerContext loggerContext = (LoggerContext)org.slf4j.LoggerFactory.getILoggerFactory();
        if (loggerContext == null) {
            log.warning("Cannot obtain Logback LoggerContext; Airbrake integration will be unavailable.");
            return;
        }

        // Get root logger.
        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        if (rootLogger == null) {
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

    private void addHealthChecks(
            Environment environment,
            DatabaseConfiguration registrationsConfig,
            DatabaseConfiguration usersConfig,
            DatabaseConfiguration entityMatchingConfig
    ) {
        environment.healthChecks().register("RegistrationsHealthCheck", new MongoHealthCheck(registrationsConfig));
        environment.healthChecks().register("UsersHealthCheck", new MongoHealthCheck(usersConfig));
        environment.healthChecks().register("EntityMatchingHealthCheck", new MongoHealthCheck(entityMatchingConfig));
    }

    private void addResources(
            Environment environment,
            DatabaseConfiguration registrationsConfig,
            DatabaseConfiguration usersConfig,
            DatabaseConfiguration entityMatchingConfig,
            String postcodeFilePath,
            SettingsConfiguration settings
    ) {

        // Add Create Resource.
        environment.jersey().register(new RegistrationsResource(registrationsConfig, postcodeFilePath));
        // Add Read Resource.
        environment.jersey().register(new RegistrationReadEditResource(registrationsConfig, usersConfig, settings));
        // Add Version Resource.
        environment.jersey().register(new RegistrationVersionResource());

        // Add Payment Resource, testing new URL for get payment details.
        environment.jersey().register(new NewPaymentResource());
        environment.jersey().register(new PaymentResource(registrationsConfig, usersConfig, settings));

        // Add Order Resource.
        environment.jersey().register(new OrderResource(registrationsConfig));
        environment.jersey().register(new OrdersResource(registrationsConfig, usersConfig, settings));

        // Add Settings resource.
        environment.jersey().register(new SettingsResource(settings));

        // Add search resource.
        environment.jersey().register(new SearchResource(registrationsConfig, settings.getSearchResultCount()));

        // Add IR Renewals resource.
        environment.jersey().register(new IRRenewalResource(registrationsConfig));

    }

    private void addBackgroundJobs(
            Environment environment,
            DatabaseConfiguration registrationsConfig,
            ExportJobConfiguration exportConfig,
            RegistrationStatusJobConfiguration statusJobConfiguration
    ) {
        // Add managed component and tasks for Background Scheduled Jobs.
        BackgroundJobScheduler dailyJobScheduler = BackgroundJobScheduler.getInstance();
        dailyJobScheduler.setDatabaseConfiguration(registrationsConfig);
        dailyJobScheduler.setExportJobConfiguration(exportConfig);
        dailyJobScheduler.setRegistrationStatusJobConfiguration(statusJobConfiguration);
        environment.lifecycle().manage(dailyJobScheduler);
    }

    private void addTasks(
            Environment environment,
            DatabaseConfiguration registrationsConfig,
            DatabaseConfiguration entityMatchingConfig,
            String entityFilePath,
            IRConfiguration irConfig,
            String postcodeFilePath
    ) {
        // These link to the background jobs and allow us to execute them manually via the admin port
        environment.admin().addTask(new BackgroundJobMetricsReporter("get-jobMetrics"));
        environment.admin().addTask(new ExportJobStarter("start-exportJob"));
        environment.admin().addTask(new RegistrationStatusJobStarter("start-registrationStatusJob"));

        // Allow us to test exception handling, particularly Airbrake / Errbit integration
        environment.admin().addTask(new ExceptionTesterTask("generateTestException"));

        // Add Location Population functionality to create location indexes for all provided addresses of all data.
        environment.admin().addTask(new LocationPopulatorTask("location", registrationsConfig, postcodeFilePath));

        // Add tasks related to IR data.
        environment.admin().addTask(new IRRenewalPopulatorTask("ir-repopulate", registrationsConfig, irConfig));

        // Add task to re-populate entity matching when called
        environment.admin().addTask(new EntityPopulatorTask("entity-populator", entityMatchingConfig, entityFilePath));

        //Add a task to ensure that indexes have been defined in the database.
        EnsureDatabaseIndexesTask ensureDbIndexesTask = new EnsureDatabaseIndexesTask(
                "EnsureDatabaseIndexes",
                new RegistrationDao(registrationsConfig)
        );
        environment.admin().addTask(ensureDbIndexesTask);

        try {
            ensureDbIndexesTask.execute(null, new PrintWriter(System.out));
        } catch (Exception e) {
            log.severe("Could not ensure indexes at startup: " + e.getMessage());
        }
    }

    /**
     * Get and print the Jar Version to the console for logging purposes.
     */
    private void logPackageNameAndVersion() {


        Package objPackage = this.getClass().getPackage();
        if (objPackage.getImplementationTitle() != null) {
            // Only print name and version if running as a Jar, otherwise these functions will not work.
            String name = objPackage.getImplementationTitle();
            String version = objPackage.getImplementationVersion();
            log.info("\n\nRunning: " + name + " service\nVersion: " + version + "\n");
        }
    }
}
