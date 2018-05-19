package uk.gov.ea.wastecarrier.services;

import org.junit.*;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.search.AccountSearch;
import uk.gov.ea.wastecarrier.services.support.*;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class AccountSearchTest {

    private static RegistrationsConnectionUtil connection;
    private static final String accountEmail = "joe@example.com";

    @BeforeClass
    public static void setup() {
        connection = new RegistrationsConnectionUtil();
        createRegistrations();
    }

    /**
     * Deletes any registrations we have created during testing
     */
    @AfterClass
    public static void tearDown() {
        connection.clean();
    }

    @Test
    public void searchForRegistration() {
        AccountSearch search = new AccountSearch(connection.searchHelper, accountEmail);
        List<Registration> results = search.execute();
        String resultEmail = results.get(0).getAccountEmail();
        assertEquals("Matching registrations are found", accountEmail, resultEmail);
    }

    /**
     * Create a test registration
     *
     * We use this in our subsequent tests to test the search functionality
     */
    private static void createRegistrations() {
        Registration reg = new RegistrationBuilder(RegistrationBuilder.BuildType.LOWER)
                .accountEmail(accountEmail)
                .build();

        connection.dao.insert(reg);
    }

}
