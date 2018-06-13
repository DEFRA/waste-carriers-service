package uk.gov.ea.wastecarrier.services.resources;

import com.mongodb.MongoException;
import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.ConvictionSearchResult;
import uk.gov.ea.wastecarrier.services.core.Entity;
import uk.gov.ea.wastecarrier.services.dao.EntityDao;
import uk.gov.ea.wastecarrier.services.helper.DatabaseHelper;
import uk.gov.ea.wastecarrier.services.helper.SearchHelper;
import uk.gov.ea.wastecarrier.services.match.CompanyMatch;
import uk.gov.ea.wastecarrier.services.match.PersonMatch;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.logging.Logger;

@Path("/match")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MatchResource {

    private Logger log = Logger.getLogger(MatchResource.class.getName());

    private final SearchHelper searchHelper;

    public MatchResource(DatabaseConfiguration configuration) {
        this.searchHelper = new SearchHelper(
                new DatabaseHelper(configuration),
                new EntityDao(configuration)
        );
    }

    @GET
    @Path("/company")
    public ConvictionSearchResult matchCompany(
            @QueryParam("name") String name,
            @QueryParam("number") String number
    ) {

        log.fine("Get Method Detected at /match/company");

        ConvictionSearchResult result = new ConvictionSearchResult();
        result.searchedAt = new Date();

        try {
            CompanyMatch matcher = new CompanyMatch(this.searchHelper, name, number);
            Entity match = matcher.execute();
            result.update(match);
        } catch (MongoException e) {
            log.severe("Match error: " + e.getMessage());
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
        }

        return result;
    }

    @GET
    @Path("/person")
    public ConvictionSearchResult matchPerson(
            @QueryParam("firstname") String firstname,
            @QueryParam("lastname") String lastname
    ) {

        log.fine("Get Method Detected at /match/person");

        ConvictionSearchResult result = new ConvictionSearchResult();
        result.searchedAt = new Date();

        try {
            PersonMatch matcher = new PersonMatch(this.searchHelper, firstname, lastname, null);
            Entity match = matcher.execute();
            result.update(match);
        } catch (MongoException e) {
            log.severe("Match error: " + e.getMessage());
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
        }

        return result;
    }
}
