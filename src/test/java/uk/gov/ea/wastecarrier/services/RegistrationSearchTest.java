package uk.gov.ea.wastecarrier.services;

import org.junit.*;
import uk.gov.ea.wastecarrier.services.core.ConvictionSearchResult;
import uk.gov.ea.wastecarrier.services.core.MetaData;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.search.RegistrationSearch;
import uk.gov.ea.wastecarrier.services.support.ConnectionUtil;
import uk.gov.ea.wastecarrier.services.support.RegistrationBuilder;
import uk.gov.ea.wastecarrier.services.support.TestUtil;

import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RegistrationSearchTest {

    private static ConnectionUtil connection;

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
    public void searchWithNoFilters() {
        RegistrationSearch search = new RegistrationSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                null,
                false,
                false,
                0
        );

        List<Registration> results = search.execute();

        assertEquals("8 registration are returned", 8, results.size());
        assertEquals("Of which CBDU1 is NOT included", true, results.stream().map(Registration::getRegIdentifier).noneMatch("CBDU1"::equals));
        assertEquals("Nor is CBDU2", true, results.stream().map(Registration::getRegIdentifier).noneMatch("CBDU2"::equals));
    }

    @Test
    public void searchForRouteAssistedDigital() {
        HashSet<String> routes = new HashSet<>();
        routes.add("ASSISTED_DIGITAL");

        RegistrationSearch search = new RegistrationSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                routes,
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                null,
                false,
                false,
                0
        );

        List<Registration> results = search.execute();

        assertEquals("1 registration is returned", 1, results.size());
        assertEquals("Matching registration is CBDL4", "CBDL4", results.get(0).getRegIdentifier());
    }

    @Test
    public void searchForMultipleRoutes() {
        HashSet<String> routes = new HashSet<>();
        routes.add("ASSISTED_DIGITAL");
        routes.add("DIGITAL");

        RegistrationSearch search = new RegistrationSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                routes,
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                null,
                false,
                false,
                0
        );

        List<Registration> results = search.execute();

        assertEquals("8 registration are returned", 8, results.size());
        assertEquals("Of which CBDU1 is NOT included", true, results.stream().map(Registration::getRegIdentifier).noneMatch("CBDU1"::equals));
        assertEquals("Nor is CBDU2", true, results.stream().map(Registration::getRegIdentifier).noneMatch("CBDU2"::equals));
    }

    @Test
    public void searchForTierLower() {
        HashSet<String> tiers = new HashSet<>();
        tiers.add("LOWER");

        RegistrationSearch search = new RegistrationSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                new HashSet<>(),
                tiers,
                new HashSet<>(),
                new HashSet<>(),
                null,
                false,
                false,
                0
        );

        List<Registration> results = search.execute();

        assertEquals("2 registrations are returned", 2, results.size());
        assertEquals("One of them is CBDL3", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDL3"::equals));
        assertEquals("And the other is CBDL4", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDL4"::equals));
    }

    @Test
    public void searchForMultipleTiers() {
        HashSet<String> tiers = new HashSet<>();
        tiers.add("LOWER");
        tiers.add("UPPER");

        RegistrationSearch search = new RegistrationSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                new HashSet<>(),
                tiers,
                new HashSet<>(),
                new HashSet<>(),
                null,
                false,
                false,
                0
        );

        List<Registration> results = search.execute();

        assertEquals("8 registration are returned", 8, results.size());
        assertEquals("Of which CBDU1 is NOT included", true, results.stream().map(Registration::getRegIdentifier).noneMatch("CBDU1"::equals));
        assertEquals("Nor is CBDU2", true, results.stream().map(Registration::getRegIdentifier).noneMatch("CBDU2"::equals));
    }

    @Test
    public void searchForStatusPending() {
        HashSet<String> statuses = new HashSet<>();
        statuses.add("PENDING");

        RegistrationSearch search = new RegistrationSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                new HashSet<>(),
                new HashSet<>(),
                statuses,
                new HashSet<>(),
                null,
                false,
                false,
                0
        );

        List<Registration> results = search.execute();

        assertEquals("1 registration is returned", 1, results.size());
        assertEquals("Matching registration is CBDU5", "CBDU5", results.get(0).getRegIdentifier());
    }

    @Test
    public void searchForMultipleStatuses() {
        HashSet<String> statuses = new HashSet<>();
        statuses.add("PENDING");
        statuses.add("REVOKED");

        RegistrationSearch search = new RegistrationSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                new HashSet<>(),
                new HashSet<>(),
                statuses,
                new HashSet<>(),
                null,
                false,
                false,
                0
        );

        List<Registration> results = search.execute();

        assertEquals("2 registrations are returned", 2, results.size());
        assertEquals("One of them is CBDU5", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDU5"::equals));
        assertEquals("And the other is CBDU6", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDU6"::equals));
    }

    @Test
    public void searchForBusinessTypeCharity() {
        HashSet<String> businessTypes = new HashSet<>();
        businessTypes.add("charity");

        RegistrationSearch search = new RegistrationSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                businessTypes,
                null,
                false,
                false,
                0
        );

        List<Registration> results = search.execute();

        assertEquals("1 registration is returned", 1, results.size());
        assertEquals("Matching registration is CBDL3", "CBDL3", results.get(0).getRegIdentifier());
    }

    @Test
    public void searchForMultipleBusinessTypes() {
        HashSet<String> businessTypes = new HashSet<>();
        businessTypes.add("charity");
        businessTypes.add("publicBody");

        RegistrationSearch search = new RegistrationSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                businessTypes,
                null,
                false,
                false,
                0
        );

        List<Registration> results = search.execute();

        assertEquals("2 registrations are returned", 2, results.size());
        assertEquals("One of them is CBDL3", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDL3"::equals));
        assertEquals("And the other is CBDU8", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDU8"::equals));
    }

    @Test
    public void searchForHasDeclaredConviction() {
        RegistrationSearch search = new RegistrationSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                null,
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
        RegistrationSearch search = new RegistrationSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                null,
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
        RegistrationSearch search = new RegistrationSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                null,
                true,
                true,
                0
        );

        List<Registration> results = search.execute();

        assertEquals("Only one registration is returned", 1, results.size());
        assertEquals("Matching registration is CBDU8", "CBDU8", results.get(0).getRegIdentifier());
    }

    @Test
    public void searchForMultipleTypes() {
        HashSet<String> routes = new HashSet<>();
        routes.add("ASSISTED_DIGITAL");
        routes.add("DIGITAL");

        HashSet<String> tiers = new HashSet<>();
        tiers.add("LOWER");
        tiers.add("UPPER");

        HashSet<String> statuses = new HashSet<>();
        statuses.add("ACTIVE");
        statuses.add("PENDING");

        HashSet<String> businessTypes = new HashSet<>();
        businessTypes.add("charity");
        businessTypes.add("publicBody");

        RegistrationSearch search = new RegistrationSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                routes,
                tiers,
                statuses,
                businessTypes,
                null,
                true,
                true,
                0
        );

        List<Registration> results = search.execute();

        assertEquals("1 registration is returned", 1, results.size());
        assertEquals("Matching registration is CBDU8", "CBDU8", results.get(0).getRegIdentifier());
    }

    @Test
    public void searchForCopyCardNew() {
        RegistrationSearch search = new RegistrationSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                "NEW",
                false,
                false,
                0
        );

        List<Registration> results = search.execute();

        assertEquals("1 registration is returned", 1, results.size());
        assertEquals("Matching registration is CBDU9", "CBDU9", results.get(0).getRegIdentifier());
    }

    @Test
    public void searchForCopyCardRenew() {
        RegistrationSearch search = new RegistrationSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                "RENEW",
                false,
                false,
                0
        );

        List<Registration> results = search.execute();

        assertEquals("1 registration is returned", 1, results.size());
        assertEquals("Matching registration is CBDU10", "CBDU10", results.get(0).getRegIdentifier());
    }

    @Test
    public void searchForCopyCardAny() {
        RegistrationSearch search = new RegistrationSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                "ANY",
                false,
                false,
                0
        );

        List<Registration> results = search.execute();

        assertEquals("2 registrations are returned", 2, results.size());
        assertEquals("One of them is CBDU9", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDU9"::equals));
        assertEquals("And the other is CBDU10", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDU10"::equals));
    }

    @Test
    public void searchForCopyCardNone() {
        RegistrationSearch search = new RegistrationSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                "NONE",
                false,
                false,
                0
        );

        List<Registration> results = search.execute();

        assertEquals("4 registrations are returned", 4, results.size());
        assertEquals("Of which CBDU9 is NOT included", true, results.stream().map(Registration::getRegIdentifier).noneMatch("CBDU9"::equals));
        assertEquals("Nor is CBDU10", true, results.stream().map(Registration::getRegIdentifier).noneMatch("CBDU10"::equals));
    }

    @Test
    public void limitResults() {
        RegistrationSearch search = new RegistrationSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                null,
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
        // These first 2 registrations should never be included in our results
        // Create a new registration, but its dates are before the date filters
        // in RegistrationSearch
        Registration reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER)
                .regIdentifier("CBDU1")
                .dateRegistered(TestUtil.fromCurrentDate(0, 0, -1))
                .build();
        connection.registrationsDao.insertRegistration(reg);

        // Create a new registration, but its dates are after the date filters
        // in RegistrationSearch
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER)
                .regIdentifier("CBDU2")
                .dateRegistered(TestUtil.fromCurrentDate(0, 0, 1))
                .build();
        connection.registrationsDao.insertRegistration(reg);

        // Create a new CHARITY lower tier ACTIVE registration
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.LOWER)
                .regIdentifier("CBDL3")
                .businessType("charity")
                .build();
        connection.registrationsDao.insertRegistration(reg);

        // Create a new ASSISTED_DIGITAL registration
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.LOWER)
                .regIdentifier("CBDL4")
                .registrationRoute(MetaData.RouteType.ASSISTED_DIGITAL)
                .build();
        connection.registrationsDao.insertRegistration(reg);

        // Create registration where a conviction has been declared and status
        // is PENDING
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER)
                .regIdentifier("CBDU5")
                .registrationStatus(MetaData.RegistrationStatus.PENDING)
                .declaredConvictions("yes")
                .build();
        connection.registrationsDao.insertRegistration(reg);

        // Create registration where the company has been flagged and the status
        // is REVOKED
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER)
                .regIdentifier("CBDU6")
                .registrationStatus(MetaData.RegistrationStatus.REVOKED)
                .companyConvictionMatch(ConvictionSearchResult.MatchResult.YES)
                .build();
        connection.registrationsDao.insertRegistration(reg);

        // Create registration where a person has been flagged
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER)
                .regIdentifier("CBDU7")
                .keyPersonConvictionMatch(ConvictionSearchResult.MatchResult.YES)
                .build();
        connection.registrationsDao.insertRegistration(reg);

        // Create a PUBLICBODY registration where a conviction has been declared
        // and it has also been flagged
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER)
                .regIdentifier("CBDU8")
                .businessType("publicBody")
                .declaredConvictions("yes")
                .keyPersonConvictionMatch(ConvictionSearchResult.MatchResult.YES)
                .build();
        connection.registrationsDao.insertRegistration(reg);

        // Create a new registration with a copy card
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER, true)
                .regIdentifier("CBDU9")
                .build();
        connection.registrationsDao.insertRegistration(reg);

        // Create a IR renewal registration with a copy card
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.IRRENEWAL, true)
                .regIdentifier("CBDU10")
                .build();
        connection.registrationsDao.insertRegistration(reg);

    }
}
