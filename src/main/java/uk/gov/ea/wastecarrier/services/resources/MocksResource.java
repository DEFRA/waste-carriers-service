package uk.gov.ea.wastecarrier.services.resources;

import org.hibernate.validator.constraints.NotEmpty;
import org.w3c.dom.Document;
import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.MockConfiguration;
import uk.gov.ea.wastecarrier.services.helper.ResourceHelper;
import uk.gov.ea.wastecarrier.services.helper.mocks.CompaniesHouseHelper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.logging.Logger;

@Path("/mocks")
public class MocksResource {

    private final CompaniesHouseHelper companyHelper;
    private final Integer mockDelay;

    private Logger log = Logger.getLogger(MocksResource.class.getName());

    public MocksResource(DatabaseConfiguration configuration, MockConfiguration config) {
        this.companyHelper = new CompaniesHouseHelper();
        this.mockDelay = config.delay;
    }

    @GET
    @Path("/company/{companyNumber}")
    @Produces(MediaType.APPLICATION_JSON)
    public String companiesHouse(@PathParam("companyNumber") String companyNumber) {
        delay();
        return this.companyHelper.response(companyNumber);
    }

    private void delay() {

        try {
            Thread.sleep(this.mockDelay);
        } catch (InterruptedException ex) {
            log.severe("Error trying to delay mock responses: " + ex);
        }
    }
}
