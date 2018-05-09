package uk.gov.ea.wastecarrier.services.resources;

import com.google.common.base.Optional;
import com.mongodb.MongoException;
import org.hibernate.validator.constraints.NotEmpty;
import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.CopyCards;
import uk.gov.ea.wastecarrier.services.core.Payment;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.mongoDb.*;
import uk.gov.ea.wastecarrier.services.search.CopyCardSearch;
import uk.gov.ea.wastecarrier.services.search.AccountSearch;
import uk.gov.ea.wastecarrier.services.search.OriginalRegNumberSearch;
import uk.gov.ea.wastecarrier.services.search.SearchHelper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SearchResource {

    private Logger log = Logger.getLogger(OrdersResource.class.getName());

    private final DatabaseHelper databaseHelper;

    public SearchResource(DatabaseConfiguration databaseConfiguration) {
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
            @QueryParam("copyCards[]") Set<String> copyCards,
            @QueryParam("declaredConvictions") Optional<String> declaredConvictions,
            @QueryParam("convictionCheckMatch") Optional<String> convictionCheckMatch,
            @QueryParam("resultCount") Optional<Integer> resultCount
    ) {

        log.fine("Get Method Detected at /search/registrations");
        List<Registration> searchresults;

        try {
            RegistrationSearch search = new RegistrationSearch(new SearchHelper(this.databaseHelper));
            search.fromDate = from;
            search.toDate = until;
            search.route = route;
            search.status = status;
            search.businessType = businessType;
            search.tier = tier;
            search.copyCards = copyCards;
            search.declaredConvictions = declaredConvictions;
            search.convictionCheckMatch = convictionCheckMatch;
            search.resultCount = resultCount;

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
            @QueryParam("chargeType[]") Set<String> chargeTypes,
            @QueryParam("resultCount") Optional<Integer> resultCount
    ) {

        log.fine("Get Method Detected at /search/payments");
        List<Registration> searchResults;

        try {
            PaymentSearch search = new PaymentSearch(new SearchHelper(this.databaseHelper));
            search.fromDate = from;
            search.toDate = until;
            search.paymentStatuses = paymentStatuses;
            search.paymentTypes = paymentTypes;
            search.chargeTypes = chargeTypes;
            search.resultCount = resultCount;

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

    @GET
    @Path("/copycards")
    public List<Registration> queryCopyCards(
            @QueryParam("from") @NotEmpty String from,
            @QueryParam("until") @NotEmpty String until,
            @QueryParam("declaredConvictions") Optional<String> declaredConvictions,
            @QueryParam("convictionCheckMatch") Optional<String> convictionCheckMatch,
            @QueryParam("resultCount") Optional<Integer> resultCount
    ) {

        log.fine("Get Method Detected at /search/copycards");
        List<Registration> searchResults;

        try {
            Integer extractedResultCount = 0;
            if (resultCount.isPresent()) extractedResultCount = resultCount.get();
            CopyCardSearch search = new CopyCardSearch(
                    new SearchHelper(this.databaseHelper),
                    from,
                    until,
                    declaredConvictions.isPresent(),
                    convictionCheckMatch.isPresent(),
                    extractedResultCount
            );

            searchResults = search.execute();

        } catch (MongoException e) {
            log.severe("Query error: " + e.getMessage());
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
        }

        return searchResults;
    }

    @GET
    @Path("/account/{accountEmail}")
    public List<Registration> queryAccountEmail(
            @PathParam("accountEmail") @NotEmpty String accountEmail
    ) {
        log.fine("Get Method Detected at /search/account");
        List<Registration> searchResults;

        try {
            AccountSearch search = new AccountSearch(new SearchHelper(this.databaseHelper), accountEmail);

            searchResults = search.execute();
        } catch (MongoException e) {
            log.severe("Query error: " + e.getMessage());
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
        }

        return searchResults;
    }

    @GET
    @Path("/originalRegistrationNumber/{originalRegNumber}")
    public List<Registration> queryOriginalRegNumber(
            @PathParam("originalRegistrationNumber") @NotEmpty String originalRegNumber
    ) {
        log.fine("Get Method Detected at /search/originalRegistrationNumber");
        List<Registration> searchResults;

        try {
            OriginalRegNumberSearch search = new OriginalRegNumberSearch(new SearchHelper(this.databaseHelper), originalRegNumber);

            searchResults = search.execute();
        } catch (MongoException e) {
            log.severe("Query error: " + e.getMessage());
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
        }

        return searchResults;
    }
}
