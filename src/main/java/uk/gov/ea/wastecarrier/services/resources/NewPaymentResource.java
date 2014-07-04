package uk.gov.ea.wastecarrier.services.resources;

import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.core.Payment;

import com.yammer.metrics.annotation.Timed;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.util.logging.Logger;

/**
 * This class represents the payment details URL (defined at @Path) and associated operations for Getting a 
 * new Payment. This class may not be needed depending on how the rails creates the initial model.
 * 
 */
@Path("/"+Registration.COLLECTION_NAME+"/{id}/"+Payment.COLLECTION_NAME+"/new.json")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NewPaymentResource
{
	
	private Logger log = Logger.getLogger(NewPaymentResource.class.getName());
	
	/**
	 * 
	 */
	public NewPaymentResource()
	{
	}
	
	/**
     * This retrieves and empty payment details from the services
     * 
     * @return if found returns a Payment object from the Java, otherwise throws WebApplicationException NOT_FOUND
     */
    @GET
    @Timed
    public Payment newPayment() 
    {	
    	log.info("Get Method Detected, Returning empty payment details");
    	
    	return new Payment();
    }

}
