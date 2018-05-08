package uk.gov.ea.wastecarrier.services;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import org.junit.*;
import org.junit.runners.MethodSorters;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.mongoDb.AccountSearch;
import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;
import uk.gov.ea.wastecarrier.services.mongoDb.RegistrationsMongoDao;
import uk.gov.ea.wastecarrier.services.mongoDb.SearchHelper;

import java.util.List;

import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AccountSearchTest {
    private static RegistrationsMongoDao dao;
    private static SearchHelper helper;

    private static final String accountEmail = "joe@example.com";

    @BeforeClass
    public static void runOnceBefore() {
        String host = System.getenv("WCRS_SERVICES_DB_HOST");
        int port = Integer.valueOf(System.getenv("WCRS_SERVICES_DB_PORT"));
        String name = System.getenv("WCRS_SERVICES_DB_NAME");
        String username = System.getenv("WCRS_SERVICES_DB_USER");
        String password = System.getenv("WCRS_SERVICES_DB_PASSWD");

        DatabaseConfiguration config = new DatabaseConfiguration(host, port, name, username, password);

        helper = new SearchHelper(new DatabaseHelper(config));
        dao = new RegistrationsMongoDao(config);
    }

    /**
     * Create a test registration
     *
     * We use this in our subsequent tests to test the searc functionality
     */
    @Before
    public void createRegistration() {
        Registration reg = new RegistrationBuilder().accountEmail(this.accountEmail).buildLowerTier();
        reg = dao.insertRegistration(reg);
    }

    @Test
    public void test1_searchForRegistration() {
        AccountSearch search = new AccountSearch(this.helper, accountEmail);
        List<Registration> results = search.execute();
        String resultEmail = results.get(0).getAccountEmail();
        assertEquals("Matching registrations are found", accountEmail, resultEmail);
    }

    /**
     * Deletes any registrations we have created during testing
     * @throws Exception
     */
    @After
    public void deleteRegistration() {
        DBCollection collection = helper.getRegistrationsCollection();

        BasicDBObject query = new BasicDBObject("accountEmail", accountEmail);
        collection.remove(query);
    }

}
