package uk.gov.ea.wastecarrier.services;

import org.junit.*;
import org.junit.runners.MethodSorters;

import uk.gov.ea.wastecarrier.services.core.*;
import uk.gov.ea.wastecarrier.services.core.MetaData.RegistrationStatus;

import uk.gov.ea.wastecarrier.services.support.*;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RegistrationsMongoDaoTest {

    private static ConnectionUtil connection;
	private static final String lowerTierRegNumber = "CBDL99999";

	@BeforeClass
    public static void runOnceBefore() {
        connection = new ConnectionUtil();
	}

    /**
     * Deletes any registrations we have created during testing
     */
	@AfterClass
    public static void runOnceAfter() {
        connection.clean();
	}

    /**
     * Check we can connect to MongoDb. Simply creating a connection is not a
     * guarantee that everything is working. So we also call
     * getNumberOfRegistrationsTotal() as this performs a query against the DB,
     * and checks we can connect, authenticate, query and return a result
     */
	@Test
	public void test1_getNumberOfRegistrationsTotal() {
        connection.registrationsDao.getNumberOfRegistrationsTotal();
	}

    /**
     * Check we can insert a new lower tier registration into the registrations
     * collection. This goes beyond simply connecting and querying, and
     * checks that the Monjojack POJO mapping is working correctly.
     */
    @Test
    public void test2_insertRegistration() {
        Registration reg = new RegistrationBuilder(RegistrationBuilder.BuildType.LOWER)
                .regIdentifier(lowerTierRegNumber)
                .build();

        reg = connection.registrationsDao.insertRegistration(reg);
        String id = reg.getId();
        assertTrue("The registration is inserted", id != null && !id.isEmpty());
    }

    /**
     * Check we can find our registration
     */
    @Test
    public void test3_findRegistration() {
        Registration reg = connection.registrationsDao.findRegistration(lowerTierRegNumber);
        String regNo = reg.getRegIdentifier();
        assertEquals("The registration is found", lowerTierRegNumber, regNo);
    }

    /**
     * Check we can update our registration
     */
    @Test
    public void test4_updateRegistration() {
        Registration reg = connection.registrationsDao.findRegistration(lowerTierRegNumber);
        reg.getMetaData().setStatus(RegistrationStatus.ACTIVE);
        reg.getMetaData().setDateActivated(new Date());
        reg = connection.registrationsDao.updateRegistration(reg);
        assertEquals(
                "The registration is updated",
                RegistrationStatus.ACTIVE,
                reg.getMetaData().getStatus()
        );
    }
}
