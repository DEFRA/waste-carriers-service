package uk.gov.ea.wastecarrier.services;

import org.junit.*;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.search.OriginalRegNumberSearch;
import uk.gov.ea.wastecarrier.services.support.*;

import static org.junit.Assert.assertEquals;

public class OriginalRegNumberSearchTest {

    private static RegistrationsConnectionUtil connection;
    private static final String originalRegNumber = "CB/VM8888WW/A001";

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
        OriginalRegNumberSearch search = new OriginalRegNumberSearch(connection.searchHelper, originalRegNumber);

        Registration result = search.execute();

        assertEquals("Matching registrations are found", originalRegNumber, result.getOriginalRegistrationNumber());
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

        connection.dao.insert(reg);
    }
}
