package uk.gov.ea.wastecarrier.services.backgroundJobs;

import com.yammer.dropwizard.lifecycle.Managed;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.CronScheduleBuilder.cronSchedule;

import java.util.logging.Logger;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;

/**
 * This class is a simple wrapper around the Quartz scheduler to integrate it 
 * with DropWizard.  It follows the Singleton pattern.  The result is a
 * mechanism which allows long-running background jobs to be executed within
 * the Waste Carriers Service.
 * 
 * Jobs can either be scheduled using a crontab syntax, or triggered via a
 * DropWizard Task.
 * 
 * TODO: Crontab-style scheduling.
 */
public class BackgroundJobScheduler implements Managed
{
    // Singleton instance.
    private static BackgroundJobScheduler instance;
    
    // Private static members.
    private final static Logger log = Logger.getLogger(BackgroundJobScheduler.class.getName());
    
    // Private string constants.
    private static final String JOB_GROUP = "WasteCarriersService";
    
    // Private instance members relating to Quartz.
    private Scheduler scheduler;
    private Trigger manualJobTrigger;
    
    // Private instance members storing Quartz jobs.
    private JobDetail exportJob;
    
    // Private instance members storing job configuration.
    private ExportJobConfiguration exportJobConfig;
    private DatabaseConfiguration databaseConfig;
    
    /**
     * Returns the single instance of this class.
     * @return The single instance of the BackgroundJobScheduler class.
     */
    public static BackgroundJobScheduler getInstance()
    {
        if (instance == null)
        {
            instance = new BackgroundJobScheduler();
        }
        
        return instance;
    }
    
    /**
     * Sets the database configuration that background jobs should use.  This
     * configuration should be set before the first job is started, and the
     * configuration should not usually be changed again.
     * @param configuration The database configuration to use.
     */
    public void setDatabaseConfiguration(DatabaseConfiguration configuration)
    {
        databaseConfig = configuration;
    }
    
    /**
     * Sets the configuration to use when executing the Export background job.
     * This configuration should be set before the first job is started, and
     * the configuration should not usually be changed again.
     * @param configuration The configuration to use for the Export job.
     */
    public void setExportJobConfiguration(ExportJobConfiguration configuration)
    {
        exportJobConfig = configuration;
    }
    
    /**
     * Logs the specified message under the 'severe' level, then throws a new
     * RuntimeException with the same message.
     * @param message The message to log and use in the exception.
     * @throws RuntimeException 
     */
    private void logAndThrowRuntimeException(String message) throws RuntimeException
    {
        log.severe(message);
        throw new RuntimeException(message);
    }
    
    /**
     * Returns a Quartz JobDetail object defining the "Export" job, which
     * exports data from the Waste Carriers database to files for the electronic
     * Public Register and for a Reporting Snapshot.
     * @return A Quartz JobDetail object defining the "Export" job.
     */
    private JobDetail getExportJob() throws RuntimeException
    {
        // We re-use a single JobDetail regardless how many times it is used.
        if (exportJob == null)
        {
            if (databaseConfig == null)
            {
                logAndThrowRuntimeException("Database configuration has not been set.");
            }
            
            if (exportJobConfig == null)
            {
                logAndThrowRuntimeException("Export Job configuration has not been set.");
            }
            
            exportJob = newJob(ExportJob.class)
                 .withIdentity("ExportJob", JOB_GROUP)
                 .build();
            
            JobDataMap dataMap = exportJob.getJobDataMap();
            dataMap.put(ExportJob.DATABASE_HOST, databaseConfig.getHost());
            dataMap.put(ExportJob.DATABASE_PORT, databaseConfig.getPort());
            dataMap.put(ExportJob.DATABASE_NAME, databaseConfig.getName());
            dataMap.put(ExportJob.DATABASE_USERNAME, databaseConfig.getUsername());
            dataMap.put(ExportJob.DATABASE_PASSWORD, databaseConfig.getPassword());
            dataMap.put(ExportJob.EPR_EXPORT_FILE, exportJobConfig.getEprExportFile());
            dataMap.put(ExportJob.EPR_DATE_FORMAT, exportJobConfig.getEprExportDateFormat());
            dataMap.put(ExportJob.REPORTING_EXPORT_PATH, exportJobConfig.getReportingExportPath());
            dataMap.put(ExportJob.REPORTING_DATE_FORMAT, exportJobConfig.getReportingExportDateFormat());
        }
        
        return exportJob;
    }
    
