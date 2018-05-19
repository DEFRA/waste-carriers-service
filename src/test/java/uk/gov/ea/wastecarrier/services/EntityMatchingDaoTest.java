package uk.gov.ea.wastecarrier.services;

import org.junit.*;
import uk.gov.ea.wastecarrier.services.core.Entity;
import uk.gov.ea.wastecarrier.services.support.EntityBuilder;
import uk.gov.ea.wastecarrier.services.support.EntityMatchingConnectionUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    public void checkConnection() {
        connection.dao.getCollection().count();
    }

    @Test
    public void insert() {
        Entity document = new EntityBuilder(EntityBuilder.BuildType.COMPANY)
                .build();

        String id = this.connection.dao.insert(document).id;

        assertTrue("The entity is inserted", id != null && !id.isEmpty());
    }

    @Test
    public void find() {
        String testIncidentNo = "FTW001234";

        Entity document = new EntityBuilder(EntityBuilder.BuildType.COMPANY)
                .incidentNumber(testIncidentNo)
                .build();

        String id = this.connection.dao.insert(document).id;

        document = this.connection.dao.find(id);

        assertEquals("The entity is found", testIncidentNo, document.incidentNumber);
    }
}
