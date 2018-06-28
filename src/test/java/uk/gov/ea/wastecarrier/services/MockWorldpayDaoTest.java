package uk.gov.ea.wastecarrier.services;

import org.junit.*;
import uk.gov.ea.wastecarrier.services.core.mocks.WorldpayOrder;
import uk.gov.ea.wastecarrier.services.support.MockWorldpayOrderConnectionUtil;
import uk.gov.ea.wastecarrier.services.support.WorldpayOrderBuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MockWorldpayDaoTest {

    private static MockWorldpayOrderConnectionUtil connection;

    @BeforeClass
    public static void setup() {
        connection = new MockWorldpayOrderConnectionUtil();
    }

    /**
     * Deletes any orders we have created during testing
     */
    @AfterClass
    public static void tearDown() {
        connection.clean();
    }

    @Test
    public void checkConnection() {
        assertTrue("Returns true when credentials are valid", connection.dao.checkConnection());
    }

    @Test(expected = Exception.class)
    public void checkConnectionThrowsExceptionWhenConfigInvalid() {
        connection.invalidCredentialsDao().checkConnection();
    }

    @Test
    public void insert() {
        WorldpayOrder document = new WorldpayOrderBuilder()
                .build();

        String id = connection.dao.insert(document).id;

        assertTrue("The order is inserted", id != null && !id.isEmpty());
    }

    @Test
    public void find() {
        WorldpayOrder document = new WorldpayOrderBuilder()
                .build();

        String id = connection.dao.insert(document).id;

        document = connection.dao.find(id);

        assertEquals("The order is found", id, document.id);
    }

    @Test
    public void findByOrderCode() {
        String orderCode = "1234567890";

        WorldpayOrder document = new WorldpayOrderBuilder()
                .orderCode(orderCode)
                .build();

        connection.dao.insert(document);

        assertEquals("The order is found", orderCode, connection.dao.findByOrderCode(orderCode).orderCode);
    }
}
