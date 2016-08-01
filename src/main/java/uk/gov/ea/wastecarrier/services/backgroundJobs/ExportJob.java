package uk.gov.ea.wastecarrier.services.backgroundJobs;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobKey;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.DBCursor;

import au.com.bytecode.opencsv.CSVWriter;
import java.text.DecimalFormat;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.Address;
import uk.gov.ea.wastecarrier.services.core.ConvictionSearchResult;
import uk.gov.ea.wastecarrier.services.core.ConvictionSignOff;
import uk.gov.ea.wastecarrier.services.core.FinanceDetails;
import uk.gov.ea.wastecarrier.services.core.KeyPerson;
import uk.gov.ea.wastecarrier.services.core.MetaData;
import uk.gov.ea.wastecarrier.services.core.Order;
import uk.gov.ea.wastecarrier.services.core.OrderItem;
import uk.gov.ea.wastecarrier.services.core.Payment;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;

/**
 * Quartz job which exports data from the Waste Carriers database to flat files
 * which are then used to populate the electronic Public Register (ePR) and
 * Reporting systems.  This job is normally run once daily.
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
    public static final String REPORTING_MONEY_FORMAT = "reporting_money_format";

    // Constants used in string fields of the Registration object.
    private static final String COMPANY = "limitedCompany";
    
    // Private static members.
    private final static Logger log = Logger.getLogger(ExportJob.class.getName());

    // Private instance members, initialised inside execute().
    private SimpleDateFormat eprDateFormatter;
    private SimpleDateFormat reportingDateFormatter;
    private DecimalFormat moneyFormatter;
    
    // Private members referencing files we'll write to.
    private CSVWriter registrationsCsvFile = null;
    private CSVWriter signOffsCsvFile = null;
    private CSVWriter addressesCsvFile = null;
    private CSVWriter keyPeopleCsvFile = null;
    private CSVWriter ordersCsvFile = null;
    private CSVWriter orderItemsCsvFile = null;
    private CSVWriter paymentsCsvFile = null;
    
    // Counters we'll use to create UIDs for relating parent/child items in the
    // reporting snapshot export.
    private int registrationUid = 0;
    private int orderUid = 0;

    // Private members used to provide job metrics.
    private static Date lastStartTime = null;
    private static int lastRunTimeMS = -1;
    private static int eprExportFailCount = 0;
    private static int reportingExportFailCount = 0;
    
    // Used to remove embedded new-lines from strings in the export.
    private static final Pattern newlinePattern = Pattern.compile("\\n+");
    
    // Used for sanitising strings (should remove all Unicode non-printable characters).
    private static final Pattern sanitiserPattern = Pattern.compile("\\p{C}");
    
    /**
     * Public empty constructor, for Quartz.
     */
    public ExportJob()
    {
        // Nothing to do; "initialisation" is done inside execute().
    }
    
    /**
     * Resets metrics we store about this job.
     */
    private void resetJobMetrics()
    {
        lastStartTime = new Date();
        lastRunTimeMS = -1;
        eprExportFailCount = 0;
        reportingExportFailCount = 0;
    }
    
    /**
     * Prints some basic metrics to the specified writer.  Used by the
     * BackgroundJobMetricsReporter.
     * @param out An object to write the metrics to.
     */
    public static void reportMetrics(PrintWriter out)
    {
        out.println("\n** Export job **");
        
        if (lastStartTime == null)
        {
            out.println("The export job has not yet been run.");
        }
        else if (lastRunTimeMS < 0)
        {
            out.println("The export job is currently running.");
            out.println(String.format("Start time: %s", BackgroundJobMetricsReporter.formatDate(lastStartTime)));
        }
        else
        {
            int msPerMin = 1000 * 60;
            int minutes = lastRunTimeMS / msPerMin;
            int seconds = (lastRunTimeMS - (minutes * msPerMin)) / 1000;
            out.println(String.format("Last started: %s", BackgroundJobMetricsReporter.formatDate(lastStartTime)));
            out.println(String.format("Last run-time: %d minutes %02d seconds", minutes, seconds));
            out.println(String.format("Number of registrations that generated an error during ePR export: %d", eprExportFailCount));
            out.println(String.format("Number of registrations that generated an error during Reporting Snapshot export: %d", reportingExportFailCount));
        }
    }
    
    /**
     * Quartz entry point.  Executes this job.
     * @param context Job configuration passed in by Quartz.
     * @throws JobExecutionException 
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        resetJobMetrics();
        
        DatabaseHelper dbHelper = null;
        
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
            log.fine(String.format("--> The Reporting money format is %s", jobConfig.getString(REPORTING_MONEY_FORMAT)));
            
            // Initialise date formatters from provided configuration.
            eprDateFormatter = new SimpleDateFormat(jobConfig.getString(EPR_DATE_FORMAT));
            reportingDateFormatter = new SimpleDateFormat(jobConfig.getString(REPORTING_DATE_FORMAT));
            eprDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            reportingDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            
            // Initialise the money formatter from the provided configuration.
            moneyFormatter = new DecimalFormat(jobConfig.getString(REPORTING_MONEY_FORMAT));
            
            // Build a database helper using the provided configuration.
            dbHelper = new DatabaseHelper(new DatabaseConfiguration(
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
            
            // Almost done; lets calculate job execution duration.
            lastRunTimeMS = (int)((new Date()).getTime() - lastStartTime.getTime());
            
            // Finished successfully.
            log.info("Successfully completed execution of the Export job");
        }
        catch (Exception ex)
        {
            // Quartz only allows us to throw a JobExecutionException from this
            // method, so we wrap all other exceptions.
            log.severe(String.format("Unexpected exception during Export job: %s", ex.getMessage()));
            throw new JobExecutionException(ex);
        }
        finally
        {
            if (dbHelper != null)
            {
                MongoClient mongo = dbHelper.getMongoClient();
                if (mongo != null)
                {
                    mongo.close();
                }
            }
        }
    }
    
    /**
     * Helper that creates a CSVWriter, outputting to a file with UTF-8 encoding.
     * @param path Full path to the file to write to.
     * @return A CSVWriter as described above.
     * @throws Exception 
     */
    private CSVWriter createCsvFileWriter(String path) throws Exception
    {
        return new CSVWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"));
    }
    
    /**
     * Closes a CSVWriter, logging any IOException which is thrown.
     * @param writer The CSVWriter to close.
     */
    private void closeCsvWriterQuietly(CSVWriter writer)
    {
        try
        {
            if (writer != null)
            {
                writer.close();
            }
        }
        catch (IOException ex)
        {
            log.severe(String.format("Unexpected IOError whilst closing a CSVWriter: %s", ex.getMessage()));
        }
    }
    
    /**
     * Safely formats a date.  Doesn't throw an exception if the date is null.
     * @param formatter A formatter to use.
     * @param date A date to format.
     * @return The formatted date, or null if either parameter is null.
     */
    private String safelyFormatDate(SimpleDateFormat formatter, Date date)
    {
        return ((formatter == null) || (date == null)) ? null : formatter.format(date);
    }
    
    /**
     * Safely formats a money amount, expressed in pence as an integer.  Doesn't
     * throw an exception if the formatter is null.
     * @param formatter A formatter to use.
     * @param money The money amount to format.
     * @return The formatted value, or null if the formatter is null.
     */
    private String safelyFormatMoney(DecimalFormat formatter, int money)
    {
        return (formatter == null) ? null : formatter.format(money / 100.0);
    }
    
    /**
     * Safely gets the name of an enumerated value.  Doesn't throw an exception
     * if the value is null.
     * @param value The value to retrieve a name for.
     * @return The name of the enum value, or null if the value itself is null.
     */
    private String safelyGetEnumName(Enum value)
    {
        return (value == null) ? null : value.toString();
    }
    
    /**
     * Gets the Company Number for a registration, returning null if the raw
     * company number does not appear to be valid.
     * @param registration The registration to retrieve the company number for.
     * @return A string containing the company number, or null if the raw
     *   company number does not appear to be valid.
     */
    private String safelyGetValidCompanyNumberForEpr(Registration registration)
    {
        String result = null;
        
        if ((registration != null) && COMPANY.equals(registration.getBusinessType()))
        {
            result = registration.getCompanyNo();
            if (result != null)
            {
                result = result.trim();
                
                // Check if the company number contains only zeroes.
                if (result.matches("^0+$"))
                {
                    result = null;
                }
            }
        }
        
        return result;
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
     * Sanitises newline characters in a single string, replacing them with
     * something suitable for a CSV export.  Should be used on each string where
     * an embedded newline character could reasonably be expected, but no point
     * in applying to strings where this is not the case.
     * @param input A string to sanitise from embedded newlines.
     * @return A sanitised string.
     */
    private String sanitiseNewlines(String input)
    {
        return (input == null) ? null : newlinePattern.matcher(input).replaceAll(". ");
    }
    
    /**
     * Sanitises an array of strings, removing all Unicode non-printable characters.
     * Should be applied to all CSV output that could contain user-entered data.
     * @param input An array of strings to sanitise.
     * @return A sanitised array of strings.
     */
    private String[] sanitiseStrings(String[] input)
    {
        String[] result = null;
        
        if ((input != null) && (input.length > 0))
        {
            int nItems = input.length;
            result = new String[nItems];
            for (int n = 0; n < nItems; n++)
            {
                result[n] = (input[n] == null) ? null : sanitiserPattern.matcher(input[n]).replaceAll("");
            }
        }
        
        return result;
    }
    
    /**
     * Iterates over all registrations in the database, processing each in turn.
     * If appropriate, the registration details will be exported to one or more
     * of the export files.
     * @param database A Mongo database object providing access to the
     * registration data.
     * @param jobConfig Configuration options for the execution of this job.
     * @throws Exception 
     */
    private void processAllRegistrations(DB database, JobDataMap jobConfig) throws Exception
    {
        log.info("Beginning processing all Registration records");
        
        // A cursor we will use later.
        DBCursor<Registration> dbcur = null;
        
        // Create MONGOJACK connection to the database.
        JacksonDBCollection<Registration, String> registrations = JacksonDBCollection.wrap(
            database.getCollection(Registration.COLLECTION_NAME), Registration.class, String.class);
        
        // We declare the ePR CSVWriter here, and not as a class member, so that
        // only functions we explicitly pass it to can access this CSV file.
        // This reduces the risk of somebody accidentically "leaking" data
        // into the ePR ("public") CSV file.
        CSVWriter eprCsvFile = null;
            
        // Make sure we don't leak resources, even on an exception.
        try
        {
            // Initialise the EPR export.  
            eprCsvFile = createCsvFileWriter(jobConfig.getString(EPR_EXPORT_FILE));
            eprCsvFile.writeNext(getEprColumnTitles());

            // Initialise the Reporting Snapshot exports.
            registrationsCsvFile = createCsvFileWriter(getReportingSnapshotFileWithPath(jobConfig, "registrations.csv"));
            addressesCsvFile = createCsvFileWriter(getReportingSnapshotFileWithPath(jobConfig, "addresses.csv"));
            keyPeopleCsvFile = createCsvFileWriter(getReportingSnapshotFileWithPath(jobConfig, "key_people.csv"));
            signOffsCsvFile = createCsvFileWriter(getReportingSnapshotFileWithPath(jobConfig, "sign_offs.csv"));
            ordersCsvFile = createCsvFileWriter(getReportingSnapshotFileWithPath(jobConfig, "orders.csv"));
            orderItemsCsvFile = createCsvFileWriter(getReportingSnapshotFileWithPath(jobConfig, "order_items.csv"));
            paymentsCsvFile = createCsvFileWriter(getReportingSnapshotFileWithPath(jobConfig, "payments.csv"));
            
            registrationsCsvFile.writeNext(getRegistrationExportColumnTitles());
            addressesCsvFile.writeNext(getAddressesExportColumnTitles());
            keyPeopleCsvFile.writeNext(getKeyPeopleExportColumnTitles());
            signOffsCsvFile.writeNext(getSignOffsExportColumnTitles());
            ordersCsvFile.writeNext(getOrdersExportColumnTitles());
            orderItemsCsvFile.writeNext(getOrderItemsExportColumnTitles());
            paymentsCsvFile.writeNext(getPaymentExportColumnTitles());

            // Process all registrations in the database.
            // IMPORTANT: As this cursor will be quite long-lived, we need to
            // enable the "snapshot" option to ensure each document is returned
            // only once.
            dbcur = registrations.find().snapshot();
            for (Registration reg : dbcur)
            {
                registrationUid++;
                exportRegistrationForReporting(reg);
                exportRegistrationForEpr(eprCsvFile, reg);
            }
        }
        finally
        {
            // Release Mongo resource.
            if (dbcur != null)
            {
                dbcur.close();
            }
            
            // Release Reporting Snapshot export files.
            closeCsvWriterQuietly(registrationsCsvFile);
            registrationsCsvFile = null;
            closeCsvWriterQuietly(addressesCsvFile);
            addressesCsvFile = null;
            closeCsvWriterQuietly(keyPeopleCsvFile);
            keyPeopleCsvFile = null;
            closeCsvWriterQuietly(signOffsCsvFile);
            signOffsCsvFile = null;
            closeCsvWriterQuietly(ordersCsvFile);
            ordersCsvFile = null;
            closeCsvWriterQuietly(orderItemsCsvFile);
            orderItemsCsvFile = null;
            closeCsvWriterQuietly(paymentsCsvFile);
            paymentsCsvFile = null;
            
            // Release ePR export file.
            closeCsvWriterQuietly(eprCsvFile);
            eprCsvFile = null;  // Deliberate unused assignment.
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
     * @param eprCsvFile Object providing methods to write to the ePR CSV file.
     * @param reg The registration record to export.
     */
    private void exportRegistrationForEpr(CSVWriter eprCsvFile, Registration reg)
    {
        boolean hadError = false;
        
        try
        {
            MetaData metaData = reg.getMetaData();
            Address registeredAddress = reg.getFirstAddressByType(Address.addressType.REGISTERED);

            if ((metaData == null) || (registeredAddress == null))
            {
                hadError = true;
                log.warning(String.format("Cannot export %s to ePR because Metadata or Registered Address is missing", reg.getRegIdentifier()));
            }
            else if (reg.goesOnPublicRegister())
            {
                boolean isUpper = (reg.getTier() == Registration.RegistrationTier.UPPER);
                
                eprCsvFile.writeNext(sanitiseStrings(new String[] {
                    reg.getRegIdentifier(),     // Permit number
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
                    registeredAddress.getFirstOrOnlyEasting(),
                    registeredAddress.getFirstOrOnlyNorthing(),
                    reg.getBusinessType(),                                                    // Sole trader, Partnership etc.
                    reg.getTier().name(),                                                     // UPPER or LOWER (tier).
                    isUpper ? reg.getRegistrationType() : "carrier_broker_dealer",            // Carrier / Broker / Dealer status.
                    safelyFormatDate(eprDateFormatter, metaData.getDateActivated()),          // Registration date.
                    isUpper ? safelyFormatDate(eprDateFormatter, reg.getExpires_on()) : null, // Expiry date.
                    safelyGetValidCompanyNumberForEpr(reg)                                    // Company number.
                }));
            }
        }
        catch (Exception ex)
        {
            hadError = true;
            log.warning(String.format("Unexpected exception writing registration to ePR export: %s", ex.getMessage()));
        }
        
        eprExportFailCount += (hadError ? 1 : 0);
    }
    
    /**
     * Returns an array of column titles for the Registrations component of the
     * Reporting Snapshot export.
     * @return An array of strings, one per CSV-file column.
     */
    private String[] getRegistrationExportColumnTitles()
    {
        return new String[] {
            "RegistrationUID",
            "RegistrationNumber",
            "RenewedRegistrationNumber",
            "Tier",
            "OrganisationType",
            "RegistrationType",
            "OrganisationName",
            "CompanyNumber",
            "ContactFirstName",
            "ContactLastName",
            "ContactPhoneNumber",
            "ContactEmail",
            "AccountEmail",
            "Status",
            "Balance",
            "RevokedReason",
            "RegistrationTimestamp",
            "ActivationTimestamp",
            "ExpiryTimestamp",
            "LastModifiedTimestamp",
            
            "Route",
            "OtherBusinesses",
            "IsMainService",
            "ConstructionWaste",
            "OnlyAMF",
            "Declaration",
            "DeclaredConvictions",
            
            "OrganisationFlaggedForReview",
            "ReviewFlagTimestamp"
        };
    }
    
    /**
     * Exports details of a registration to a group of CSV files in the format
     * used for the reporting snapshot.  Any exceptions are handed within the
     * function.
     * @param reg  The registration record to export.
     */
    private void exportRegistrationForReporting(Registration reg)
    {
        boolean hadError = false;
        
        try
        {
            MetaData metaData = reg.getMetaData();
            FinanceDetails financeDetails = reg.getFinanceDetails();
            ConvictionSearchResult csr = reg.getConvictionSearchResult();
        
            if (metaData == null)
            {
                hadError = true;
                log.warning(String.format("Cannot export %s to Reporting Snapshot because Metadata is missing", reg.getRegIdentifier()));
            }
            else
            {
                registrationsCsvFile.writeNext(sanitiseStrings(new String[] {
                    // The "interesting" fields.
                    Integer.toString(registrationUid),      // A "unique ID"
                    reg.getRegIdentifier(),                 // Permit number
                    reg.getOriginalRegistrationNumber(),    // Old IR number, for IR renewals
                    safelyGetEnumName(reg.getTier()),       // Tier
                    reg.getBusinessType(),                  // Company vs Partnership vs Sole Trader etc.
                    reg.getRegistrationType(),              // Carrier / Broker / Dealer status.
                    reg.getCompanyName(),                   // Business name
                    reg.getCompanyNo(),
                    reg.getFirstName(),
                    reg.getLastName(),
                    reg.getPhoneNumber(),
                    reg.getContactEmail(),
                    reg.getAccountEmail(),
                    safelyGetEnumName(metaData.getStatus()),
                    (financeDetails != null) ? safelyFormatMoney(moneyFormatter, financeDetails.getBalance()) : null,
                    sanitiseNewlines(metaData.getRevokedReason()),
                    safelyFormatDate(reportingDateFormatter, metaData.getDateRegistered()),
                    safelyFormatDate(reportingDateFormatter, metaData.getDateActivated()),
                    safelyFormatDate(reportingDateFormatter, reg.getExpires_on()),
                    safelyFormatDate(reportingDateFormatter, metaData.getLastModified()),
                    
                    // Fields related to the application only.
                    safelyGetEnumName(metaData.getRoute()),
                    reg.getOtherBusinesses(),
                    reg.getIsMainService(),
                    reg.getConstructionWaste(),
                    reg.getOnlyAMF(),
                    reg.getDeclaration(),
                    reg.getDeclaredConvictions(),
                    
                    // Organisation conviction search.
                    (csr != null) ? safelyGetEnumName(csr.getMatchResult()) : null,
                    (csr != null) ? safelyFormatDate(reportingDateFormatter, csr.getSearchedAt()) : null
                }));
                
                if (!exportRegistrationAddresses(reg) || !exportRegistrationSignOffs(reg)
                        || !exportRegistrationKeyPeople(reg) || !exportRegistrationFinanceDetails(reg))
                {
                    hadError = true;
                }
            }
        }
        catch (Exception ex)
        {
            log.warning(String.format("Error exporting registration to reporting snapshot: %s", ex.getMessage()));
            hadError = true;
        }
        
        reportingExportFailCount += (hadError ? 1 : 0);
    }
    
    /**
     * Returns an array of column titles for the Addresses component of the
     * Reporting Snapshot export.
     * @return An array of strings, one per CSV-file column.
     */
    private String[] getAddressesExportColumnTitles()
    {
        return new String[] {
            "RegistrationUID",
            "AddressType",
            "UPRN",
            "Premises",
            "AddressLine1",
            "AddressLine2",
            "AddressLine3",
            "AddressLine4",
            "TownCity",
            "Postcode",
            "Country",
            "Easting",
            "Northing",
            "CorrespondentFirstName",
            "CorrespondentLastName"
        };
    }
    
    /**
     * Exports all the addresses for a registration to the Reporting Snapshot
     * files.
     * @param reg The registration to export data for.
     * @return True if no error occurs during export; otherwise False.
     */
    private boolean exportRegistrationAddresses(Registration reg)
    {
        boolean noErrors = true;
        
        List<Address> addresses = reg.getAddresses();
        if (addresses != null)
        {
            for (Address addr : addresses)
            {
                try
                {
                    addressesCsvFile.writeNext(sanitiseStrings(new String[] {
                        Integer.toString(registrationUid),
                        safelyGetEnumName(addr.getAddressType()),
                        addr.getUprn(),
                        addr.getHouseNumber(),
                        addr.getAddressLine1(),
                        addr.getAddressLine2(),
                        addr.getAddressLine3(),
                        addr.getAddressLine4(),
                        addr.getTownCity(),
                        addr.getPostcode(),
                        addr.getCountry(),
                        addr.getFirstOrOnlyEasting(),
                        addr.getFirstOrOnlyNorthing(),
                        addr.getFirstName(),
                        addr.getLastName()
                    }));
                }
                catch (Exception ex)
                {
                    log.warning(String.format("Error exporting address to reporting snapshot: %s", ex.getMessage()));
                    noErrors = false;
                }
            }
        }
        
        return noErrors;
    }
    
    /**
     * Returns an array of column titles for the Sign-Offs component of the
     * Reporting Snapshot export.
     * @return An array of strings, one per CSV-file column.
     */
    private String[] getSignOffsExportColumnTitles()
    {
        return new String[] {
            "RegistrationUID",
            "Confirmed",
            "ConfirmedBy",
            "Timestamp"
        };
    }
    
    /**
     * Exports all the conviction sign-offs for a registration to the Reporting
     * Snapshot files.
     * @param reg The registration to export data for.
     * @return True if no error occurs during the export; otherwise False.
     */
    private boolean exportRegistrationSignOffs(Registration reg)
    {
        boolean noErrors = true;
        
        List<ConvictionSignOff> signOffs = reg.getConviction_sign_offs();
        if (signOffs != null)
        {
            for (ConvictionSignOff signOff : signOffs)
            {
                try
                {
                    signOffsCsvFile.writeNext(sanitiseStrings(new String[] {
                        Integer.toString(registrationUid),
                        signOff.getConfirmed(),
                        signOff.getConfirmedBy(),
                        safelyFormatDate(reportingDateFormatter, signOff.getConfirmedAt())
                    }));
                }
                catch (Exception ex)
                {
                    log.warning(String.format("Error exporting sign-off to reporting snapshot: %s", ex.getMessage()));
                    noErrors = false;
                }
            }
        }
        
        return noErrors;
    }
    
    /**
     * Returns an array of column titles for the Key People component of the
     * Reporting Snapshot export.
     * @return An array of strings, one per CSV-file column.
     */
    private String[] getKeyPeopleExportColumnTitles()
    {
        return new String[] {
            "RegistrationUID",
            "PersonType",
            "FirstName",
            "LastName",
            "Position",
            "FlaggedForReview",
            "ReviewFlagTimestamp"
        };
    }
    
    /**
     * Exports all the Key People associated with a registration to the
     * Reporting Snapshot files.
     * @param reg The registration to export data for.
     * @return True if no error occurs during the export; otherwise False.
     */
    private boolean exportRegistrationKeyPeople(Registration reg)
    {
        boolean noErrors = true;
        
        List<KeyPerson> keyPeople = reg.getKeyPeople();
        if (keyPeople != null)
        {
            for (KeyPerson keyPerson : keyPeople)
            {
                try
                {
                    ConvictionSearchResult csr = keyPerson.getConvictionSearchResult();
                    
                    keyPeopleCsvFile.writeNext(sanitiseStrings(new String[] {
                        Integer.toString(registrationUid),
                        safelyGetEnumName(keyPerson.getPersonType()),
                        keyPerson.getFirstName(),
                        keyPerson.getLastName(),
                        keyPerson.getPosition(),
                        (csr != null) ? safelyGetEnumName(csr.getMatchResult()) : null,
                        (csr != null) ? safelyFormatDate(reportingDateFormatter, csr.getSearchedAt()) : null
                    }));
                }
                catch (Exception ex)
                {
                    log.warning(String.format("Error exporting Key People to reporting snapshot: %s", ex.getMessage()));
                    noErrors = false;
                }
            }
        }
        
        return noErrors;
    }
    
    /**
     * Returns an array of column titles for the Orders component of the
     * Reporting Snapshot export.
     * @return An array of strings, one per CSV-file column.
     */
    private String[] getOrdersExportColumnTitles()
    {
        return new String[] {
            "RegistrationUID",
            "OrderUID",
            "OrderCode",
            "PaymentMethod",
            "TotalCharge",
            "Description",
            "MerchantID",
            "CreationTimestamp",
            "LastModifiedTimestamp",
            "LastModifiedBy"
        };
    }
    
    /**
     * Returns an array of column titles for the Order Items component of the
     * Reporting Snapshot export.
     * @return An array of strings, one per CSV-file column.
     */
    private String[] getOrderItemsExportColumnTitles()
    {
        return new String[] {
            "RegistrationUID",
            "OrderUID",
            "ItemType",
            "ItemCharge",
            "Description",
            "Reference",
            "LastModifiedTimestamp"
        };
    }
    
    /**
     * Returns an array of column titles for the Payments component of the
     * Reporting Snapshot export.
     * @return An array of strings, one per CSV-file column.
     */
    private String[] getPaymentExportColumnTitles()
    {
        return new String[] {
            "RegistrationUID",
            "OrderKey",
            "PaymentType",
            "Amount",
            "Reference",
            "Comment",
            "PaymentReceivedTimestamp",
            "PaymentEnteredTimestamp",
            "LastModifiedBy"
        };
    }
    
    /**
     * Exports all the Financial Details associated with a registration to the
     * Reporting Snapshot files.
     * @param reg The registration to export data for.
     * @return True if no error occurs during the export; otherwise False.
     */
    private boolean exportRegistrationFinanceDetails(Registration reg)
    {
        boolean noErrors = true;
        
        FinanceDetails financeDetails = reg.getFinanceDetails();
        if (financeDetails != null)
        {
            // Export orders.
            List<Order> orders = financeDetails.getOrders();
            if (orders != null)
            {
                for (Order order : orders)
                {
                    orderUid++;
                    if (!exportRegistrationOrder(order))
                    {
                        noErrors = false;
                    }
                }
            }
            
            // Export payments.
            List<Payment> payments = financeDetails.getPayments();
            if (payments != null)
            {
                for (Payment payment : payments)
                {
                    try
                    {
                        paymentsCsvFile.writeNext(sanitiseStrings(new String[] {
                            Integer.toString(registrationUid),
                            payment.getOrderKey(),
                            safelyGetEnumName(payment.getPaymentType()),
                            safelyFormatMoney(moneyFormatter, payment.getAmount()),
                            payment.getRegistrationReference(),
                            sanitiseNewlines(payment.getComment()),
                            safelyFormatDate(reportingDateFormatter, payment.getDateReceived()),
                            safelyFormatDate(reportingDateFormatter, payment.getDateEntered()),
                            payment.getUpdatedByUser()
                        }));
                    }
                    catch (Exception ex)
                    {
                        log.warning(String.format("Error exporting Payment to reporting snapshot: %s", ex.getMessage()));
                        noErrors = false;
                    }
                }
            }           
        }
        
        return noErrors;
    }
    
    /**
     * Exports a single Order to the Reporting Snapshot files.
     * @param order The order to export data for.
     * @return True if no error occurs during the export; otherwise False.
     */
    private boolean exportRegistrationOrder(Order order)
    {
        boolean noErrors = true;
        
        try
        {
            // Export the Order itself.
            ordersCsvFile.writeNext(sanitiseStrings(new String[] {
                Integer.toString(registrationUid),
                Integer.toString(orderUid),
                order.getOrderCode(),
                safelyGetEnumName(order.getPaymentMethod()),
                safelyFormatMoney(moneyFormatter, order.getTotalAmount()),
                sanitiseNewlines(order.getDescription()),
                order.getMerchantId(),
                safelyFormatDate(reportingDateFormatter, order.getDateCreated()),
                safelyFormatDate(reportingDateFormatter, order.getDateLastUpdated()),
                order.getUpdatedByUser()
            }));
            
            // Export all the items within the Order.
            List<OrderItem> orderItems = order.getOrderItems();
            if (orderItems != null)
            {
                for (OrderItem item : orderItems)
                {
                    orderItemsCsvFile.writeNext(sanitiseStrings(new String[] {
                        Integer.toString(registrationUid),
                        Integer.toString(orderUid),
                        safelyGetEnumName(item.getType()),
                        safelyFormatMoney(moneyFormatter, item.getAmount()),
                        sanitiseNewlines(item.getDescription()),
                        item.getReference(),
                        safelyFormatDate(reportingDateFormatter, item.getLastUpdated())
                    }));
                }
            }
        }
        catch (Exception ex)
        {
            log.warning(String.format("Error exporting Order to reporting snapshot: %s", ex.getMessage()));
            noErrors = false;
        }
        
        return noErrors;
    }
}
