package uk.gov.ea.wastecarrier.services;

import org.junit.*;
import uk.gov.ea.wastecarrier.services.core.ConvictionSearchResult;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.search.CopyCardSearch;
import uk.gov.ea.wastecarrier.services.support.RegistrationsConnectionUtil;
import uk.gov.ea.wastecarrier.services.support.RegistrationBuilder;
import uk.gov.ea.wastecarrier.services.support.TestUtil;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class CopyCardSearchTest {

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

    @Test
    public void searchWithNoFilters() {
        CopyCardSearch search = new CopyCardSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                false,
                false,
                0
        );

        List<Registration> results = search.execute();

        assertEquals("5 registrations are returned", 5, results.size());
    }

    @Test
    public void searchForHasDeclaredConviction() {
        CopyCardSearch search = new CopyCardSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                true,
                false,
                0
        );

        List<Registration> results = search.execute();

        assertEquals("2 registrations are returned", 2, results.size());
        assertEquals("One of them is CBDU5", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDU5"::equals));
        assertEquals("And the other is CBDU8", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDU8"::equals));
    }

    @Test
    public void searchForHasMatchedConvictions() {
        CopyCardSearch search = new CopyCardSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                false,
                true,
                0
        );

        List<Registration> results = search.execute();

        assertEquals("3 registrations are returned", 3, results.size());
        assertEquals("One of them is CBDU6", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDU6"::equals));
        assertEquals("One is CBDU7", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDU7"::equals));
        assertEquals("And the other is CBDU8", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDU8"::equals));
    }

    @Test
    public void searchForHasDeclaredAndMatchedConviction() {
        CopyCardSearch search = new CopyCardSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                true,
                true,
                0
        );

        List<Registration> results = search.execute();

        assertEquals("Only one registration is returned", 1, results.size());
        assertEquals("Matching registration is CBDU8", "CBDU8", results.get(0).getRegIdentifier());
    }

    @Test
    public void limitResults() {
        CopyCardSearch search = new CopyCardSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                false,
                false,
                1
        );

        List<Registration> results = search.execute();

        assertEquals("Only one registration is returned", 1, results.size());
    }

    /**
     * Create test registrations
     *
     * We use this in our tests to check the search functionality
     */
    private static void createRegistrations() {
        // These first 3 registrations should never be included in our results
        // Create a registration with no copy cards.
        Registration reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER)
                .regIdentifier("CBDU1")
                .build();
        connection.dao.insert(reg);

        // Create registration with copy cards, but its order updated date is before
        // the date filter
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER, true)
                .regIdentifier("CBDU2")
                .orderUpdatedDate(TestUtil.fromCurrentDate(0,0,-1))
                .build();
        connection.dao.insert(reg);

        // Create registration with copy cards, but its order updated date is after
        // the date filter
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER, true)
                .regIdentifier("CBDU3")
                .orderUpdatedDate(TestUtil.fromCurrentDate(0,0,1))
                .build();
        connection.dao.insert(reg);

        // Create registration with copy cards, but no declared or matched convictions
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER, true)
                .regIdentifier("CBDU4")
                .build();
        connection.dao.insert(reg);

        // Create registration with copy cards where a conviction has been declared
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER, true)
                .regIdentifier("CBDU5")
                .declaredConvictions("yes")
                .build();
        connection.dao.insert(reg);

        // Create registration with copy cards where the company has been flagged
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER, true)
                .regIdentifier("CBDU6")
                .companyConvictionMatch(ConvictionSearchResult.MatchResult.YES)
                .build();
        connection.dao.insert(reg);

        // Create registration with copy cards where a person has been flagged
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER, true)
                .regIdentifier("CBDU7")
                .keyPersonConvictionMatch(ConvictionSearchResult.MatchResult.YES)
                .build();
        connection.dao.insert(reg);

        // Create registration with copy cards where a conviction has been declared
        // and it has also been flagged
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER, true)
                .regIdentifier("CBDU8")
                .declaredConvictions("yes")
                .keyPersonConvictionMatch(ConvictionSearchResult.MatchResult.YES)
                .build();
        connection.dao.insert(reg);
    }
}
