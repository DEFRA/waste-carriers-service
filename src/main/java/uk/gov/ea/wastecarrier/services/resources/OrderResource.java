package uk.gov.ea.wastecarrier.services.resources;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.Order;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.mongoDb.OrdersMongoDao;

import com.yammer.metrics.annotation.Timed;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.util.Date;
import java.util.logging.Logger;

/**
 * Resource for accessing and updating individual orders within a registration.
 * 
 */
@Path("/"+Registration.COLLECTION_NAME+"/{registrationId}/"+Order.COLLECTION_NAME+"/{id}.json")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource
{
	
	private OrdersMongoDao dao;

	private Logger log = Logger.getLogger(OrderResource.class.getName());
	
	/**
	 * 
	 * @param database
	 */
	public OrderResource(DatabaseConfiguration database)
	{
		dao = new OrdersMongoDao(database);
	}
	

	/**
	 * Update the given Order within the Registration
	 * @param registrationId
	 * @param id
	 * @param order
	 * @return
	 */
	@PUT
	@Timed
    public Order updateOrder(@PathParam("registrationId") String registrationId, @PathParam("id") String id, Order order)
    {
		log.info("PUT method on the order. Updating the Order in the database.");
		order.setDateLastUpdated(new Date());
    	return dao.updateOrder(registrationId, id, order);
    }
}
