package uk.gov.ea.wastecarrier.services.mongoDb;

import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import net.vz.mongodb.jackson.DBUpdate;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.WriteResult;
import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.FinanceDetails;
import uk.gov.ea.wastecarrier.services.core.Payment;
import uk.gov.ea.wastecarrier.services.core.Registration;

import com.mongodb.DB;

/**
 * Data access operations for registration payments.
 *
 */
public class PaymentsMongoDao
{
    /** logger for this class. */
    private static Logger log = Logger.getLogger(PaymentsMongoDao.class.getName());
    
    /** The database helper. */
    private DatabaseHelper databaseHelper;
    
    /**
     * Constructor with arguments
     * @param databaseHelper the DatabaseHelper
     */
    public PaymentsMongoDao(DatabaseConfiguration database)
    {
        log.fine("Constructing DAO with databaseHelper.");
        this.databaseHelper = new DatabaseHelper(database);
    }
    
    /**
     * Before adding the payment, verify that this payment (identified by its orderCode)
     * has not already been received.
     *
     * @param registrationId
     * @param payment
     * @return true if payment valid, false otherwise
     */
    public boolean validatePayment(String registrationId, Payment payment)
    {
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
        
        Registration registration = registrations.findOneById(registrationId);
        FinanceDetails financeDetails = registration.getFinanceDetails();
        if (financeDetails != null)
        {
            Payment existingPayment = financeDetails.getPaymentForOrderCode(payment.getOrderKey());
            if (existingPayment != null)
            {
                log.info("The registration already has a payment for order code " + payment.getOrderKey() + ". Not adding payment again.");
                return false;
            }
        }
        return true;
    }
    
    /**
     * Insert payment into registration details into the database
     *
     * @param registrationId the registration with which to add the payment
     * @param payment the payment to add
     * @return the updated payment
     */
    public Payment addPayment(String registrationId, Payment payment)
    {
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
         * Before adding the payment, verify that this payment (identified by its orderCode)
         * has not already been received.
         */
        if (!this.validatePayment(registrationId, payment))
        {
            throw new WebApplicationException(Status.NOT_MODIFIED);
        }
        
        /*
         * Update registration with payment information into database
         */
        WriteResult<Registration, String> result = registrations.updateById(registrationId,
                DBUpdate.push(FinanceDetails.COLLECTION_NAME + "." + Payment.COLLECTION_NAME, payment));
        
        if (result.getError() == null)
        {
            return payment;
        }
        else
        {
            log.severe("Error occured while updating registration with a payment, " + result.getError());
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
    }

    public void deletePayment(Payment resultPayment)
    {
        // TODO Auto-generated method stub
        log.severe("Not implemented delete payment - part of the rollback");
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
}
