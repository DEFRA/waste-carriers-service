/**
 * 
 */
package uk.gov.ea.wastecarrier.services.mongo;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;
import uk.gov.ea.wastecarrier.services.mongoDb.RegistrationsMongoDao;
import junit.framework.TestCase;

/**
 * Tests (involving the MongoDB database) for the Registrations DAO.
 * @author gmueller
 *
 */
public class RegistrationsMongoDaoTest extends TestCase {

	private RegistrationsMongoDao dao;
	
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		//TODO Get config from DW config files
		String dbHost = System.getenv("WCRS_SERVICES_DB_HOST");
		int dbPort = Integer.valueOf(System.getenv("WCRS_SERVICES_DB_PORT"));
		String dbName = System.getenv("WCRS_SERVICES_DB_NAME");
		String dbUser = System.getenv("WCRS_SERVICES_DB_USER");
		String dbPassword = System.getenv("WCRS_SERVICES_DB_PASSWD");
		DatabaseConfiguration dbConfig = new DatabaseConfiguration(dbHost, dbPort, dbName, dbUser, dbPassword);
		DatabaseHelper databaseHelper = new DatabaseHelper(dbConfig);
		dao = new RegistrationsMongoDao(databaseHelper);
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link uk.gov.ea.wastecarrier.services.mongoDb.RegistrationsMongoDao#getNumberOfRegistrationsTotal()}.
	 */
	public void testGetNumberOfRegistrationsTotal() {
		//TODO Re-enable test after config setup
		//long total = dao.getNumberOfRegistrationsTotal();
		//System.out.println("total = " + total);
	}

	/**
	 * Test method for {@link uk.gov.ea.wastecarrier.services.mongoDb.RegistrationsMongoDao#getNumberOfRegistrationsPending()}.
	 */
	public void testGetNumberOfRegistrationsPending() {
		//TODO Re-enable test after config setup
		//long total = dao.getNumberOfRegistrationsPending();
		//System.out.println("total = " + total);
	}

}
