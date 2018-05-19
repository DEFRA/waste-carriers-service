package uk.gov.ea.wastecarrier.services.resources;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.SettingsConfiguration;
import uk.gov.ea.wastecarrier.services.core.Order;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.core.Settings;
import uk.gov.ea.wastecarrier.services.core.User;

import uk.gov.ea.wastecarrier.services.dao.OrderDao;
import uk.gov.ea.wastecarrier.services.helper.PaymentHelper;
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
    private OrderDao dao;
    private RegistrationDao regDao;
    private UserDao userDao;
    private PaymentHelper paymentHelper;
    
    private Logger log = Logger.getLogger(OrdersResource.class.getName());
    
    /**
     *
     * @param database
     */
    public OrdersResource(DatabaseConfiguration database, DatabaseConfiguration userDatabase,
            SettingsConfiguration settingConfig)
    {
        dao = new OrderDao(database);
        regDao = new RegistrationDao(database);
        userDao = new UserDao(userDatabase);
        paymentHelper = new PaymentHelper(new Settings(settingConfig));
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
        Order resultOrder = dao.addOrder(registrationId, order);
        
        /*
         * Update the registration status, if appropriate
         * 
         */
        Registration registration = regDao.find(registrationId);
        User user = userDao.getUserByEmail(registration.getAccountEmail());
        
        if (paymentHelper.isReadyToBeActivated(registration, user) )
        {
            registration = paymentHelper.setupRegistrationForActivation(registration);
            try
            {
                regDao.update(registration);
            }
            catch(Exception e)
            {
                /*
                 * TODO: Need to handle this better because if the registration update
                 * fails we should roll-back the order?
                 */
                log.severe("Error while updating registration after order with ID " + registration.getId() + " in MongoDB.");
                throw new WebApplicationException(Status.NOT_MODIFIED);
            }
        }
        
        return resultOrder;
    }
}
