package uk.gov.ea.wastecarrier.services.backgroundJobs;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobKey;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.*;
import java.util.logging.Logger;
import java.text.SimpleDateFormat;

import com.mongodb.DB;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.DBCursor;

import au.com.bytecode.opencsv.CSVWriter;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.Address;
import uk.gov.ea.wastecarrier.services.core.MetaData;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;

/**
 * Quartz job which exports data from the Waste Carriers database to flat files
 * which are then used to populate the electronic Public Register (ePR) and
 * Reporting systems.  This job also updates Registration status (ACTIVE,
 * EXPIRED etc).  This job is normally run once daily.
 */
@DisallowConcurrentExecution
public class ExportJob implements Job
{
    // Public 'constants' used in the JobDataMap, which passes configuration
    // to this job.
    public static final String DATABASE_HOST = "database_host";
    public static final String DATABASE_PORT = "database_port";
    public static final String DATABASE_NAME = "database_name";
    public static final String DATABASE_USERNAME = "database_username";
    public static final String DATABASE_PASSWORD = "database_password";

    public static final String EPR_EXPORT_FILE = "epr_export_file";
    public static final String EPR_DATE_FORMAT = "epr_date_format";

    public static final String REPORTING_EXPORT_PATH = "reporting_export_path";
    public static final String REPORTING_DATE_FORMAT = "reporting_date_format";

    // Constants used in string fields of the Registration object.
    private static final String COMPANY = "limitedCompany";
    
    // Private static members.
    private final static Logger log = Logger.getLogger(ExportJob.class.getName());

    // Private instance members, initialised inside execute().
    private SimpleDateFormat eprDateFormatter;
    private SimpleDateFormat reportingDateFormatter;
    
    // Private members referencing files we'll write to.
    private CSVWriter eprWriter = null;

    /**
     * Public empty constructor, for Quartz.
     */
    public ExportJob()
    {
        // Nothing to do; "initialisation" is done inside execute().
    }

    /**
     * Quartz entry point.  Executes this job.
     * @param context Job configuration passed in by Quartz.
     * @throws JobExecutionException 
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        try
        {
            // Record the start of the job execution.
            JobKey jobKey = context.getJobDetail().getKey();
            log.info(String.format("Starting execution of the Export job, with key %s", jobKey.toString()));
            
            // Log job configuration for debugging purposes.
            JobDataMap jobConfig = context.getJobDetail().getJobDataMap();
            log.fine(String.format("--> Will attempt to use database %s on %s:%d",
                jobConfig.getString(DATABASE_NAME),
                jobConfig.getString(DATABASE_HOST),
                jobConfig.getInt(DATABASE_PORT)
            ));
            log.fine(String.format("--> The EPR export file is %s", jobConfig.getString(EPR_EXPORT_FILE)));
            log.fine(String.format("--> The EPR date format is %s", jobConfig.getString(EPR_DATE_FORMAT)));
            log.fine(String.format("--> The Reporting export path is %s", jobConfig.getString(REPORTING_EXPORT_PATH)));
            log.fine(String.format("--> The Reporting date format is %s", jobConfig.getString(REPORTING_DATE_FORMAT)));
            
            // Initialise date formatters from provided configuration.
            eprDateFormatter = new SimpleDateFormat(jobConfig.getString(EPR_DATE_FORMAT));
            reportingDateFormatter = new SimpleDateFormat(jobConfig.getString(REPORTING_DATE_FORMAT));
            
            // Build a database helper using the provided configuration.
            DatabaseHelper dbHelper = new DatabaseHelper(new DatabaseConfiguration(
                jobConfig.getString(DATABASE_HOST),
                jobConfig.getInt(DATABASE_PORT),
                jobConfig.getString(DATABASE_NAME),
                jobConfig.getString(DATABASE_USERNAME),
                jobConfig.getString(DATABASE_PASSWORD)
            ));
            
            // Check we can connect to the database, and are authenticated.
            DB db = dbHelper.getConnection();
            if (db == null)
            {
                throw new RuntimeException("Error: No database connection available; aborting.");
            }
            if (!db.isAuthenticated())
            {
                throw new RuntimeException("Error: Could not authenticate against database; aborting.");
            }
            
            // Do useful work...
            processAllRegistrations(db, jobConfig);
        }
        catch (Exception e)
        {
            // Quartz only allows us to throw a JobExecutionException from this
            // method, so we wrap all other exceptions.
            throw new JobExecutionException(e);
        }
    }
    
    /**
     * Prepends the Reporting Snapshot export path to the specified filename.
     * @param jobConfig The Quartz JobDataMap object provided to this job.
     * @param filename The filename (without any path) to use.
     * @return A fully-qualified file path.
     */
    private String getReportingSnapshotFileWithPath(JobDataMap jobConfig, String filename)
    {
        return String.format("%s%c%s", jobConfig.getString(REPORTING_EXPORT_PATH), File.separatorChar, filename);
    }

