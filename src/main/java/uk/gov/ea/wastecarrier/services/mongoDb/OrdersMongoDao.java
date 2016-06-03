package uk.gov.ea.wastecarrier.services.mongoDb;

import java.util.UUID;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import net.vz.mongodb.jackson.DBUpdate;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.WriteResult;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.FinanceDetails;
import uk.gov.ea.wastecarrier.services.core.Order;
import uk.gov.ea.wastecarrier.services.core.Registration;

public class OrdersMongoDao
{
    /** logger for this class. */
    private static Logger log = Logger.getLogger(OrdersMongoDao.class.getName());
    
    /** The database helper. */
    private DatabaseHelper databaseHelper;
    
    /**
     * Constructor with arguments
     * @param databaseHelper the DatabaseHelper
     */
    public OrdersMongoDao(DatabaseConfiguration database)
    {
        log.fine("Constructing DAO with databaseHelper.");
        this.databaseHelper = new DatabaseHelper(database);
    }

    public Order addOrder(String registrationId, Order order)
    {
        log.info("Adding order to registration with id = " + registrationId);
        DB db = databaseHelper.getConnection();
        if (db != null)
        {
            if (!db.isAuthenticated())
            {
                throw new WebApplicationException(Status.FORBIDDEN);
            }
            
            // Generate order id
            order.setOrderId(UUID.randomUUID().toString());
            
            /*
             * Create MONGOJACK connection to the database
             */
            JacksonDBCollection<Registration, String> registrations = JacksonDBCollection.wrap(
                    db.getCollection(Registration.COLLECTION_NAME), Registration.class, String.class);
            
            /*
             * Before adding the order, verify that this order (identified by its orderCode)
             * has not already been received.
             */
            
            Registration registration = registrations.findOneById(registrationId);
            Order existingOrder = registration.getFinanceDetails().getOrderForOrderCode(order.getOrderCode());
            if (existingOrder != null)
            {
                // TT: Changed this logging level to SEVERE as I suspect we ought
                // to handle this differently?
                log.severe("The registration already has a order for order code " + order.getOrderCode() + ". Not adding order again.");
                return order;
            }
            
            /*
             * Update registration with order information into the database
             */
            WriteResult<Registration, String> result = registrations.updateById(registrationId,
                    DBUpdate.push(FinanceDetails.COLLECTION_NAME + "." + Order.COLLECTION_NAME, order));
            
            if (result.getError() == null)
            {
                // Find registration after adding order, This updates the balance in the database
                Registration foundReg = registrations.findOneById(registrationId);
                result = registrations.updateById(registrationId, foundReg);
                if (result.getError() == null)
                {
                    return order;
                }
                else
                {
                    log.severe("Error occured while updating registration after orders placed, " + result.getError());
                    throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
                }
            }
            else
            {
                log.severe("Error occured while updating registration with a order, " + result.getError());
                throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
            }
        }
        else
        {
            log.severe("Could not establish database connection to MongoDB! Check the database is running");
            throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
        }
    }
    
    
    public Order updateOrder(String registrationId, String orderId, Order order)
    {
        log.info("Updating order " + orderId + " of registration id = " + registrationId);
        DB db = databaseHelper.getConnection();
        if (db == null)
        {
            log.severe("Could not establish database connection to MongoDB! Check the database is running");
            throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
        }

        if (!db.isAuthenticated())
        {
            throw new WebApplicationException(Status.FORBIDDEN);
        }
        
        /*
         * Create MONGOJACK connection to the database
         */
        JacksonDBCollection<Registration, String> registrations = JacksonDBCollection.wrap(
                db.getCollection(Registration.COLLECTION_NAME), Registration.class, String.class);
        
        /*
         * Update registration with order information into the database
         * Using the positional operator ('.$.') to update the given Order sub-document within the orders array/list of the registration
         */
        
        Registration registration = registrations.findOneById(registrationId);
        if (registration == null)
        {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        
        // New way to update an order, by finding the order to update and calling DBUpdate.set directly
        DBObject query1 = new BasicDBObject();
        query1.put("regIdentifier", registration.getRegIdentifier());
        query1.put("financeDetails.orders.orderId", orderId);
        
        // Update order attributes
        WriteResult<Registration, String> result;
        result = registrations.update(query1, DBUpdate.set(FinanceDetails.COLLECTION_NAME + "." + Order.COLLECTION_NAME + ".$", order));
        
        if (result.getError() == null)
        {
            // Find registration after adding order
            Registration foundReg = registrations.findOneById(registrationId);
            result = registrations.updateById(registrationId, foundReg);
            if (result.getError() == null)
            {
                return order;
            }
            else
            {
                log.severe("Error occured while updating registration after updating order, " + result.getError());
                throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
            }
        }
        else
        {
            log.severe("Error occured while updating registration with a order, " + result.getError());
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
    }

}
