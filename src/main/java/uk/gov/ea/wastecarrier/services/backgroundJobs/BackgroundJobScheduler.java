package uk.gov.ea.wastecarrier.services.backgroundJobs;

import io.dropwizard.lifecycle.Managed;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.SchedulerMetaData;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.CronScheduleBuilder.cronSchedule;

import java.io.PrintWriter;
import java.util.logging.Logger;
import java.util.List;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;

/**
 * This class is a simple wrapper around the Quartz scheduler to integrate it 
 * with DropWizard.  It follows the Singleton pattern.  The result is a
 * mechanism which allows long-running background jobs to be executed within
 * the Waste Carriers Service.
 * 
 * Jobs can either be scheduled using a crontab syntax, or triggered via a
 * DropWizard Task.
 */
public class BackgroundJobScheduler implements Managed
{
    // Singleton instance.
    private static BackgroundJobScheduler instance;
    
    // Private static members.
    private final static Logger log = Logger.getLogger(BackgroundJobScheduler.class.getName());
    
    // Private string constants.
    private static final String EXPORT_JOB = "ExportJob";
    private static final String CRON_GROUP = "CronJobs";
    private static final String MANUAL_GROUP = "ManuallyTriggeredJobs";
    
    // Private instance members relating to Quartz.
    private Scheduler scheduler;
    
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
     * Sets the configuration to use for the Export background job.
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
     * Checks if a job with the specified name is currently executing.
     * @param jobName The name of the job to check for.
     * @return True if a job with the specified name is executing; False otherwise.
     */
    private boolean isJobExecuting(String jobName)
    {
        boolean result = false;
        
        if (scheduler != null)
        {
            try
            {
                List<JobExecutionContext> currentJobs = scheduler.getCurrentlyExecutingJobs();
                if ((currentJobs != null) && !currentJobs.isEmpty())
                {
                    for (JobExecutionContext job : currentJobs)
                    {
                        JobDetail jobDetail = job.getJobDetail();
                        if (jobDetail != null)
                        {
                            JobKey jobKey = jobDetail.getKey();
                            if ((jobKey != null) && jobName.equals(jobKey.getName()))
                            {
                                result = true;
                                break;
                            }
                        }
                    }
                }
            }
            catch (SchedulerException e)
            {
                log.warning("Unexpected error whilst checking if job already executing");
            }
        }
        
        return result;
    }
    
    /**
     * Returns a Quartz JobDetail object defining the "Export" job, which
     * exports data from the Waste Carriers database to files for the electronic
     * Public Register and for a Reporting Snapshot.
     * @param jobGroup The group to place the job in.
     * @return A Quartz JobDetail object defining the "Export" job.
     */
    private JobDetail getExportJob(String jobGroup) throws RuntimeException
    {
        if (databaseConfig == null)
        {
            logAndThrowRuntimeException("Database configuration has not been set.");
        }

        if (exportJobConfig == null)
        {
            logAndThrowRuntimeException("Export Job configuration has not been set.");
        }

        JobDetail exportJob = newJob(ExportJob.class)
            .withIdentity(EXPORT_JOB, jobGroup)
            .build();

        JobDataMap dataMap = exportJob.getJobDataMap();
        dataMap.put(ExportJob.DATABASE_URI, databaseConfig.getUri());
        dataMap.put(ExportJob.DATABASE_TIMEOUT, databaseConfig.getServerSelectionTimeout());
        dataMap.put(ExportJob.EPR_EXPORT_FILE, exportJobConfig.getEprExportFile());
        dataMap.put(ExportJob.EPR_DATE_FORMAT, exportJobConfig.getEprExportDateFormat());
        dataMap.put(ExportJob.REPORTING_EXPORT_PATH, exportJobConfig.getReportingExportPath());
        dataMap.put(ExportJob.REPORTING_DATE_FORMAT, exportJobConfig.getReportingExportDateFormat());
        dataMap.put(ExportJob.REPORTING_MONEY_FORMAT, exportJobConfig.getReportingExportMoneyFormat());
        
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
        return newTrigger() 
                .withIdentity("ManualJobTrigger", MANUAL_GROUP)
                .startNow()
                .build();
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
                if (isJobExecuting(EXPORT_JOB))
                {
                    throw new RuntimeException("Export Job is already executing");
                }
                scheduler.scheduleJob(getExportJob(MANUAL_GROUP), getManualJobTrigger());
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
            // provided in the job configuration.
            if ((cronExpression != null) && !cronExpression.isEmpty())
            {
                scheduler.scheduleJob(getExportJob(CRON_GROUP), newTrigger()
                        .withIdentity("ScheduledExport", CRON_GROUP)
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
            log.info("Waiting for any current background jobs to finish executing...");
            scheduler.shutdown(true);
            scheduler = null;
            log.fine("... done");
        }
        else
        {
            log.warning("Attempted to stop Background Job Scheduler when it isn't started.  Doing nothing.");
        }
    }
    
    /**
     * Prints some basic metrics to the specified writer.  Used by the
     * BackgroundJobMetricsReporter.
     * @param out An object to write the metrics to.
     */
    public void reportMetrics(PrintWriter out)
    {
        out.println("\n** Scheduler **");
        
        if (scheduler == null)
        {
            out.println("The scheduler is not running; no metrics to report.");
        }
        else
        {
            try
            {
                SchedulerMetaData smd = scheduler.getMetaData();
                out.println(String.format("Scheduler running since %s", BackgroundJobMetricsReporter.formatDate(smd.getRunningSince())));
                out.println(String.format("Number of jobs executed: %d", smd.getNumberOfJobsExecuted()));
                out.println(String.format("Thread pool size: %d", smd.getThreadPoolSize()));
                
                List<JobExecutionContext> currentJobs = scheduler.getCurrentlyExecutingJobs();
                if ((currentJobs != null) && !currentJobs.isEmpty())
                {
                    out.println("Currently-executing jobs:");
                    for (JobExecutionContext job : currentJobs)
                    {
                        out.println(String.format(" - %s (running for %dms)",
                                job.getJobDetail().getKey().toString(),
                                job.getJobRunTime()));
                    }
                }
                else
                {
                    out.println("No jobs are currently executing.");
                }
            }
            catch (SchedulerException ex)
            {
                out.println("Exception whilst attempting to report scheduler metrics.");
            }
        }
    }
}