    /**
     * Iterates over all registrations in the database, processing each in turn.
     * If appropriate, the registration details will be exported to one or more
     * of the export files.  The status of the registration (active, expired etc.)
     * will also be updated depending upon the current date.
     * @param database A Mongo database object providing access to the
     * registration data.
     * @param jobConfig Configuration options for the execution of this job.
     * @throws Exception 
     */
    private void processAllRegistrations(DB database, JobDataMap jobConfig) throws Exception
    {
        log.info("Beginning processing all Registration records");
        
        // Decide where to export the results.
        String eprExportFile = jobConfig.getString(EPR_EXPORT_FILE);
        String reportingSnapshotFile = getReportingSnapshotFileWithPath(jobConfig, "wc_registrations_snapshot.csv");

        // Create MONGOJACK connection to the database.
        JacksonDBCollection<Registration, String> registrations = JacksonDBCollection.wrap(
            database.getCollection(Registration.COLLECTION_NAME), Registration.class, String.class);
            
        // Make sure we don't leak resources, even on an exception.
        try
        {
            // Initialise the EPR export.
            eprWriter = new CSVWriter(new FileWriter(eprExportFile));
            eprWriter.writeNext(getEprColumnTitles());

            // Initialise the Reporting Snapshot exports.
            // TODO: complete this.

            // Process all registrations in the database.
            DBCursor<Registration> dbcur = registrations.find();
            for (Registration r : dbcur)
            {
                exportRegistrationForEpr(r);
            }
        }
        finally
        {
            if (eprWriter != null)
            {
                eprWriter.close();
            }
        }
        
        log.info("Finished processing all Registration records");
    }
    
    /**
     * Returns an array of column titles for the electronic Public Register export.
     * @return An array of strings, in which each string is the title of a column.
     */
    private String[] getEprColumnTitles()
    {
        return new String[] {
            "Registration number",
            "Organisation name",
            "UPRN",
            "Building",
            "Address line 1",
            "Address line 2",
            "Address line 3",
            "Address line 4",
            "Town",
            "Postcode",
            "Country",
            "Easting",
            "Northing",
            "Applicant type",
            "Registration tier",
            "Registration type",
            "Registration date",
            "Expiry date",
            "Company number"
        };
    }
    
    /**
     * Exports details of a registration to a CSV file in the format used for
     * the electronic Public Register.  Any exceptions are handled within the
     * function.
     * @param reg The registration record to export.
     */
    private void exportRegistrationForEpr(Registration reg)
    {
        try
        {
            MetaData metaData = reg.getMetaData();
            Address registeredAddress = reg.getFirstAddressByType(Address.addressType.REGISTERED);

            if ((metaData == null) || (registeredAddress == null))
            {
                log.warning(String.format("Cannot export %s to ePR because Metadata or Registered Address is missing", reg.getRegIdentifier()));
            }
            else if (reg.goesOnPublicRegister())
            {
                boolean isUpper = (reg.getTier() == Registration.RegistrationTier.UPPER);
                eprWriter.writeNext(new String[] {
                    reg.getRegIdentifier(),     // IR Permission number
                    reg.getCompanyName(),       // Business name
                    registeredAddress.getUprn(),
                    registeredAddress.getHouseNumber(),
                    registeredAddress.getAddressLine1(),
                    registeredAddress.getAddressLine2(),
                    registeredAddress.getAddressLine3(),
                    registeredAddress.getAddressLine4(),
                    registeredAddress.getTownCity(),
                    registeredAddress.getPostcode(),
                    registeredAddress.getCountry(),
                    registeredAddress.getEasting(),
                    registeredAddress.getNorthing(),
                    reg.getBusinessType(),                                          // Sole trader, Partnership etc.
                    reg.getTier().name(),                                           // UPPER or LOWER (tier).
                    isUpper ? reg.getRegistrationType() : "carrier_broker_dealer",  // Carrier / Broker / Dealer status.
                    eprDateFormatter.format(metaData.getDateActivated()),           // Registration date.
                    isUpper ? eprDateFormatter.format(reg.getExpires_on()) : "",    // Expiry date.
                    COMPANY.equals(reg.getBusinessType()) ? reg.getCompanyNo() : "" // Company number.
                });
            }
        }
        catch (Exception e)
        {
            log.warning(String.format("Unexpected exception writing registration to ePR export: %s", e.getMessage()));
        }
    }
}
