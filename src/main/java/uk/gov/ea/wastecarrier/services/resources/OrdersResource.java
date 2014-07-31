package uk.gov.ea.wastecarrier.services.resources;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.Order;
import uk.gov.ea.wastecarrier.services.core.Registration;

import uk.gov.ea.wastecarrier.services.mongoDb.OrdersMongoDao;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import java.util.logging.Logger;

/**
 * This class represents the order details URL (defined at @Path) and associated operations for POSTing
 * orders
 * 
 */
@Path("/"+Registration.COLLECTION_NAME+"/{registrationId}/"+Order.COLLECTION_NAME+".json")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrdersResource
{	
	private OrdersMongoDao dao;
	
	private Logger log = Logger.getLogger(OrdersResource.class.getName());
	
	/**
	 * 
	 * @param database
	 */
	public OrdersResource(DatabaseConfiguration database)
	{
		dao = new OrdersMongoDao(database);
	}

	/**
	 * POSTs a new order and updates the registration from the ID provided into the database
	 * 
	 * @param registrationId the id of the registration to update in the database
	 * @param order the order to save in the database
	 * @return the updated Order object after it was saved in the database
	 * @throws WebApplicationException SERVICE_UNAVAILABLE - If the database is not available
	 * @throws WebApplicationException FORBIDDEN - If access to the database is not allowed
	 * @throws WebApplicationException INTERNAL_SERVER_ERROR - If an error has occurred
	 */
	@POST
	public Order submitOrder(@PathParam("registrationId") String registrationId, @Valid Order order)
	{
		log.info("POST METHOD detected in submitOrder() method. Creating/adding order in database.");
		return dao.addOrder(registrationId, order);
	}
}
