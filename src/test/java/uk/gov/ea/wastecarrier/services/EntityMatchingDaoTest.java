package uk.gov.ea.wastecarrier.services;

import org.junit.*;
import org.junit.runners.MethodSorters;
import uk.gov.ea.wastecarrier.services.core.Entity;
import uk.gov.ea.wastecarrier.services.support.EntityBuilder;
import uk.gov.ea.wastecarrier.services.support.EntityMatchingConnectionUtil;

import static org.junit.Assert.assertTrue;

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
        connection.dao.getCollection().count();
    }

    @Test
    public void test2_insert() {
        Entity document = new EntityBuilder(EntityBuilder.BuildType.COMPANY).build();

        document = connection.dao.insert(document);

        String id = document.id;

        assertTrue("The registration is inserted", id != null && !id.isEmpty());
    }
}
