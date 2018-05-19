package uk.gov.ea.wastecarrier.services;

import org.junit.*;
import org.junit.runners.MethodSorters;
import uk.gov.ea.wastecarrier.services.support.EntityMatchingConnectionUtil;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EntityMatchingDaoTest {

    private static EntityMatchingConnectionUtil connection;

    @BeforeClass
    public static void setup() {
        connection = new EntityMatchingConnectionUtil();
    }

    /**
     * Deletes any registrations we have created during testing
     */
    @AfterClass
    public static void tearDown() {
        connection.clean();
    }

    @Test
    public void test1_checkConnection() {
        connection.databaseHelper.getCollection("entities").count();
    }
}
