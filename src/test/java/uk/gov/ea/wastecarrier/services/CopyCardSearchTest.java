package uk.gov.ea.wastecarrier.services;

import org.junit.*;
import uk.gov.ea.wastecarrier.services.core.ConvictionSearchResult;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.search.CopyCardSearch;
import uk.gov.ea.wastecarrier.services.support.ConnectionUtil;
import uk.gov.ea.wastecarrier.services.support.RegistrationBuilder;
import uk.gov.ea.wastecarrier.services.support.TestUtil;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CopyCardSearchTest {

    private static ConnectionUtil connection;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @BeforeClass
    public static void runOnceBefore() {
        connection = new ConnectionUtil();
    }

    /**
     * Create test registrations
     *
     * We use this in our subsequent tests to test the search functionality
     */
    @Before
    public void createRegistrations() {
        // These first 3 registrations should never be included in our results
        // Create a registration with no copy cards.
        Registration reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER)
                .regIdentifier("CBDU1")
                .build();
        connection.registrationsDao.insertRegistration(reg);

        // Create registration with copy cards, but its order updated date is before
        // the date filter
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER_COPY)
                .regIdentifier("CBDU2")
                .orderUpdatedDate(TestUtil.fromCurrentDate(0,0,-1))
                .build();
        connection.registrationsDao.insertRegistration(reg);

        // Create registration with copy cards, but its order updated date is after
        // the date filter
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER_COPY)
                .regIdentifier("CBDU3")
                .orderUpdatedDate(TestUtil.fromCurrentDate(0,0,1))
                .build();
        connection.registrationsDao.insertRegistration(reg);

        // Create registration with copy cards, but no declared or matched convictions
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER_COPY)
                .regIdentifier("CBDU4")
                .build();
        connection.registrationsDao.insertRegistration(reg);

        // Create registration with copy cards where a conviction has been declared
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER_COPY)
                .regIdentifier("CBDU5")
                .declaredConvictions("yes")
                .build();
        connection.registrationsDao.insertRegistration(reg);

        // Create registration with copy cards where the company has been flagged
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER_COPY)
                .regIdentifier("CBDU6")
                .companyConvictionMatch(ConvictionSearchResult.MatchResult.YES)
                .build();
        connection.registrationsDao.insertRegistration(reg);

        // Create registration with copy cards where a person has been flagged
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER_COPY)
                .regIdentifier("CBDU7")
                .keyPersonConvictionMatch(ConvictionSearchResult.MatchResult.YES)
                .build();
        connection.registrationsDao.insertRegistration(reg);

        // Create registration with copy cards where a conviction has been declared
        // and it has also been flagged
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER_COPY)
                .regIdentifier("CBDU8")
                .declaredConvictions("yes")
                .keyPersonConvictionMatch(ConvictionSearchResult.MatchResult.YES)
                .build();
        connection.registrationsDao.insertRegistration(reg);
    }

    @Test
    public void searchWithNoFilters() {
        CopyCardSearch search = new CopyCardSearch(
                connection.searchHelper,
                dateFormat.format(new Date()),
                dateFormat.format(new Date()),
                false,
                false,
                0
        );

        List<Registration> results = search.execute();
        String resultRegIdentifier = results.get(0).getRegIdentifier();

        assertEquals("5 registrations are returned", 5, results.size());
        assertEquals("Matching registration is CBDU4", "CBDU4", resultRegIdentifier);
    }

    @Test
    public void searchForHasDeclaredConviction() {
        CopyCardSearch search = new CopyCardSearch(
                connection.searchHelper,
                dateFormat.format(new Date()),
                dateFormat.format(new Date()),
                true,
                false,
                0
        );

        List<Registration> results = search.execute();

        String[] regs = {
                results.get(0).getRegIdentifier(),
                results.get(1).getRegIdentifier()
        };

        assertEquals("2 registrations are returned", 2, results.size());
        assertEquals("One of them is CBDU5", true, Arrays.stream(regs).anyMatch("CBDU5"::equals));
        assertEquals("And the other is CBDU8", true, Arrays.stream(regs).anyMatch("CBDU8"::equals));
    }

    @Test
    public void searchForHasMatchedConvictions() {
        CopyCardSearch search = new CopyCardSearch(
                connection.searchHelper,
                dateFormat.format(new Date()),
                dateFormat.format(new Date()),
                false,
                true,
                0
        );

        List<Registration> results = search.execute();

        String[] regs = {
                results.get(0).getRegIdentifier(),
                results.get(1).getRegIdentifier(),
                results.get(2).getRegIdentifier()
        };

        assertEquals("3 registrations are returned", 3, results.size());
        assertEquals("One of them is CBDU6", true, Arrays.stream(regs).anyMatch("CBDU6"::equals));
        assertEquals("One is CBDU7", true, Arrays.stream(regs).anyMatch("CBDU7"::equals));
        assertEquals("And the other is CBDU8", true, Arrays.stream(regs).anyMatch("CBDU8"::equals));
    }

    @Test
    public void searchForHasDeclaredAndMatchedConviction() {
        CopyCardSearch search = new CopyCardSearch(
                connection.searchHelper,
                dateFormat.format(new Date()),
                dateFormat.format(new Date()),
                true,
                true,
                0
        );

        List<Registration> results = search.execute();
        String resultRegIdentifier = results.get(0).getRegIdentifier();

        assertEquals("Only one registration is returned", 1, results.size());
        assertEquals("Matching registration is CBDU8", "CBDU8", resultRegIdentifier);
    }

    @Test
    public void limitResults() {
        CopyCardSearch search = new CopyCardSearch(
                connection.searchHelper,
                dateFormat.format(new Date()),
                dateFormat.format(new Date()),
                false,
                false,
                1
        );

        List<Registration> results = search.execute();

        assertEquals("Only one registration is returned", 1, results.size());
    }

    /**
     * Deletes any registrations we have created during testing
     */
    @After
    public void deleteRegistration() {
        connection.clean();
    }
}
