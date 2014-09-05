package uk.gov.ea.wastecarrier.services.resources;

import com.google.common.base.Optional;
import com.mongodb.MongoException;
import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.Payment;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;
import uk.gov.ea.wastecarrier.services.mongoDb.PaymentSearch;
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
        List<Registration> searchresults;

        try {
            ReportingHelper search = new ReportingHelper(new QueryHelper(this.databaseHelper));
            search.fromDate = from;
            search.toDate = until;
            search.route = route;
            search.status = status;
            search.businessType = businessType;
            search.tier = tier;
            search.declaredConvictions = declaredConvictions;
            search.criminallySuspect = criminallySuspect;

            searchresults = search.getRegistrations();

            if (searchresults.size() == 0) {
                log.info("No results found - returning empty list");
            }
        } catch (MongoException e) {
            log.severe("Database not found, check the database is running");
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
        }

        return searchresults;
    }

    @GET
    @Path("/" + Payment.COLLECTION_NAME)
    public List<Registration> getRegistrations(
            @QueryParam("from") Optional<String> from,
            @QueryParam("until") Optional<String> until,
            @QueryParam("paymentStatus[]") Set<String> paymentStatuses,
            @QueryParam("paymentType[]") Set<String> paymentTypes,
            @QueryParam("chargeType[]") Set<String> chargeTypes
    ) {

        log.fine("Get Method Detected at /query/payments");
        List<Registration> searchResults;

        try {
            PaymentSearch search = new PaymentSearch(new QueryHelper(this.databaseHelper));
            search.fromDate = from;
            search.toDate = until;
            search.paymentStatuses = paymentStatuses;
            search.paymentTypes = paymentTypes;
            search.chargeTypes = chargeTypes;

            searchResults = search.getRegistrations();

            if (searchResults.size() == 0) {
                log.info("No results found - returning empty list");
            }
        } catch (MongoException e) {
            log.severe("Database not found, check the database is running");
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
        }

        return searchResults;
    }
}
