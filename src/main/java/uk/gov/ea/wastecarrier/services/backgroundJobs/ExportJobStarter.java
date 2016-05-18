package uk.gov.ea.wastecarrier.services.backgroundJobs;

import com.yammer.dropwizard.tasks.Task;

import java.io.PrintWriter;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.google.common.collect.ImmutableMultimap;

/**
 * DropWizard task that allows the background Export Job to be started manually
 * by a HTTP REST call.
 */
public class ExportJobStarter extends Task
{
    private static Logger log = Logger.getLogger(ExportJobStarter.class.getName());

    /**
     * Constructor used by DropWizard.  We don't need any custom code.
     * @param name The task's name.
     */
    public ExportJobStarter(String name)
    {
        super(name);
    }

    /**
     * Utility method to write a message to the log, and output the same message
     * to a PrintWriter.
     * @param logLevel The level to log the message at (severe, info, etc).
     * @param out An object allowing formatted messages to be returned to the
     * REST call originator.
     * @param message The string to log and output.
     */
    private void outputAndLogMessage(Level logLevel, PrintWriter out, String message)
    {
        log.log(logLevel, message);
        out.println(message);
    }
    
    /**
     * Executes the task.
     * @param arg0 Task parameters provided via the query string.  We don't use
     * this value.
     * @param out An object allowing formatted messages to be returned to the
     * REST call originator.
     * @throws Exception 
     */
    @Override
    public void execute(ImmutableMultimap<String, String> arg0, PrintWriter out) throws Exception
    {
        outputAndLogMessage(Level.INFO, out, "Attempting to schedule the Export Job for immediate execution...");

        try
        {
            BackgroundJobScheduler.getInstance().startExportJob();
            outputAndLogMessage(Level.INFO, out, "Export Job execution has been successfully scheduled.");
        }
        catch (RuntimeException e)
        {
            outputAndLogMessage(Level.SEVERE, out,
                    String.format("An error occurred whilst scheduling the Export Job: %s", e.getMessage()));
        }
    }	
}
