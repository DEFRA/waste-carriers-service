package uk.gov.ea.wastecarrier.services;

import org.junit.*;

import uk.gov.ea.wastecarrier.services.core.*;
import uk.gov.ea.wastecarrier.services.core.MetaData.RegistrationStatus;

import uk.gov.ea.wastecarrier.services.support.*;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RegistrationDaoTest {

    private static RegistrationsConnectionUtil connection;
    private static final String lowerTierRegNumber = "CBDL99999";

    @BeforeClass
    public static void setup() {
        connection = new RegistrationsConnectionUtil();
    }

    /**
     * Deletes any registrations we have created during testing
     */
    @AfterClass
    public static void tearDown() {
        connection.clean();
    }

    /**
     * Check we can connect to MongoDb. Simply creating a connection is not a
     * guarantee that everything is working. So we also call
     * getNumberOfRegistrationsTotal() as this performs a query against the DB,
     * and checks we can connect, authenticate, query and return a result
     */
    @Test
    public void checkConnection() {
        connection.dao.getCollection().count();
    }

    /**
     * Check we can insert a new lower tier registration into the registrations
     * collection. This goes beyond simply connecting and querying, and
     * checks that the Monjojack POJO mapping is working correctly.
     */
    @Test
    public void insert() {
        Registration document = new RegistrationBuilder(RegistrationBuilder.BuildType.LOWER)
                .regIdentifier("CBDL99999")
                .build();

        String id = this.connection.dao.insert(document).getId();

        assertTrue("The registration is inserted", id != null && !id.isEmpty());
    }

    /**
     * Check we can find a registration by its ID
     */
    @Test
    public void find() {

        Registration document = new RegistrationBuilder(RegistrationBuilder.BuildType.LOWER)
                .build();

        String id = this.connection.dao.insert(document).getId();

        document = this.connection.dao.find(id);

        assertEquals("The entity is found", id, document.getId());
    }

    /**
     * Check we can find a registration by its registration number
     */
    @Test
    public void findByRegIdentifier() {
        String regIdentifier = "CBDL99999";

        Registration document = new RegistrationBuilder(RegistrationBuilder.BuildType.LOWER)
                .regIdentifier(regIdentifier)
                .build();

        this.connection.dao.insert(document);

        String foundRegIdentifier = this.connection.dao.findByRegIdentifier(regIdentifier).getRegIdentifier();

        assertEquals("The entity is found", regIdentifier, foundRegIdentifier);
    }

    /**
     * Check we can update a registration
     */
    @Test
    public void update() {
        Registration document = new RegistrationBuilder(RegistrationBuilder.BuildType.LOWER)
                .build();

        document = this.connection.dao.insert(document);

        document.getMetaData().setStatus(RegistrationStatus.ACTIVE);
        document.getMetaData().setDateActivated(new Date());

        RegistrationStatus status = connection.dao.update(document).getMetaData().getStatus();

        assertEquals(
                "The registration is updated",
                RegistrationStatus.ACTIVE,
                status
        );
    }
}
