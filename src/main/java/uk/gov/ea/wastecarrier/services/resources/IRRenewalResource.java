package uk.gov.ea.wastecarrier.services.resources;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.core.Registration.RegistrationTier;
import uk.gov.ea.wastecarrier.services.core.irdata.CompanyIRData;
import uk.gov.ea.wastecarrier.services.core.irdata.IRData;
import uk.gov.ea.wastecarrier.services.core.irdata.IndividualIRData;
import uk.gov.ea.wastecarrier.services.core.irdata.PartnersIRData;
import uk.gov.ea.wastecarrier.services.core.irdata.PublicBodyIRData;
import uk.gov.ea.wastecarrier.services.mongoDb.IRRenewalMongoDao;

import com.google.common.base.Optional;
import com.yammer.metrics.annotation.Timed;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import java.util.logging.Logger;

import com.mongodb.DB;
import net.vz.mongodb.jackson.DBQuery;
import net.vz.mongodb.jackson.JacksonDBCollection;

import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;

/**
 * Resource for accessing and updating individual orders within a registration.
 *
 */
@Path("/irrenewals.json")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class IRRenewalResource
{
    // Private members set in the constructor.  They provide access to Mongo
    // documents.
    private final IRRenewalMongoDao dao;
    private final DatabaseHelper dbHelper;

    // Logging capability.
    private final Logger log = Logger.getLogger(IRRenewalResource.class.getName());

    /**
     * Constructor.  Create objects to access Mongo documents later.
     * @param database  The database configuration to use.
     */
    public IRRenewalResource(DatabaseConfiguration database)
    {
        dao = new IRRenewalMongoDao(database);
        dbHelper = new DatabaseHelper(database);
    }

    /**
     * Get the IR renewal Registration for the original registration number provided
     * @param registrationNumber
     * @return
     */
    @GET
    @Timed
    public Registration getIRRenewal(@QueryParam("irNumber") Optional<String> registrationNumber)
    {
        log.info("GET method on the getIRRenewal.");
        if (registrationNumber.isPresent())
        {
            String regNumber = registrationNumber.get();
            log.info("Searching for IR registration number: " + regNumber);
            
            // Find generic IR data
            IRData irData = dao.findIRData(regNumber);
            if (irData != null)
            {
                Registration r = new Registration();
                
                // Set generic registration information
                r.setOriginalRegistrationNumber(regNumber);
                r.setTier(RegistrationTier.UPPER);
                switch (irData.getTrueRegistrationType())
                {
                    case CARRIER:
                        r.setRegistrationType("carrier_dealer");
                        break;
                    case BROKER:
                        r.setRegistrationType("broker_dealer");
                        break;
                    case CARRIER_AND_BROKER:
                        r.setRegistrationType("carrier_broker_dealer");
                        break;
                    default:
                        break;
                }
                updateCarrierTypeFromMigratedRegistrations(r);

                r.setOriginalDateExpiry(irData.getExpiryDate());

                log.fine("IR get type: " + irData.getIrType());
                switch (irData.getIrType())
                {
                    case COMPANY:
                        CompanyIRData companyData = dao.findOneCompanyIRData(regNumber);
                        // Add additional type specific information
                        if (!companyData.getCompanyName().isEmpty())
                        {
                            r.setCompanyName(companyData.getCompanyName());
                        }
                        else
                        {
                            r.setCompanyName(companyData.getTradingName());
                        }
                        r.setBusinessType("limitedCompany");
                        r.setCompanyNo(companyData.getCompanyNumber());
                        break;
                    case INDIVIDUAL:
                        IndividualIRData individualData = dao.findOneIndividualIRData(regNumber);
                        // Add additional type specific information
                        int firstSpace = individualData.getPermitHolderName().indexOf(" ");
                        if (firstSpace != -1)
                        {
                            r.setFirstName(individualData.getPermitHolderName().substring(0, firstSpace));
                            r.setLastName(individualData.getPermitHolderName().substring(firstSpace+1));
                        }
                        r.setCompanyName(individualData.getTradingName());
                        r.setBusinessType("soleTrader");
                        break;
                    case PARTNER:
                        PartnersIRData partnersData = dao.findOnePartnersIRData(regNumber);
                        // Add additional type specific information
                        if (!partnersData.getTradingName().isEmpty())
                        {
                            r.setCompanyName(partnersData.getTradingName());
                        }
                        else
                        {
                            r.setCompanyName(partnersData.getPartnershipName());
                        }
                        r.setBusinessType("partnership");
                        break;
                    case PUBLIC_BODY:
                        PublicBodyIRData publicBodyData = dao.findOnePublicBodyIRData(regNumber);
                        // Add additional type specific information
                        if (!publicBodyData.getTradingName().isEmpty())
                        {
                            r.setCompanyName(publicBodyData.getTradingName());
                        }
                        else
                        {
                            r.setCompanyName(publicBodyData.getPartyName());
                        }
                        r.setBusinessType("publicBody");
                        break;
                    default:
                        log.warning("Invalid IR data type found. Unable to determine true, sub-class as type: " + irData.getIrType() + " is not valid.");
                        throw new WebApplicationException(Status.BAD_REQUEST);
                }
                return r;
            }
            log.info("Failed to find company with number:" + registrationNumber);
            throw new WebApplicationException(Status.NO_CONTENT);
        }
        else
        {
            // Required search parameter not found
            throw new WebApplicationException(Status.BAD_REQUEST);
        }
    }
    
    /**
     * Attempt to update the Carrier Type (aka "Registration Type") for an 
     * IR Renewal with the latest data from the old registration.  This only
     * became necessary in March 2016, when active IR registrations were
     * migrated into this service, and must be done so that we don't charge the
     * customer twice for changing their Carrier Type.
     * @param templateReg The template registration to update.
     */
    private void updateCarrierTypeFromMigratedRegistrations(Registration templateReg)
    {
        try
        {
            log.info("Attempting to check for updated Carrier Type in IR Renewal");
            
            DB db = dbHelper.getConnection();
            if (db == null)
            {
                throw new RuntimeException("No access to Mongo database");
            }
            if (!db.isAuthenticated())
            {
                throw new RuntimeException("Not authenticated against database");
            }

            // Create MONGOJACK connection to the Registrations Mongo document collection.
            JacksonDBCollection<Registration, String> registrations = JacksonDBCollection.wrap(
                    db.getCollection(Registration.COLLECTION_NAME), Registration.class, String.class);

            // Try to find a single registration matching the IR number.
            Registration expiringReg = registrations.findOne(
                    DBQuery.is("regIdentifier", templateReg.getOriginalRegistrationNumber().trim()));

            // Update the new registration with the current (old) Carrier Type.
            if (expiringReg == null)
            {
                throw new RuntimeException("Cannot find old registration with IR Renewal number");
            }
            String expiringRegRegistrationType = expiringReg.getRegistrationType();
            if ((expiringRegRegistrationType == null) || expiringRegRegistrationType.isEmpty())
            {
                throw new RuntimeException("Old registration does not have the Carrier Type set");
                
            }
            log.info(String.format("Updating IR Renewal Carrier Type: was %s, now %s",
                    templateReg.getRegistrationType(), expiringRegRegistrationType));
            templateReg.setRegistrationType(expiringRegRegistrationType);

            // Carrier type has been successfully updated.
        }
        catch (Exception e)
        {
            log.warning(String.format("Unexpected error whilst trying to update IR Renewal Carrier type: %s", e.getMessage()));
        }
    }
}
