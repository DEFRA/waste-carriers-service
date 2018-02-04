package uk.gov.ea.wastecarrier.services.tasks;

import com.yammer.dropwizard.tasks.Task;
import com.google.common.collect.ImmutableMultimap;

import java.util.logging.Logger;

import java.io.PrintWriter;

/**
 * This class exists solely to allow an exception to be generated on demand, so
 * that we can test exception handling, particularly AirBrake / Errbit
 * integration.
 */ 
public class ExceptionTester extends Task
{
    private static final Logger log = Logger.getLogger(ExceptionTester.class.getName());
    
    public ExceptionTester(String name)
    {
        super(name);
    }
    
    @Override
    public void execute(ImmutableMultimap<String, String> arg0, PrintWriter out) throws Exception
    {
        log.fine("Test log message - FINE level");
        log.info("Test log message - INFO level");
        log.warning("Test log message - WARNING level");
        log.severe("Test log message - SEVERE level");
        
        throw new NullPointerException("Test exception message - not a real error");
    }
}