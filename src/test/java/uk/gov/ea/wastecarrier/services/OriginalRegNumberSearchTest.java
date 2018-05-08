package uk.gov.ea.wastecarrier.services;

import org.junit.*;
import org.junit.runners.MethodSorters;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.mongoDb.OriginalRegNumberSearch;

import uk.gov.ea.wastecarrier.services.support.*;

import java.util.List;

import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OriginalRegNumberSearchTest {

    private static ConnectionUtil connection;
    private static final String originalRegNumber = "CB/VM8888WW/A001";

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
        Registration reg = new RegistrationBuilder(RegistrationBuilder.BuildType.IRRENEWAL)
                .originalRegNumber(this.originalRegNumber)
                .build();

        connection.registrationsDao.insertRegistration(reg);
    }

    @Test
    public void test1_searchForRegistration() {
        OriginalRegNumberSearch search = new OriginalRegNumberSearch(connection.searchHelper, originalRegNumber);
        List<Registration> results = search.execute();
        String resultOriginalRegNo = results.get(0).getOriginalRegistrationNumber();
        assertEquals("Matching registrations are found", originalRegNumber, resultOriginalRegNo);
    }

    /**
     * Deletes any registrations we have created during testing
     */
    @After
    public void deleteRegistration() {
        connection.clean();
    }
}
