package uk.gov.ea.wastecarrier.services.resources;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.SettingsConfiguration;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.core.Payment;
import uk.gov.ea.wastecarrier.services.core.Settings;
import uk.gov.ea.wastecarrier.services.core.User;

import uk.gov.ea.wastecarrier.services.helper.PaymentHelper;
import uk.gov.ea.wastecarrier.services.dao.PaymentDao;
import uk.gov.ea.wastecarrier.services.dao.RegistrationDao;
import uk.gov.ea.wastecarrier.services.dao.UserDao;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

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
    private PaymentDao dao;
    private PaymentHelper paymentHelper;
    private RegistrationDao regDao;
    private UserDao userDao;

    private Logger log = Logger.getLogger(PaymentResource.class.getName());

    /**
     *
     * @param database
     */
    public PaymentResource(DatabaseConfiguration database, DatabaseConfiguration userDatabase,
            SettingsConfiguration settingConfig)
    {
        dao = new PaymentDao(database);
        regDao = new RegistrationDao(database);
        paymentHelper = new PaymentHelper(new Settings(settingConfig));
        userDao = new UserDao(userDatabase);
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
         *
         */
        Registration registration = regDao.find(registrationId);
        User user = userDao.findByEmail(registration.getAccountEmail());
        if (paymentHelper.isReadyToBeActivated(registration, user) || paymentHelper.isReadyToBeRenewed(registration))
        {
            if (paymentHelper.isReadyToBeRenewed(registration)) registration.setRenewalRequested(null);
            registration = paymentHelper.setupRegistrationForActivation(registration);
        }
        try
        {
            regDao.update(registration);
        }
        catch(Exception e)
        {
            /*
             * TODO: Need to handle this better because if the registration update
             * fails we should roll-back the payment?
             */
            dao.deletePayment(resultPayment);
            log.severe("Error while updating registration after payment with ID " + registration.getId() + " in MongoDB.");
            throw new WebApplicationException(Status.NOT_MODIFIED);
        }

        return resultPayment;
    }
}
