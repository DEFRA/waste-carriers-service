package uk.gov.ea.wastecarrier.services.resources;

import com.google.common.base.Optional;
import com.mongodb.MongoException;
import org.hibernate.validator.constraints.NotEmpty;
import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.helper.DatabaseHelper;
import uk.gov.ea.wastecarrier.services.dao.RegistrationDao;
import uk.gov.ea.wastecarrier.services.helper.SearchHelper;
import uk.gov.ea.wastecarrier.services.search.*;

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

    private final Integer defaultResultCount;

    private final SearchHelper searchHelper;

    public SearchResource(
            DatabaseConfiguration configuration,
            Integer defaultResultCount
    ) {
        this.searchHelper = new SearchHelper(
                new DatabaseHelper(configuration),
                new RegistrationDao(configuration)
        );
        this.defaultResultCount = defaultResultCount;
    }

    @GET
    @Path("/registrations/{searchWithin}/{searchValue}")
    public List<Registration> queryWithin(
            @PathParam("searchValue") @NotEmpty String searchValue,
            @PathParam("searchWithin") @NotEmpty String searchWithin
    ) {
        log.fine("Get Method Detected at /search/registrations/{searchWithin}/{searchValue}");
        List<Registration> searchResults;

        try {
            WithinSearch search = new WithinSearch(
                    this.searchHelper,
                    searchValue,
                    searchWithin,
                    defaultResultCount
            );

            searchResults = search.execute();
        } catch (MongoException e) {
            log.severe("Query error: " + e.getMessage());
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
        }

        return searchResults;
    }

    @GET
    @Path("/registrations")
    public List<Registration> queryRegistrations(
            @QueryParam("from") @NotEmpty String from,
            @QueryParam("until") @NotEmpty String until,
            @QueryParam("route[]") Set<String> routes,
            @QueryParam("tier[]") Set<String> tiers,
            @QueryParam("status[]") Set<String> statuses,
            @QueryParam("businessType[]") Set<String> businessTypes,
            @QueryParam("copyCards") Optional<String> copyCards,
            @QueryParam("declaredConvictions") Optional<String> declaredConvictions,
            @QueryParam("convictionCheckMatch") Optional<String> convictionCheckMatch,
            @QueryParam("resultCount") Optional<Integer> resultCount
    ) {

        log.fine("Get Method Detected at /search/registrations");
        List<Registration> searchResults;

        Integer extractedResultCount = 0;
        if (resultCount.isPresent()) extractedResultCount = resultCount.get();

        String extractedCopyCards = null;
        if (copyCards.isPresent()) extractedCopyCards = copyCards.get();

        try {
            RegistrationSearch search = new RegistrationSearch(
                    this.searchHelper,
                    from,
                    until,
                    routes,
                    tiers,
                    statuses,
                    businessTypes,
                    extractedCopyCards,
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
    @Path("/payments")
    public List<Registration> queryPayments(
            @QueryParam("from") @NotEmpty String from,
            @QueryParam("until") @NotEmpty String until,
            @QueryParam("paymentStatus") @NotEmpty String paymentStatus,
            @QueryParam("paymentType[]") Set<String> paymentTypes,
            @QueryParam("chargeType[]") Set<String> chargeTypes,
            @QueryParam("resultCount") Optional<Integer> resultCount
    ) {

        log.fine("Get Method Detected at /search/payments");
        List<Registration> searchResults;

        Integer extractedResultCount = 0;
        if (resultCount.isPresent()) extractedResultCount = resultCount.get();

        try {
            PaymentSearch search = new PaymentSearch(
                    this.searchHelper,
                    from,
                    until,
                    paymentStatus,
                    paymentTypes,
                    chargeTypes,
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

        Integer extractedResultCount = 0;
        if (resultCount.isPresent()) extractedResultCount = resultCount.get();

        try {
            CopyCardSearch search = new CopyCardSearch(
                    this.searchHelper,
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
            AccountSearch search = new AccountSearch(
                    this.searchHelper,
                    accountEmail
            );

            searchResults = search.execute();
        } catch (MongoException e) {
            log.severe("Query error: " + e.getMessage());
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
        }

        return searchResults;
    }

    @GET
    @Path("/original/{originalRegistrationNumber}")
    public Registration queryOriginalRegNumber(
            @PathParam("originalRegistrationNumber") String originalRegNumber
    ) {
        log.fine("Get Method Detected at /search/originalRegistrationNumber");
        Registration searchResult;

        try {
            OriginalRegNumberSearch search = new OriginalRegNumberSearch(
                    this.searchHelper,
                    originalRegNumber
            );

            searchResult = search.execute();
        } catch (MongoException e) {
            log.severe("Query error: " + e.getMessage());
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
        }

        return searchResult;
    }
}
