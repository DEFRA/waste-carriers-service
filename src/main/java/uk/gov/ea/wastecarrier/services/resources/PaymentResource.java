package uk.gov.ea.wastecarrier.services.resources;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.core.Payment;

import uk.gov.ea.wastecarrier.services.mongoDb.PaymentsMongoDao;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import java.util.Date;
import java.util.logging.Logger;

/**
 * This class represents the payment details URL (defined at @Path) and associated operations for POSTing
 * payments
 * 
 */
@Path("/"+Registration.COLLECTION_NAME+"/{id}/"+Payment.COLLECTION_NAME+".json")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PaymentResource
{	
	private PaymentsMongoDao dao;
	
	private Logger log = Logger.getLogger(PaymentResource.class.getName());
	
	/**
	 * 
	 * @param database
	 */
	public PaymentResource(DatabaseConfiguration database)
	{
		dao = new PaymentsMongoDao(database);
	}

	/**
	 * POSTs a new payment and updates the registration from the ID provided into the database
	 * 
	 * @param registrationId the id of the registration to update in the database
	 * @param payment the payment to save in the database
	 * @return the updated Payment object after it was saved in the database
	 * @throws WebApplicationException SERVICE_UNAVAILABLE - If the database is not available
	 * @throws WebApplicationException FORBIDDEN - If access to the database is not allowed
	 * @throws WebApplicationException INTERNAL_SERVER_ERROR - If an error has occurred
	 */
	@POST
	public Payment submitPayment(@PathParam("id") String registrationId, Payment payment)
	{
		log.info("POST METHOD detected in submitPayment() method for payment creation");
		
		/*
		 * Update payment request with the payment entered date
		 */
		payment.setDateEntered(new Date());
		
		/*
		 * Add payment to database
		 */
		return dao.addPayment(registrationId, payment);
	}
}
