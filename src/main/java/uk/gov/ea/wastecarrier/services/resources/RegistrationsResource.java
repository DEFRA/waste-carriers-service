package uk.gov.ea.wastecarrier.services.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import com.mongodb.*;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;
import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.*;
import uk.gov.ea.wastecarrier.services.core.Registration.RegistrationTier;
import uk.gov.ea.wastecarrier.services.helper.DatabaseHelper;
import uk.gov.ea.wastecarrier.services.dao.RegistrationDao;
import uk.gov.ea.wastecarrier.services.helper.RegistrationHelper;
import uk.gov.ea.wastecarrier.services.tasks.PostcodeRegistry;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.util.*;
import java.util.logging.Logger;

/**
 * This class represents the registration details URL (defined at @Path) and associated operations for POSTing
 * registrations, and GETting the entire registration information
 *
 */
@Path("/"+Registration.COLLECTION_NAME+".json")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RegistrationsResource
{
    private final RegistrationDao dao;
    private final DatabaseHelper databaseHelper;
    private final PostcodeRegistry postcodeRegistry;

    // Standard logging declaration
    private Logger log = Logger.getLogger(RegistrationsResource.class.getName());

    /**
     *
     * @param database the DatabaseConfiguration
     */
    public RegistrationsResource(
            DatabaseConfiguration database,
            String postcodeFilePath) {
        this.databaseHelper = new DatabaseHelper(database);
        this.dao = new RegistrationDao(database);
        this.postcodeRegistry = new PostcodeRegistry(PostcodeRegistry.POSTCODE_FROM.FILE, postcodeFilePath);
    }

    @GET
    @Timed
    @Path("/{registrationNumber}")
    public Registration fetch(@PathParam("registrationNumber") String registrationNumber) {

        Registration reg = dao.findByRegIdentifier(registrationNumber);

        return reg;
    }

    /**
     * Creates an array of Query objects that represent the parameters passed in the URL,
     * ignoring any that are not present
     *
     * @param paramMap contains the various URL parameters that have been passed into this request
     * @return a Query[] list if available, otherwise returns an empty list
     */
    private Query[] createConditionalSearchParams(Map<String, Optional<String>> paramMap) {
        log.fine("Create conditional search paramters: " + paramMap.keySet().size());
        ArrayList<Query> newQueryList = new ArrayList<Query>();
        
        // Check if parameter exists and if so create a Database Query
        for (String keyName : paramMap.keySet())
        {
            Optional<String> param = paramMap.get(keyName);
            if (param.isPresent())
            {
                log.info( "param name: " + keyName + " value: " + param.get());
                Query paramQuery = DBQuery.is(keyName, param.get());
                newQueryList.add(paramQuery);
            }
        }
        
        // Convert the ArrayList to a Standard array[]
        Query[] queryList = new Query[newQueryList.size()];
        int counter = 0;
        for (Query q : newQueryList)
        {
            queryList[counter] = q;
            counter++;
        }
        
        return queryList;
    }

    /**
     * POSTs the full registration details to save registration details in the database
     *
     * @param reg the Registration details to save in the database
     * @return the updated Registration object after it was saved in the database
     * @throws WebApplicationException SERVICE_UNAVAILABLE - If the database is not available
     */
    @POST
    public Registration register(@Valid Registration reg) {
        log.info("POST METHOD detected in register() method for registration create.");
        Registration savedObject = null;

        DB db = databaseHelper.getConnection();
        if (db != null) {
            if (!reg.validateUuid()) {
                log.severe("New registration to be inserted is missing a uuid - preventing accidental duplicate inserts.");
                throw new WebApplicationException(Status.PRECONDITION_FAILED);
            }
            
            // Possible bug?  Some registrations get persisted to the database
            // with no value in the metaData.route field.  Try to block these.
            MetaData currentMetaData = reg.getMetaData();
            if ((currentMetaData == null) || (currentMetaData.getRoute() == null)) {
                log.severe("Incoming registration with missing route field");
                throw new WebApplicationException(Status.BAD_REQUEST);
            }
            
            /*
             * Insert registration details into the database
             */
            // Update Registration MetaData to include current time
            reg.setMetaData(new MetaData(MetaData.getCurrentDateTime(), "userDetailAddedAtRegistration", currentMetaData.getRoute()));

            // Update Registration Location to include location, derived from postcode
            Address regAddress = null;
            for (Iterator<Address> address = reg.getAddresses().iterator(); address.hasNext(); ) {
                Address thisAddress = address.next();
                if (thisAddress.getAddressType().equals(Address.addressType.REGISTERED)) {
                    regAddress = thisAddress;
                    break;
                }
            }
            if (regAddress != null) {
                Double[] xyCoords = postcodeRegistry.getXYCoords(regAddress.getPostcode());
                regAddress.setLocation( new Location( xyCoords[0], xyCoords[1]));
            } else {
                log.info("Non-UK Address assumed as Postcode could not be found in the registration, Using default location of X:1, Y:1");
                regAddress.setLocation( new Location(1, 1));
                
                // Update MetaData to include a message to state location information set to default
                MetaData tmpMD = reg.getMetaData();
                tmpMD.setAnotherString("Non-UK Address Assumed");
                reg.setMetaData(tmpMD);
            }
            
            // Update Registration to include sequential identifier.
            updateRegistrationIdentifier(reg, db);
            
            // If upper tier, assert that Finance Details are provided and that
            // they contain (at least) one order.
            if (RegistrationTier.UPPER.equals(reg.getTier())) {
                if ((reg.getFinanceDetails() == null) ||
                        (reg.getFinanceDetails().getOrders() == null) ||
                        (reg.getFinanceDetails().getOrders().size() < 1)) {
                    log.severe("Incoming upper-tier registration with no initial order present");
                    throw new WebApplicationException(Status.BAD_REQUEST);
                }
            }

            // If user has declared convictions or we have matched convictions
            // we need to add a conviction sign off record
            String declaredConvictions = reg.getDeclaredConvictions();
            if (declaredConvictions != null && declaredConvictions.equalsIgnoreCase("yes") || RegistrationHelper.hasUnconfirmedConvictionMatches(reg)) {
                List<ConvictionSignOff> signOffs = new ArrayList<ConvictionSignOff>();
                signOffs.add(new ConvictionSignOff("no", null, null));
                reg.setConviction_sign_offs(signOffs);
            }
            
            // Create MONGOJACK connection to the database
            JacksonDBCollection<Registration, String> registrations = JacksonDBCollection.wrap(
                    db.getCollection(Registration.COLLECTION_NAME), Registration.class, String.class);

            // Insert registration information into database
            WriteResult<Registration, String> result = registrations.insert(reg);

            // Get unique ID out of response, and find updated record
            String id = result.getSavedId();
            savedObject = registrations.findOneById(id);

            log.info("Found savedObject: '" + savedObject.getId() );

            // Return saved object to user (returned as JSON)
            return savedObject;
        } else {
            log.severe("Could not establish database connection to MongoDB! Check the database is running");
            throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
        }
    }

    /**
     * Update Registration to include a unique sequential ID.
     * Use the 'counters' collection and search for 'regid' and then increment the counter 'seq' by 1 every time
     *
     * @param reg the current Registration document
     * @param db the database connection to use
     */
    private void updateRegistrationIdentifier(Registration reg, DB db) {
        DBCollection col = db.getCollection(Registration.COUNTERS_COLLECTION_NAME);
        DBObject query = new BasicDBObject();
        query.put("_id", "regid");

        BasicDBObject incDocument =
                new BasicDBObject().append("$inc",
                new BasicDBObject().append("seq", 1));
        // Find the current latest sequence and update it
        DBObject dbObj = col.findAndModify(query, incDocument);
        if (dbObj == null) {
            // Try to Create/First Entry and then use if did not exist.
            BasicDBObject newDocument =
                    new BasicDBObject().append("_id", "regid")
                                       .append("seq", 1);
            com.mongodb.WriteResult wr = col.insert(newDocument);

            // Re-try find and modify
            dbObj = col.findAndModify(query, incDocument);
        }
        if (dbObj != null) {
            int sequentialNumber = (Integer) dbObj.get("seq");
            // Set the formatted identifier in the registration document
            reg.setRegIdentifier(getFormattedRegIdentifier(sequentialNumber, reg.getTier()));
        } else {
            // Counters collection cannot be found
            log.severe("Cannot find Counters table, and or cannot update it");
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Returns a formated unique string representing the registration identifier.
     * NOTE: THis is NOT the ID for the registration
     * @param sequentialNumber sequential integer of the registration counter
     * @param tier RegistrationTier enum for tier
     * @return String of the formatted registration, prefixed with Registration.REGID_PREFIX
     */
    private String getFormattedRegIdentifier(int sequentialNumber, Registration.RegistrationTier tier) {
        return Registration.REGID_PREFIX + tier.getPrefix() + Integer.toString(sequentialNumber);
    }
}
