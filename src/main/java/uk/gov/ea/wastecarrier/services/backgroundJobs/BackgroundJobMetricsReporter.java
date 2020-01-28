package uk.gov.ea.wastecarrier.services.backgroundJobs;

import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.Task;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * DropWizard task that prints some basic metrics about background jobs to the
 * REST caller.
 * 
 * Note: this is a really poor way to provide metrics and should be improved
 * when time allows.  One option may be to update to a more recent version
 * of DropWizard and use the enhanced metrics features it provides.
 * 
 * TODO: Improve upon this mechanism for providing background job metrics.
 */
public class BackgroundJobMetricsReporter extends Task
{
    // A formatter used to provide consistent date styles in the output.
    private static SimpleDateFormat dateFormatter;
    
    /**
     * Helper function that other classes can use to provide a consistent
     * date format in the task output.
     * @param date A date to format.
     * @return A formatted date.
     */
    public static String formatDate(Date date)
    {
        if (dateFormatter == null)
        {
            dateFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        
        return (date == null) ? "[unknown]" : dateFormatter.format(date);
    }
    
    /**
     * Constructor used by DropWizard.  We don't need any custom code.
     * @param name The task's name.
     */
    public BackgroundJobMetricsReporter(String name)
    {
        super(name);
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
        out.println("Note: all timestamps are in UTC.");
        BackgroundJobScheduler.getInstance().reportMetrics(out);
        ExportJob.reportMetrics(out);
    }	
}
