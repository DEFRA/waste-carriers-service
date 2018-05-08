package uk.gov.ea.wastecarrier.services.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.RegistrationBuilder;
import uk.gov.ea.wastecarrier.services.core.*;
import uk.gov.ea.wastecarrier.services.core.MetaData.RegistrationStatus;
import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;
import uk.gov.ea.wastecarrier.services.mongoDb.RegistrationsMongoDao;

import java.util.Date;


/**
 * Tests (involving the MongoDB database) for the Registrations DAO.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RegistrationsMongoDaoTest {

	private static RegistrationsMongoDao dao;
	private static DatabaseHelper helper;

	private static final String lowerTierRegNumber = "CBDL99999";

	@BeforeClass
    public static void runOnceBefore() {
	    String host = System.getenv("WCRS_SERVICES_DB_HOST");
        int port = Integer.valueOf(System.getenv("WCRS_SERVICES_DB_PORT"));
		String name = System.getenv("WCRS_SERVICES_DB_NAME");
		String username = System.getenv("WCRS_SERVICES_DB_USER");
		String password = System.getenv("WCRS_SERVICES_DB_PASSWD");

		DatabaseConfiguration config = new DatabaseConfiguration(host, port, name, username, password);

		helper = new DatabaseHelper(config);
		dao = new RegistrationsMongoDao(config);
	}

    /**
     * Deletes any registrations we have created during testing
     * @throws Exception
     */
	@AfterClass
    public static void runOnceAfter() {
		DB db = helper.getConnection();
        DBCollection collection = db.getCollection("registrations");

        BasicDBObject lowerTierQuery = new BasicDBObject("regIdentifier", lowerTierRegNumber);
        collection.remove(lowerTierQuery);
	}

    /**
     * Check we can connect to MongoDb. Simply creating a connection is not a
     * guarantee that everything is working. So we also call
     * getNumberOfRegistrationsTotal() as this performs a query against the DB,
     * and checks we can connect, authenticate, query and return a result
     */
	@Test
	public void test1_getNumberOfRegistrationsTotal() {
		dao.getNumberOfRegistrationsTotal();
	}

    /**
     * Check we can insert a new lower tier registration into the registrations
     * collection. This goes beyond simply connecting and querying, and
     * checks that the Monjojack POJO mapping is working correctly.
     */
    @Test
    public void test2_insertRegistration() {
        Registration reg = new RegistrationBuilder().regIdentifier(lowerTierRegNumber).buildLowerTier();
        reg = dao.insertRegistration(reg);
        String id = reg.getId();
        assertTrue("The registration is inserted", id != null && !id.isEmpty());
    }

    /**
     * Check we can find our registration
     */
    @Test
    public void test3_findRegistration() {
        Registration reg = dao.findRegistration(lowerTierRegNumber);
        String regNo = reg.getRegIdentifier();
        assertEquals("The registration is found", lowerTierRegNumber, regNo);
    }

    /**
     * Check we can update our registration
     */
    @Test
    public void test4_updateRegistration() {
        Registration reg = dao.findRegistration(lowerTierRegNumber);
        reg.getMetaData().setStatus(RegistrationStatus.ACTIVE);
        reg.getMetaData().setDateActivated(new Date());
        reg = dao.updateRegistration(reg);
        assertEquals(
                "The registration is updated",
                RegistrationStatus.ACTIVE,
                reg.getMetaData().getStatus()
        );
    }
}
