package uk.gov.ea.wastecarrier.services.resources;

import org.hibernate.validator.constraints.NotEmpty;
import org.w3c.dom.Document;
import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.MockConfiguration;
import uk.gov.ea.wastecarrier.services.core.mocks.WorldpayOrder;
import uk.gov.ea.wastecarrier.services.dao.MockWorldpayDao;
import uk.gov.ea.wastecarrier.services.helper.WorldpayHelper;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.logging.Logger;

@Path("/mocks")
public class MocksResource {

    private final MockWorldpayDao dao;
    private final WorldpayHelper helper;
    private final Integer mockDelay;

    private Logger log = Logger.getLogger(MocksResource.class.getName());

    public MocksResource(DatabaseConfiguration configuration, MockConfiguration config) {
        this.dao = new MockWorldpayDao(configuration);
        this.helper = new WorldpayHelper(config.worldPayAdminCode, config.servicesDomain, config.macSecret);
        this.mockDelay = config.delay;
    }

    @GET
    @Path("/worldpay/payment-service")
    public String paymentService(String body) {
        Document xmlBody = helper.stringToXml(body);

        WorldpayOrder order = helper.convertFromXml(xmlBody);
        this.dao.insert(order);

        delay();

        return helper.initialResponse(order.merchantCode, order.orderCode, order.worldpayId);
    }

    @GET
    @Path("/worldpay/dispatcher")
    public Response dispatcher(
            @QueryParam("OrderKey") @NotEmpty String orderKey,
            @QueryParam("successURL") @NotEmpty String successUrl,
            @QueryParam("pendingURL") String pendingURL,
            @QueryParam("failureURL") String failureURL,
            @QueryParam("cancelURL") String cancelURL,
            @QueryParam("errorURL") String errorURL
    ) {

        String orderCode = this.helper.extractOrderCodeFromKey(orderKey);

        WorldpayOrder order = this.dao.findByOrderCode(orderCode);
        String redirectUrl = this.helper.orderCompletedRedirectUrl(order, successUrl);

        URI target = UriBuilder.fromUri(redirectUrl).build();

        delay();

        return Response.seeOther(target).build();
    }

    private void delay() {

        try {
            Thread.sleep(this.mockDelay);
        } catch (InterruptedException ex) {
            log.severe("Error trying to delay mock responses: " + ex);
        }
    }
}
