package uk.gov.ea.wastecarrier.services.resources;

import uk.gov.ea.wastecarrier.services.core.Order;
import uk.gov.ea.wastecarrier.services.core.Registration;

import com.yammer.metrics.annotation.Timed;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.util.logging.Logger;

/**
 * This class represents the order details URL (defined at @Path) and associated operations for POSTing
 * orders
 * 
 */
@Path("/"+Registration.COLLECTION_NAME+"/{id}/"+Order.COLLECTION_NAME+"/new.json")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NewOrderResource
{
	private Logger log = Logger.getLogger(NewOrderResource.class.getName());
	
	/**
	 * 
	 * @param database
	 */
	public NewOrderResource()
	{
	}
	
	/**
     * This creates a new order object,
     * and returns the order object as JSON
     * 
     * @return new Order object
     */
    @GET
    @Timed
    public Order newOrder() 
    {	
    	log.info("Get Method Detected, Returning empty order details");
    	return new Order();
    }

}
