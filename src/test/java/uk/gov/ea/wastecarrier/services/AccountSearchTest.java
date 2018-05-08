package uk.gov.ea.wastecarrier.services;

import org.junit.*;
import org.junit.runners.MethodSorters;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.mongoDb.AccountSearch;

import uk.gov.ea.wastecarrier.services.support.*;

import java.util.List;

import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AccountSearchTest {

    private static ConnectionUtil connection;
    private static final String accountEmail = "joe@example.com";

    @BeforeClass
    public static void runOnceBefore() {
        connection = new ConnectionUtil();
    }

    /**
     * Create a test registration
     *
     * We use this in our subsequent tests to test the search functionality
     */
    @Before
    public void createRegistration() {
        Registration reg = new RegistrationBuilder(RegistrationBuilder.BuildType.LOWER)
                .accountEmail(this.accountEmail)
                .build();

        connection.registrationsDao.insertRegistration(reg);
    }

    @Test
    public void test1_searchForRegistration() {
        AccountSearch search = new AccountSearch(connection.searchHelper, accountEmail);
        List<Registration> results = search.execute();
        String resultEmail = results.get(0).getAccountEmail();
        assertEquals("Matching registrations are found", accountEmail, resultEmail);
    }

    /**
     * Deletes any registrations we have created during testing
     */
    @After
    public void deleteRegistration() {
        connection.clean();
    }

}
