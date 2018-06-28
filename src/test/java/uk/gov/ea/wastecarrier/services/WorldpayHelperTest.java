package uk.gov.ea.wastecarrier.services;

import org.junit.*;
import org.w3c.dom.Document;
import uk.gov.ea.wastecarrier.services.core.mocks.WorldpayOrder;
import uk.gov.ea.wastecarrier.services.helper.mocks.WorldpayHelper;
import uk.gov.ea.wastecarrier.services.support.FixtureReader;
import uk.gov.ea.wastecarrier.services.support.WorldpayOrderBuilder;

import java.io.IOException;

import static org.junit.Assert.*;

public class WorldpayHelperTest {

    private static WorldpayHelper helper;

    @BeforeClass
    public static void setup() {
        helper = new WorldpayHelper(
                "ENVIRONMENTAGENCY",
                "http://localhost:8005",
                "macKeydaddyyeahfool@7"
        );
    }

    @Test
    public void stringToXml() throws IOException {
        String xml = FixtureReader.readFile("src/test/resources/fixtures/worldpayInitialRequest.xml");
        Document result = helper.stringToXml(xml);

        assertNotNull(result);
        assertEquals("paymentService", result.getDocumentElement().getNodeName());
    }

    @Test
    public void initialResponse() {
        String result = helper.initialResponse("TEST_MERCHANT", "TEST_ORDER", "TEST_ID");

        assertNotNull(result);
        assertTrue(result.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(result.contains("TEST_MERCHANT"));
        assertTrue(result.contains("TEST_ORDER"));
        assertTrue(result.contains("TEST_ID"));
    }

    @Test
    public void convertFromXml() throws IOException {
        String xml = FixtureReader.readFile("src/test/resources/fixtures/worldpayInitialRequest.xml");
        Document result = helper.stringToXml(xml);

        WorldpayOrder order = helper.convertFromXml(result);
        assertEquals("1530087444", order.orderCode);
        assertEquals((Integer)10500, order.value);
    }

    @Test
    public void generateWorldpayId() {

        String id = helper.generateWorldPayId();

        assertTrue("Id generated is 10 chars in length",id.length() == 10);
        assertTrue("Id generated only contains numbers", id.matches("[0-9]+"));
    }

    @Test
    public void extractOrderCodeFromKey() {
        String result = helper.extractOrderCodeFromKey("MERCHANTCODE^1530092035");
        assertNotNull("I get a response", result);
        assertEquals("I get just the order code", "1530092035", result);
    }

    @Test
    public void orderCompletedRedirectUrl() {
        WorldpayOrder document = new WorldpayOrderBuilder()
                .orderCode("1530105721")
                .merchantCode("WCRSERVICE")
                .value(10500)
                .build();

        String result = helper.orderCompletedRedirectUrl(document, "http://localhost:3002");
        assertEquals("http://localhost:3002?orderKey=ENVIRONMENTAGENCY%5EWCRSERVICE%5E1530105721&paymentStatus=AUTHORISED&paymentAmount=10500&paymentCurrency=GBP&mac=361bb202cd425177602db0f15c84f365&source=WP", result);
    }

    @Test
    public void generateMac() {
        WorldpayOrder document = new WorldpayOrderBuilder()
                .orderCode("1530105721")
                .merchantCode("WCRSERVICE")
                .value(10500)
                .build();

        assertEquals("361bb202cd425177602db0f15c84f365", helper.generateMac(document));
    }
}
