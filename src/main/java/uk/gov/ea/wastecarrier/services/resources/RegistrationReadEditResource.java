package uk.gov.ea.wastecarrier.services.resources;

import com.codahale.metrics.annotation.Timed;
import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.SettingsConfiguration;
import uk.gov.ea.wastecarrier.services.core.MetaData;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.core.Settings;
import uk.gov.ea.wastecarrier.services.core.User;
import uk.gov.ea.wastecarrier.services.helper.DatabaseHelper;
import uk.gov.ea.wastecarrier.services.helper.PaymentHelper;
import uk.gov.ea.wastecarrier.services.helper.RegistrationHelper;
import uk.gov.ea.wastecarrier.services.dao.RegistrationDao;
import uk.gov.ea.wastecarrier.services.dao.UserDao;

import com.mongodb.DB;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import java.util.logging.Logger;

/**
 * This class represents a GET and PUT Resource provided as the @Path parameter.
 * Specifically serving the Edit and Update services
 *
 */
@Path("/" + Registration.COLLECTION_NAME + "/{id}.json")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RegistrationReadEditResource
{
    private DatabaseHelper databaseHelper;
    private RegistrationDao regDao;
    private UserDao userDao;
    private PaymentHelper paymentHelper;

    // Standard logging declaration
    private Logger log = Logger.getLogger(RegistrationReadEditResource.class.getName());

    public RegistrationReadEditResource(
            DatabaseConfiguration database, DatabaseConfiguration userDatabase, SettingsConfiguration settingConfig)
    {
        this.databaseHelper = new DatabaseHelper(database);
        this.regDao = new RegistrationDao(database);
        this.paymentHelper = new PaymentHelper(new Settings(settingConfig));
        this.userDao = new UserDao(userDatabase);
    }

    /**
     * This retrieves registration details from the database, given the id provided in the URL path, 
     * and returns the Registration object as JSON
     *
     * @param id the value of the {id} parameter provided in the URL, e.g. for /registrations/{id} path is /registrations/123 
     * @return if found returns a Registration object from the database, otherwise throws WebApplicationException NOT_FOUND
     */
    @GET
    @Timed
    public Registration getRegistration(@PathParam("id") String id)
    {
        log.info("Get Method Detected, attempt to return registration details for " + id);

        // Use id to lookup record in database return registration details
        DB db = databaseHelper.getConnection();
        if (db != null)
        {
            // Create MONGOJACK connection to the database
            JacksonDBCollection<Registration, String> registrations = JacksonDBCollection.wrap(
                    db.getCollection(Registration.COLLECTION_NAME), Registration.class, String.class);

            log.info("Searching for Registration ID: " + id);
            if (id.equalsIgnoreCase("new"))
            {
                // Return an empty registration object
                log.info("Returning an empty registration object.");
                Registration r = new Registration();
                r.setMetaData(new MetaData());
                return r;
            }
            try
            {
                Registration foundReg = registrations.findOneById(id);
                if (foundReg != null)
                {
                    log.info("Found Registration, CompanyName:" + foundReg.getCompanyName());

                    // Check if if Expired date is beyond today, and if so mark registration as EXPIRED
                    if (RegistrationHelper.hasExpired(foundReg))
                    {
                        // Set and update registration with updated values
                        foundReg = RegistrationHelper.setAsExpired(foundReg);
                        return regDao.update(foundReg);
                    }
                    return foundReg;
                }
                else
                {
                    log.info("Valid ID format, but Cannot find Registration for ID: " + id);
                    throw new WebApplicationException(Status.NOT_FOUND);
                }
            }
            catch (IllegalArgumentException e)
            {
                log.severe("Caught exception: " + e.getMessage() + " - Cannot find Registration ID: " + id);
                throw new WebApplicationException(Status.NOT_FOUND);
            }
        }
        else
        {
            log.severe("Get registration - Could not obtain connection to MongoDB!");
            throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
        }
    }

    /**
     * This PUT operation updates the registration details in the database given the parameters provided.
     *
     * @param id in the URL path of the Registration to update
     * @param reg Registration object to be updated
     * @return
     */
    @PUT
    @Timed
    public Registration update(@PathParam("id") String id, @Valid Registration reg)
    {
        log.info("PUT Update Registration, for ID:" + id);
        Registration savedObject = null;

        DB db = databaseHelper.getConnection();
        if (db != null)
        {
            // Create MONGOJACK connection to the database
            JacksonDBCollection<Registration, String> registrations = JacksonDBCollection.wrap(
                    db.getCollection(Registration.COLLECTION_NAME), Registration.class, String.class);

            // If object found
            WriteResult<Registration, String> result = registrations.updateById(id, reg);
            log.fine("Found result: '" + result + "' ");

            log.info("Registration updated successfully in MongoDB for ID:" + id);
            try
            {
                //Update the registration status, if appropriate
                Registration registration = regDao.find(id);
                User user = userDao.getUserByEmail(registration.getAccountEmail());

                if (paymentHelper.isReadyToBeActivated(registration, user))
                {
                    registration = paymentHelper.setupRegistrationForActivation(registration);
                    try
                    {
                        savedObject = regDao.update(registration);
                    }
                    catch (Exception e)
                    {
                        // TODO: Need to handle this better because if the registration update
                        // fails as the first update worked?
                        log.severe("Error while updating registration after update with ID " + registration.getId() + " in MongoDB.");
                        throw new WebApplicationException(Status.NOT_MODIFIED);
                    }
                }

                // Make a second request for the updated full registration details to be returned
                savedObject = registrations.findOneById(id);
                log.fine("Found Updated Registration, Details include:- CompanyName:" + savedObject.getCompanyName());

                return savedObject;
            }
            catch (IllegalArgumentException e)
            {
                log.severe("Catching exception while trying to update registration: " + e.getMessage());
                log.warning("Cannot find Registration ID: " + id);
                throw new WebApplicationException(Status.NOT_FOUND);
            }
        }
        else
        {
            log.severe("Could not obtain a connection to MongoDB! Cannot update registration in the database!");
            throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
        }
    }

    /**
     * Delete Registration from the database and Elastic Search given the ID provided
     *
     * @param id the ID of the registration to remove
     * @return null, if completed successfully, otherwise throws an Exception
     */
    @DELETE
    @Timed
    public Registration deleteRegistration(@PathParam("id") String id)
    {
        log.info("DELETE Registration, for ID:" + id);

        // Use id to lookup record in database return registration details
        DB db = databaseHelper.getConnection();
        if (db != null)
        {
            // Create MONGOJACK connection to the database
            JacksonDBCollection<Registration, String> registrations = JacksonDBCollection.wrap(
                    db.getCollection(Registration.COLLECTION_NAME), Registration.class, String.class);

            log.info("Searching for ID: " + id);
            try
            {
                Registration foundReg = registrations.findOneById(id);
                WriteResult<Registration, String> result;
                if (foundReg != null)
                {
                    log.info("Found Registration, CompanyName:" + foundReg.getCompanyName());

                    // Remove Found Registration from Database
                    registrations.removeById(id);
                    log.info("Deleted registration with ID:" + foundReg.getId() + " from MongoDB");

                    // Operation completed
                    return null;
                }
                log.info("Valid ID format, but Cannot find Registration for ID: " + id);

                throw new WebApplicationException(Status.NOT_FOUND);
            }
            catch (IllegalArgumentException e)
            {
                log.severe("Caught exception: " + e.getMessage() + " Cannot find Registration ID: " + id);
                throw new WebApplicationException(Status.NOT_FOUND);
            }
        }
        else
        {
            log.severe("Could not obtain connection to MongoDB. Cannot delete registration with id = " + id);
            throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
        }
    }
}
