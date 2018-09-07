package uk.gov.ea.wastecarrier.services.backgroundJobs;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import com.mongodb.DB;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.MetaData;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.helper.DatabaseHelper;

/**
 * Quartz job which updates the status of Registrations in the Waste Carriers
 * database to reflect the passage of time.  At present, the only change made
 * is to mark Registrations as "EXPIRED" at the appropriate time.
 */
@DisallowConcurrentExecution
public class RegistrationStatusJob implements Job
{
    // Public 'constants' used in the JobDataMap, which passes configuration
    // to this job.
    public static final String DATABASE_URI = "database_uri";
    public static final String DATABASE_TIMEOUT = "database_timeout";

    // Private static members.
    private final static Logger log = Logger.getLogger(RegistrationStatusJob.class.getName());
    
    // Private members used to provide job metrics.
    private static Date lastStartTime = null;
    private static int lastRunTimeMS = -1;
    private static int expiredCount = 0;
    
    /**
     * Public empty constructor, for Quartz.
     */
    public RegistrationStatusJob() {
        // Nothing to do; "initialisation" is done inside execute().
    }

    /**
     * Resets metrics we store about this job.
     */
    private void resetJobMetrics() {
        lastStartTime = new Date();
        lastRunTimeMS = -1;
        expiredCount = 0;
    }
    
    /**
     * Prints some basic metrics to the specified writer.  Used by the
     * BackgroundJobMetricsReporter.
     * @param out An object to write the metrics to.
     */
    public static void reportMetrics(PrintWriter out) {
        out.println("\n** Registration Status job **");
        
        if (lastStartTime == null) {
            out.println("The registration status job has not yet been run.");
        }
        else if (lastRunTimeMS < 0) {
            out.println("The registration status job is currently running.");
            out.println(String.format("Start time: %s", BackgroundJobMetricsReporter.formatDate(lastStartTime)));
        }
        else {
            int msPerMin = 1000 * 60;
            int minutes = lastRunTimeMS / msPerMin;
            int seconds = (lastRunTimeMS - (minutes * msPerMin)) / 1000;
            out.println(String.format("Last started: %s", BackgroundJobMetricsReporter.formatDate(lastStartTime)));
            out.println(String.format("Last run-time: %d minutes %02d seconds", minutes, seconds));
            out.println(String.format("Number of registrations that were expired: %d", expiredCount));
        }
    }
    
    /**
     * Quartz entry point.  Executes this job.
     * @param context Job configuration passed in by Quartz.
     * @throws JobExecutionException 
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        resetJobMetrics();
        
        DatabaseHelper dbHelper = null;
        
        try {
            // Record the start of the job execution.
            JobKey jobKey = context.getJobDetail().getKey();
            log.info(String.format("Starting execution of the Registration Status job, with key %s", jobKey.toString()));
            
            // Log job configuration for debugging purposes.
            JobDataMap jobConfig = context.getJobDetail().getJobDataMap();
            
            // Build a database helper using the provided configuration.
            dbHelper = new DatabaseHelper(new DatabaseConfiguration(
                jobConfig.getString(DATABASE_URI),
                jobConfig.getInt(DATABASE_TIMEOUT)
            ));
            
            // Check we can connect to the database, and are authenticated.
            DB db = dbHelper.getConnection();
            if (db == null) {
                throw new RuntimeException("Error: No database connection available; aborting.");
            }
            
            // Get access to the Registrations document collection.
            JacksonDBCollection<Registration, String> registrations = JacksonDBCollection.wrap(
                    db.getCollection(Registration.COLLECTION_NAME), Registration.class, String.class);
            
            // Do useful work...
            expireRegistrations(registrations);
            
            // Almost done; lets calculate job execution duration.
            lastRunTimeMS = (int)((new Date()).getTime() - lastStartTime.getTime());
            
            // Finished successfully.
            log.info("Successfully completed execution of the Registration Status job");
        } catch (Exception ex) {
            // Quartz only allows us to throw a JobExecutionException from this
            // method, so we wrap all other exceptions.
            log.severe(String.format("Unexpected exception during Registration Status job: %s", ex.getMessage()));
            throw new JobExecutionException(ex);
        } finally {
            if (dbHelper != null) {
                MongoClient mongo = dbHelper.getMongoClient();
                if (mongo != null) {
                    mongo.close();
                }
            }
        }
    }
    
    /**
     * Searches for all active upper-tier registrations that have expired, and
     * marks them as expired.
     * @param registrations The registrations document collection.
     */
    public void expireRegistrations(JacksonDBCollection<Registration, String> registrations) {
        // Expiration is not time dependent. So our filter will be anything where
        // expires_on is less than 00:00am tomorrow (so anything up 23:59 today)
        Date tomorrow = tomorrowsDate();

        // Define a query that decides which documents to update.
        // We want to find all Upper-Tier registrations that are currently in
        // the ACTIVE state and have an expiry date of earlier than 'now'.
        BasicDBObject query = new BasicDBObject();
        query.append("tier", Registration.RegistrationTier.UPPER.toString());
        query.append("metaData.status", MetaData.RegistrationStatus.ACTIVE.toString());
        query.append("expires_on", new BasicDBObject("$lt", tomorrow));
        
        // Define an operation that describes how to update all the matched
        // documents.  We want to update the registration status to EXPIRED.
        // We'll also update the 'last modified' timestamp.
        BasicDBObject fieldsToSet = new BasicDBObject();
        fieldsToSet.append("metaData.status", MetaData.RegistrationStatus.EXPIRED.toString());
        fieldsToSet.append("metaData.lastModified", new Date());
        BasicDBObject update = new BasicDBObject("$set", fieldsToSet);
        
        // Expire all relevant registrations.
        WriteResult<Registration, String> result = null;
        try {
            result = registrations.updateMulti(query, update);
        } catch (Exception e) {
            log.severe(String.format("MongoJack error expiring registrations: %s", e.getMessage()));
        }
        
        // Handle metrics.
        if (result != null) {
            expiredCount = result.getN();
        }
        log.info(String.format("Number of registrations expired: %d", expiredCount));
    }

    /**
     * Returns the date to use when determining if a registration is expired or
     * not. It takes the current date, removes the time element, then adds a day.
     * So when searching for registrations that are expired, we are looking for
     * anything less than this date.
     *
     * @return the date tomorrow, with time set to 0
     */
    private Date tomorrowsDate() {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.set(Calendar.HOUR_OF_DAY, 0);
        tomorrow.set(Calendar.MINUTE, 0);
        tomorrow.set(Calendar.SECOND, 0);
        tomorrow.add(Calendar.DATE, 1);

        return tomorrow.getTime();
    }
}