    /**
     * Returns a Quartz Trigger object that schedules a job for immediate
     * execution.
     * @return A Quartz Trigger object that schedules a job for immediate
     * execution.
     */
    private Trigger getManualJobTrigger()
    {
        // We re-use a single Trigger regardless how many times it is used.
        if (manualJobTrigger == null)
        {
            manualJobTrigger = newTrigger() 
                .withIdentity("ManualJobTrigger", JOB_GROUP)
                .startNow()
                .build();
        }
        
        return manualJobTrigger;
    }
    
    /**
     * Manually schedules the Export job for immediate execution.
     * @throws RuntimeException 
     */
    public void startExportJob() throws RuntimeException
    {
        if (scheduler == null)
        {
            logAndThrowRuntimeException("Cannot start Export job because the Scheduler has not been started.");
        }
        else
        {
            try
            {
                scheduler.scheduleJob(getExportJob(), getManualJobTrigger());
            }
            catch (SchedulerException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
    
    /**
     * If a cron expression for execution of the Export Job has been set in the
     * service configuration then we setup the execution schedule here.
     * @throws SchedulerException 
     */
    private void scheduleExportJob() throws SchedulerException
    {
        boolean scheduled = false;
        
        // Only schedule if we have Export Job configuration.
        if (exportJobConfig != null)
        {
            // Attempt to get cron expression, and trim any white-space.
            String cronExpression = exportJobConfig.getCronExpression();
            if (cronExpression != null)
            {
                cronExpression = cronExpression.trim();
            }
            
            // Only schedule the Export Job if a cron expression has been
            // provided in the Export Job configuration.
            if ((cronExpression != null) && !cronExpression.isEmpty())
            {
                scheduler.scheduleJob(getExportJob(), newTrigger()
                        .withIdentity("ScheduledExport", JOB_GROUP)
                        .withSchedule(
                                cronSchedule(cronExpression)
                                .withMisfireHandlingInstructionFireAndProceed()
                        )
                        .build()
                );
                
                scheduled = true;
                log.info(String.format("Export Job has been setup with cron schedule: %s", cronExpression));
            }
        }
        
        // Log an informative message if the Export Job isn't scheduled.
        if (!scheduled)
        {
            log.info("Export Job is not scheduled for execution; will be started by manual trigger only");
        }
    }
    
    /**
     * Called by DropWizard to start the managed module.  Starts the Quartz
     * scheduler engine.
     * @throws Exception 
     */
    @Override
    public void start() throws Exception
    {
        if (scheduler == null)
        {
            // Create the Scheduler engine.
            log.fine("Starting the Background Job Scheduler...");
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            log.fine("... done");
            
            // Setup any jobs that will run on a schedule.
            scheduleExportJob();
        }
        else
        {
            log.warning("Attempted to start Background Job Scheduler when it is already started.  Doing nothing.");
        }
    }

    /**
     * Called by DropWizard to stop the managed module.  Stops the Quartz
     * scheduler engine.
     * @throws Exception 
     */
    @Override
    public void stop() throws Exception
    {
        if (scheduler != null)
        {
            log.fine("Shutting-down the Background Job Scheduler...");
            scheduler.shutdown(true);
            scheduler = null;
            log.fine("... done");
        }
        else
        {
            log.warning("Attempted to stop Background Job Scheduler when it isn't started.  Doing nothing.");
        }
    }
}
