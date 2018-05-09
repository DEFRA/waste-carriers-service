package uk.gov.ea.wastecarrier.services;

import org.junit.*;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.search.OriginalRegNumberSearch;
import uk.gov.ea.wastecarrier.services.support.*;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class OriginalRegNumberSearchTest {

    private static ConnectionUtil connection;
    private static final String originalRegNumber = "CB/VM8888WW/A001";

    @BeforeClass
    public static void setup() {
        connection = new ConnectionUtil();
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
        OriginalRegNumberSearch search = new OriginalRegNumberSearch(connection.searchHelper, originalRegNumber);
        List<Registration> results = search.execute();
        String resultOriginalRegNo = results.get(0).getOriginalRegistrationNumber();
        assertEquals("Matching registrations are found", originalRegNumber, resultOriginalRegNo);
    }

    /**
     * Create a test registration
     *
     * We use this in our subsequent tests to test the search functionality
     */
    private static void createRegistrations() {
        Registration reg = new RegistrationBuilder(RegistrationBuilder.BuildType.IRRENEWAL)
                .originalRegNumber(originalRegNumber)
                .build();

        connection.registrationsDao.insertRegistration(reg);
    }
}
