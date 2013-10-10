package uk.gov.ea.wastecarrier.services.resources;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.MessageQueueConfiguration;
import uk.gov.ea.wastecarrier.services.core.MetaData;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;

import com.mongodb.DB;
import com.yammer.metrics.annotation.Timed;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.WriteResult;

import java.util.logging.Logger;

/**
 * This class represents a GET and PUT Resource provided as the @Path parameter. 
 * Specifically serving the Edit and Update services
 * 
 * @author Steve stevenr@aptosolutions.co.uk
 *
 */
@Path("/registrations/{id}.json")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RegistrationReadEditResource
{
	
    private final String template;
    private final String defaultName;
    private MessageQueueConfiguration messageQueue;
    private DatabaseHelper databaseHelper;
    
    // Standard logging declaration
    private Logger log = Logger.getLogger(RegistrationReadEditResource.class.getName());

    /**
     * 
     * @param template
     * @param defaultName
     * @param mQConfig
     * @param database
     */
    public RegistrationReadEditResource(String template, String defaultName, MessageQueueConfiguration mQConfig,
    		DatabaseConfiguration database)
    {
        this.template = template;
        this.defaultName = defaultName;
        this.messageQueue = mQConfig;
        
        log.fine("> template: " + this.template);
    	log.fine("> defaultName: " + this.defaultName);
    	log.fine("> messageQueue: " + this.messageQueue);

        this.databaseHelper = new DatabaseHelper(database);
    }

    /**
     * This retrieves registration details from the database, given the id provided in the URL path, 
     * and returns the Registration object as JSON
     * 
     * @param id the value of the {id} parameter provided in the URL, e.g. for /registrations/{id} path is /registrations/123 
     * @return if found returns a Registration object from the database, otherwise throws WebApplicationException NOT_FOUND
     */
    @GET
    @Timed
    public Registration getRegistration(@PathParam("id") String id) 
    {	
    	log.info("Get Method Detected, attempt to return registration details for " + id);
    	
    	// Use id to lookup record in database return registration details
    	DB db = databaseHelper.getConnection();
		if (db != null)
		{
			// Create MONGOJACK connection to the database
			JacksonDBCollection<Registration, String> registrations = JacksonDBCollection.wrap(
					db.getCollection(Registration.COLLECTION_NAME), Registration.class, String.class);
			
			log.info("Searching for Registration ID: " + id);
			try
			{
				Registration foundReg = registrations.findOneById(id);
				if (foundReg != null)
				{
					log.info("Found Registration, CompanyName:" + foundReg.getCompanyName());
					return foundReg;
				}
				log.info("Valid ID format, but Cannot find Registration for ID: " + id);
				throw new WebApplicationException(Status.NOT_FOUND);
			}
			catch (IllegalArgumentException e)
			{
				log.warning("Cannot find Registration ID: " + id);
				throw new WebApplicationException(Status.NOT_FOUND);
			}
		}
		else
		{
			throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
		}
    }
    
    /**
     * This PUT operation updates the registration details in the database given the parameters provided.
     * 
     * @param id in the URL path of the Registration to update
     * @param reg Registration object to be updated
     * @return
     */
    @PUT
	public Registration update(@PathParam("id") String id, @Valid Registration reg)
	{
    	log.info("PUT Update Registration, for ID:" + id);
    	Registration savedObject = null;
    	
    	DB db = databaseHelper.getConnection();
		if (db != null)
		{
			// Create MONGOJACK connection to the database
			JacksonDBCollection<Registration, String> registrations = JacksonDBCollection.wrap(
					db.getCollection(Registration.COLLECTION_NAME), Registration.class, String.class);
			
			// Update Registration MetaData last Modified Time
			MetaData md = reg.getMetaData();
			md.setLastModified(MetaData.getCurrentDateTime());
			reg.setMetaData(md);
			
			WriteResult<Registration, String> result = registrations.updateById(id, reg);
			log.fine("Found result: '" + result + "' " );
			
			if (!String.valueOf("").equals(result.getError()))
			{
				log.info("Registration Updated for ID:" + id);
				try
				{
					// Make a second request for the updated full registration details to be returned
					savedObject = registrations.findOneById(id);
					log.fine("Found Updated Registration, Details include:- CompanyName:" + savedObject.getCompanyName());
					return savedObject;
				}
				catch (IllegalArgumentException e)
				{
					log.warning("Cannot find Registration ID: " + id);
					throw new WebApplicationException(Status.NOT_FOUND);
				}
			}
			else
			{
				throw new WebApplicationException(Status.NOT_MODIFIED);
			}
		}
		else
		{
			throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
		}
	}
}
