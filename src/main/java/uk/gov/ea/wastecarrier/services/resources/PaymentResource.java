package uk.gov.ea.wastecarrier.services.resources;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.SettingsConfiguration;
import uk.gov.ea.wastecarrier.services.core.MetaData;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.core.Payment;
import uk.gov.ea.wastecarrier.services.core.Settings;

import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;
import uk.gov.ea.wastecarrier.services.mongoDb.PaymentsMongoDao;
import uk.gov.ea.wastecarrier.services.mongoDb.RegistrationsMongoDao;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import java.util.Calendar;
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
	private RegistrationsMongoDao regDao;
	private Settings settings;
	
	private Logger log = Logger.getLogger(PaymentResource.class.getName());
	
	/**
	 * 
	 * @param database
	 */
	public PaymentResource(DatabaseConfiguration database, SettingsConfiguration settingConfig)
	{
		dao = new PaymentsMongoDao(database);
		regDao = new RegistrationsMongoDao(new DatabaseHelper(database));
		settings = new Settings(settingConfig);
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
	 * @throws WebApplicationException NOT_MODIFIED - If the payment with a matching order code exists
	 */
	@POST
	public Payment submitPayment(@PathParam("id") String registrationId, @Valid Payment payment)
	{
		log.info("POST METHOD detected in submitPayment() method for payment creation");
		
		/*
		 * Update payment request with the payment entered date
		 */
		payment.setDateEntered(new Date());
		
		/*
		 * Add payment to database
		 */
		Payment resultPayment = dao.addPayment(registrationId, payment);
		
		/*
		 * Update the registration status, if appropriate
		 */
		Registration registration = regDao.getRegistration(registrationId);
		if (registration.getFinanceDetails().getBalance() == 0 && registration.getCriminallySuspect() == false)
		{
			//make registration active
			MetaData md = registration.getMetaData();
			md.setLastModified(MetaData.getCurrentDateTime());
			
			// Update Activation status and time
			if (!MetaData.RegistrationStatus.ACTIVE.equals(md.getStatus()))
			{
				md.setDateActivated(MetaData.getCurrentDateTime());
			}
			md.setStatus(MetaData.RegistrationStatus.ACTIVE);
			
			registration.setMetaData(md);
			   
			//set appropriate metadata
			md.setDateActivated("today");
			   
			//set expiry date
			Calendar cal = Calendar.getInstance();
			String[] regPeriodList = settings.getRegistrationPeriod().split(" ");
			int length = Integer.parseInt(regPeriodList[0]);
			String type = regPeriodList[0];			
			if (type == "YEARS")
			{
				cal.add(Calendar.YEAR, length);
			}
			else if (type == "MONTHS")
			{
				cal.add(Calendar.MONTH, length);
			}
			else if (type == "DAYS")
			{
				cal.add(Calendar.DAY_OF_MONTH, length);
			}
			Date expiryDate = cal.getTime();
			registration.setExpiresOn(expiryDate);
		}
		
		return resultPayment;
	}
}
