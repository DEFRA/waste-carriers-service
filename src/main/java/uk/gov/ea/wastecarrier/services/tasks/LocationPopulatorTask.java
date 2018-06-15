package uk.gov.ea.wastecarrier.services.tasks;

import com.google.common.collect.ImmutableMultimap;
import com.mongodb.DB;
import io.dropwizard.servlets.tasks.Task;
import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;
import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.Address;
import uk.gov.ea.wastecarrier.services.core.Location;
import uk.gov.ea.wastecarrier.services.core.MetaData;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.helper.DatabaseHelper;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import java.io.PrintWriter;
import java.util.logging.Logger;

/**
 * The Location Populator updates the records in the mongoDB database with XY Coordinates based on the
 * postcode value that exists for that record. (If an invalid postcode has been provided, X:1, Y:1 are used)
 *
 * To use this service call, E.g.
 * curl -X POST http://localhost:9091/tasks/location
 * Which performs a full location population for entries currently
 * in the database
 *
 * @author Steve
 *
 */
public class LocationPopulatorTask extends Task
{
    private final DatabaseHelper databaseHelper;
    private static Logger log = Logger.getLogger(LocationPopulatorTask.class.getName());
    private String pathToPostcodeFile;

    public LocationPopulatorTask(String name, DatabaseConfiguration database, String pathToPostcodeFile)
    {
        super(name);
        this.databaseHelper = new DatabaseHelper(database);
        this.pathToPostcodeFile = pathToPostcodeFile;
    }

    /**
     * Performs the  operation Used Via the administration ports to recreate the location coordinates
     * all of the registrations found in the mongo database
     *
     * Usage:
     * curl -X POST http://[SERVER]:[ADMINPORT]/tasks/[this.getName()]
     */
    @Override
    public void execute(ImmutableMultimap<String, String> arg0, PrintWriter out) throws Exception
    {
        out.append("Running Complete Location Population operation of Elastic Search records...\n");
        
        // Get All Registration records from the database
        DB db = this.databaseHelper.getConnection();
        if (db != null) {
            // Create MONGOJACK connection to the database
            JacksonDBCollection<Registration, String> registrations = JacksonDBCollection.wrap(
                    db.getCollection(Registration.COLLECTION_NAME), Registration.class, String.class);
            
            // Go to database, get list of registrations
            DBCursor<Registration> dbcur = registrations.find();
            log.info(String.format("Found: %s Matching criteria", dbcur.size()));
            
            PostcodeRegistryTask pr = new PostcodeRegistryTask(PostcodeRegistryTask.POSTCODE_FROM.FILE, pathToPostcodeFile);
            
            // for each registration, get out postcode
            for (Registration r : dbcur)
            {
                Address regAddress = r.getFirstAddressByType(Address.addressType.REGISTERED);
                
                if (regAddress != null)
                {
                    // Unfortunately we have addressMode hard coded> we could have put it into an enum but was a valid
                    // addressMode is null and/or blank!
                    if (regAddress.getAddressMode() == "manual-foreign")
                    {
                        log.info("Non-UK Address assumed as Postcode could not be found in the address, Using default location of X:1, Y:1");
                        regAddress.setLocation( new Location(1, 1));

                        // Update MetaData to include a message to state location information set to default
                        MetaData tmpMD = r.getMetaData();
                        tmpMD.setAnotherString("Non-UK Address Assumed");
                        r.setMetaData(tmpMD);
                    }
                    else
                    {
                        Double[] xyCoords = pr.getXYCoords(regAddress.getPostcode());
                        regAddress.setLocation( new Location( xyCoords[0], xyCoords[1]));
                    }

                    // Update database with XY information
                    try {
                        registrations.updateById(r.getId(), r);
                    } catch (Exception e) {
                        throw new WebApplicationException(Status.NOT_MODIFIED);
                    }

                    log.info(String.format("Updated Registration id: %s", r.getId()));
                }
                else
                {
                    log.warning(String.format("Registration with no REGISTERED address: %s, %s", r.getRegIdentifier(), r.getId()));
                }
            }
        }
        
        out.append("Done\n");
    }
}
