package uk.gov.ea.wastecarrier.services;

import org.junit.*;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.search.WithinSearch;
import uk.gov.ea.wastecarrier.services.support.RegistrationsConnectionUtil;
import uk.gov.ea.wastecarrier.services.support.RegistrationBuilder;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class WithinSearchTest {

    private static RegistrationsConnectionUtil connection;

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

    /**
     * We're testing a number of things here
     * - That we can do a 'like' search on the term 'waste'
     * - That the search is case insensitive
     * - That the search will still return exact matches
     * - That the results are ordered correctly by company name
     */
    @Test
    public void searchWithinCompany() {
        WithinSearch search = new WithinSearch(
                connection.searchHelper,
                "waste",
                "companyName",
                100
        );
        List<Registration> results = search.execute();

        assertEquals("4 registrations are returned", 4, results.size());

        assertEquals("The first result is CBDL11", "CBDL11", results.get(0).getRegIdentifier());
        assertEquals("The last result is CBDL1", "CBDL1", results.get(3).getRegIdentifier());
        assertEquals("And CBDL20 is not one of them", false, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDL20"::equals));
    }

    /**
     * We're testing a number of things here
     * - That we can do a 'like' search on the term 'smith'
     * - That the search is case insensitive
     * - That the search will still return exact matches
     * - That the results are ordered correctly by last name
     */
    @Test
    public void searchWithinContactName() {
        WithinSearch search = new WithinSearch(
                connection.searchHelper,
                "smith",
                "contactName",
                100
        );
        List<Registration> results = search.execute();

        assertEquals("4 registrations are returned", 4, results.size());

        assertEquals("The first result is CBDL11", "CBDL11", results.get(0).getRegIdentifier());
        assertEquals("The last result is CBDL1", "CBDL1", results.get(3).getRegIdentifier());
        assertEquals("And CBDL20 is not one of them", false, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDL20"::equals));
    }

    /**
     * We're testing a number of things here
     * - That we can do a 'like' search on the term 'bs1'
     * - That the search is case insensitive
     * - That the search will still return exact matches
     * - That the search finds registrations irrespective of whether the
     *      postcode is in the registered or postal address
     * - That the results are ordered correctly by postcode
     */
    @Test
    public void searchWithinPostcode() {
        WithinSearch search = new WithinSearch(
                connection.searchHelper,
                "bs1",
                "postcode",
                100
        );
        List<Registration> results = search.execute();

        assertEquals("4 registrations are returned", 4, results.size());

        assertEquals("The first result is CBDL11", "CBDL11", results.get(0).getRegIdentifier());
        assertEquals("The last result is CBDL1", "CBDL1", results.get(3).getRegIdentifier());
        assertEquals("And CBDL20 is not one of them", false, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDL20"::equals));
    }

    /**
     * We're testing a number of things here
     * - That we can do a 'like' search on the term 'cbdl1'
     * - That the search is case insensitive
     * - That the search will still return exact matches
     * - That the search is only looking at the regIdentifier field. Our test reg.
     *      CBDL20 has 'cbdl1' in its company name, yet it should not be returned
     *      in the results
     * - That the results are ordered correctly by registration number
     */
    @Test
    public void searchWithinAnyUsingRegistrationNumber() {
        WithinSearch search = new WithinSearch(
                connection.searchHelper,
                "cbdl1",
                "any",
                100
        );
        List<Registration> results = search.execute();

        assertEquals("4 registrations are returned", 4, results.size());

        assertEquals("The first result is CBDL1", "CBDL1", results.get(0).getRegIdentifier());
        assertEquals("The last result is CBDL13", "CBDL13", results.get(3).getRegIdentifier());
        assertEquals("And CBDL20 is not one of them", false, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDL20"::equals));
    }

    /**
     * We're testing a number of things here
     * - That we can do a 'like' search on the term 'smith'
     * - That the search is case insensitive
     * - That the search will still return exact matches
     * - That the search reverted to querying the company name, last name and
     *      postcode fields when it failed to find a matching regIdentifier
     * - That the results are ordered correctly by company name, even though
     *      we entered what based on our test data is a contact name
     */
    @Test
    public void searchWithinAnyUsingAName() {
        WithinSearch search = new WithinSearch(
                connection.searchHelper,
                "smith",
                "any",
                100
        );
        List<Registration> results = search.execute();

        assertEquals("4 registrations are returned", 4, results.size());

        assertEquals("The first result is CBDL11", "CBDL11", results.get(0).getRegIdentifier());
        assertEquals("The last result is CBDL1", "CBDL1", results.get(3).getRegIdentifier());
        assertEquals("And CBDL20 is not one of them", false, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDL20"::equals));
    }

    @Test
    public void limitResults() {
        WithinSearch search = new WithinSearch(
                connection.searchHelper,
                "smith",
                "contactName",
                1
        );

        List<Registration> results = search.execute();

        assertEquals("Only one registration is returned", 1, results.size());
    }

    /**
     * Create a test registration
     *
     * We use this in our subsequent tests to test the search functionality
     */
    private static void createRegistrations() {
        Registration reg = new RegistrationBuilder(RegistrationBuilder.BuildType.LOWER)
                .regIdentifier("CBDL20")
                .companyName("Nothing to see here CBDL1")
                .lastName("Anon")
                .registeredPostcode("NW1 5AH")
                .postalPostcode("NW1 1HT")
                .build();

        connection.dao.insert(reg);

        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.LOWER)
                .regIdentifier("CBDL13")
                .companyName("Test Waste Services")
                .lastName("Smith")
                .registeredPostcode("BS1 5AH")
                .postalPostcode("WA4 1HT")
                .build();

        connection.dao.insert(reg);

        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.LOWER)
                .regIdentifier("CBDL12")
                .companyName("Waste")
                .lastName("Smithy")
                .registeredPostcode("WA4 1HT")
                .postalPostcode("BS1 5AH")
                .build();

        connection.dao.insert(reg);

        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.LOWER)
                .regIdentifier("CBDL11")
                .companyName("Neogeo Consultants for waste")
                .lastName("Heggert-Smith")
                .registeredPostcode("BS1")
                .postalPostcode("BS1")
                .build();

        connection.dao.insert(reg);

        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.LOWER)
                .regIdentifier("CBDL1")
                .companyName("Waste carriers (Westonsmith)")
                .lastName("Westonsmith")
                .registeredPostcode("WBS1 5AH")
                .postalPostcode("WBS1 5AH")
                .build();

        connection.dao.insert(reg);
    }
}
