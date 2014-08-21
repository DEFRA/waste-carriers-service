package uk.gov.ea.wastecarrier.services.resources;

import com.google.common.base.Optional;
import com.mongodb.MongoException;
import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;
import uk.gov.ea.wastecarrier.services.mongoDb.QueryHelper;
import uk.gov.ea.wastecarrier.services.mongoDb.ReportingHelper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@Path("/query")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class QueryResource {

    private Logger log = Logger.getLogger(OrdersResource.class.getName());

    private final DatabaseHelper databaseHelper;

    public QueryResource(DatabaseConfiguration databaseConfiguration) {
        this.databaseHelper = new DatabaseHelper(databaseConfiguration);
    }

    @GET
    @Path("/" + Registration.COLLECTION_NAME)
    public List<Registration> getRegistrations(
            @QueryParam("from") Optional<String> from,
            @QueryParam("until") Optional<String> until,
            @QueryParam("route[]") Set<String> route,
            @QueryParam("status[]") Set<String> status,
            @QueryParam("businessType[]") Set<String> businessType,
            @QueryParam("tier[]") Set<String> tier,
            @QueryParam("declaredConvictions") Optional<String> declaredConvictions,
            @QueryParam("criminallySuspect") Optional<Boolean> criminallySuspect
    ) {

        log.fine("Get Method Detected at /query/registrations");
        List<Registration> reportResults;

        try {
            ReportingHelper helper = new ReportingHelper(new QueryHelper(this.databaseHelper));
            helper.fromDate = from;
            helper.toDate = until;
            helper.route = route;
            helper.status = status;
            helper.businessType = businessType;
            helper.tier = tier;
            helper.declaredConvictions = declaredConvictions;
            helper.criminallySuspect = criminallySuspect;

            reportResults = helper.getRegistrations();

            if (reportResults.size() == 0) {
                log.info("No results found - returning empty list");
            }
        } catch (MongoException e) {
            log.severe("Database not found, check the database is running");
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
        }

        return reportResults;
    }
}
