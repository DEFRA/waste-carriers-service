package uk.gov.ea.wastecarrier.services.helper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import uk.gov.ea.wastecarrier.services.core.mocks.WorldpayOrder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

public class WorldpayHelper {

    private static final String RESOURCE_PATH = "mocks/worldpay";

    private final String adminCode;
    private final String serviceDomain;
    private final String macSecret;
    private final ResourceHelper resourceHelper;

    private Logger log = Logger.getLogger(WorldpayHelper.class.getName());

    public WorldpayHelper(String adminCode, String serviceDomain, String macSecret) {
        this.adminCode = adminCode;
        this.serviceDomain = serviceDomain;
        this.macSecret = macSecret;
        this.resourceHelper = new ResourceHelper();
    }

    public Document stringToXml(String value) {
        Document result = null;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;

        try {
            builder = factory.newDocumentBuilder();
            result = builder.parse(new InputSource(new StringReader(value)));

            // Optional, but recommended
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            result.getDocumentElement().normalize();
        } catch (Exception ex) {
            log.severe("Error parsing XML: " + ex.getMessage());
        }

        return result;
    }

    public String initialResponse(String merchantCode, String orderCode, String id) {

        String template = this.resourceHelper.openResourceFile(RESOURCE_PATH, "initialResponse.xml");
        String url = this.serviceDomain + "/mocks/worldpay/dispatcher";

        String response = template.replaceAll("MERCHANT_CODE", merchantCode);
        response = response.replaceFirst("WORLDPAY_ID", id);
        response = response.replaceFirst("WORLDPAY_URL", url);

        return response.replaceAll("ORDER_CODE", orderCode);
    }

    public String orderCompletedRedirectUrl(WorldpayOrder order, String successUrl) {

        String queryString = String.format(
                "?orderKey=%s&paymentStatus=AUTHORISED&paymentAmount=%s&paymentCurrency=%s&mac=%s&source=WP",
                generateOrderKey(order).replaceAll("\\^","%5E"),
                String.valueOf(order.value),
                order.currencyCode,
                generateMac(order)
        );

        return successUrl + queryString;
    }

    public String extractOrderCodeFromKey(String orderKey) {
        return orderKey.split("\\^")[1];
    }

    public WorldpayOrder convertFromXml(Document xmlOrder) {

        WorldpayOrder order = new WorldpayOrder();

        order.worldpayId = generateWorldPayId();

        order.merchantCode = xmlOrder.getDocumentElement().getAttribute("merchantCode");

        Element eOrder = ((Element) xmlOrder.getElementsByTagName("order").item(0));

        order.orderCode = eOrder.getAttribute("orderCode");
        order.description = eOrder.getElementsByTagName("description").item(0).getTextContent();
        order.orderContent = eOrder.getElementsByTagName("orderContent").item(0).getTextContent();

        Element amount = (Element) eOrder.getElementsByTagName("amount").item(0);

        order.currencyCode = amount.getAttribute("currencyCode");
        order.value = Integer.valueOf(amount.getAttribute("value"));
        order.exponent = amount.getAttribute("exponent");

        Element shopper = (Element) eOrder.getElementsByTagName("shopper").item(0);

        order.shopperEmailAddress = shopper.getElementsByTagName("shopperEmailAddress").item(0).getTextContent();

        return order;
    }

    public String generateWorldPayId() {
        // https://stackoverflow.com/a/5328933/6117745
        return String.valueOf((long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L);
    }

    /**
     * From the order details and config settings generate the mac code used to
     * sign a response from worldpay
     *
     * Started by following the advice in
     * http://support.worldpay.com/support/kb/gg/corporate-gateway-guide/content/hostedintegration/securingpayments.htm
     * and looked at
     * https://stackoverflow.com/questions/7124735/hmac-sha256-algorithm-for-signature-calculation
     * https://github.com/danharper/hmac-examples
     *
     * However they were returning values nothing like what we were seeing from
     * Worldpay. So following what the Frontend and Renewals apps do, we looked
     * instead at MD5. Using the code below we finally were generating values
     * in the same way as Worldpay.
     *
     * Solution taken from
     * https://stackoverflow.com/a/30119004/6117745
     * @param order Order to generate the mac for
     * @return a string representing the encoded mac
     */
    public String generateMac(WorldpayOrder order) {
        String result = null;

        String message = String.format(
                "%s%s%sAUTHORISED%s",
                generateOrderKey(order),
                String.valueOf(order.value),
                order.currencyCode,
                this.macSecret
        );

        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(StandardCharsets.UTF_8.encode(message));

            result = String.format("%032x", new BigInteger(1, md5.digest()));
        } catch(NoSuchAlgorithmException ex) {
            log.severe("Error generating Mac: " + ex.getMessage());
        }

        return result;
    }

    private String generateOrderKey(WorldpayOrder order) {
        return String.format(
                "%s^%s^%s",
                this.adminCode,
                order.merchantCode,
                order.orderCode
        );
    }
}
