package uk.gov.ea.wastecarrier.services;

import org.junit.*;
import uk.gov.ea.wastecarrier.services.core.Entity;
import uk.gov.ea.wastecarrier.services.match.CompanyMatch;
import uk.gov.ea.wastecarrier.services.support.EntityBuilder;
import uk.gov.ea.wastecarrier.services.support.EntityMatchingConnectionUtil;

import javax.ws.rs.WebApplicationException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class CompanyMatchTest {

    private static EntityMatchingConnectionUtil connection;

    @BeforeClass
    public static void setup() {
        connection = new EntityMatchingConnectionUtil();
        createTestData();
    }

    /**
     * Deletes any documents we have created during testing
     */
    @AfterClass
    public static void tearDown() {
        connection.clean();
    }

    @Test
    public void nullArguments() {
        CompanyMatch matcher = new CompanyMatch(connection.searchHelper, null, null);

        Entity document = matcher.execute();

        assertNull("No entity found", document);
    }

    @Test
    public void nameMatchNoMatch() {
        CompanyMatch matcher = new CompanyMatch(connection.searchHelper, "We eat your waste", null);

        Entity document = matcher.execute();

        assertNull("No entity found", document);
    }

    @Test
    public void nameMatchExact() {
        CompanyMatch matcher = new CompanyMatch(connection.searchHelper, "Isaac and sons", null);

        Entity document = matcher.execute();

        assertNotNull(document);
        assertEquals("Company with exact name of 'Isaac and sons' is found", "Isaac and sons", document.name);
    }

    @Test
    public void nameMatchFuzzy() {
        CompanyMatch matcher = new CompanyMatch(connection.searchHelper, "Isaac", null);

        Entity document = matcher.execute();

        assertNotNull(document);
        assertEquals("Company with name of 'Isaac and sons' is found", "Isaac and sons", document.name);
    }

    @Test
    public void nameMatchIgnoreCommonWords() {
        CompanyMatch matcher = new CompanyMatch(connection.searchHelper, "Isaacs Waste Services Limited", null);

        Entity document = matcher.execute();

        assertNotNull(document);
        assertEquals("Company with name of 'Isaacs Waste Contractors Ltd' is found", "Isaacs Waste Contractors Ltd", document.name);
    }

    @Test
    public void nameMatchEndsWithPeriod() {
        CompanyMatch matcher = new CompanyMatch(connection.searchHelper, "Isaacs Waste Services Ltd.", null);

        Entity document = matcher.execute();

        assertNotNull(document);
        assertEquals("Company with name of 'Isaacs Waste Contractors Ltd' is found", "Isaacs Waste Contractors Ltd", document.name);
    }

    @Test
    public void numberMatchExact() {
        CompanyMatch matcher = new CompanyMatch(connection.searchHelper, null, "12345678");

        Entity document = matcher.execute();

        assertNotNull(document);
        assertEquals("Company with number of '12345678' is found", "12345678", document.companyNumber);
    }

    @Test
    public void numberMatchFuzzy() {
        CompanyMatch matcher = new CompanyMatch(connection.searchHelper, null, "1234");

        Entity document = matcher.execute();

        assertNull("No entity should be found", document);
    }

    @Test
    public void numberMatchIgnoreLeadingZeroes() {
        CompanyMatch matcher = new CompanyMatch(connection.searchHelper, null, "0123456");

        Entity document = matcher.execute();

        assertNotNull(document);
        assertEquals("Company with number of '00123456' is found", "00123456", document.companyNumber);
    }

    /**
     * Create test data to then test the classes search functions
     */
    private static void createTestData() {
        Entity document = new EntityBuilder(EntityBuilder.BuildType.COMPANY)
                .name("Isaac and sons")
                .companyNumber("12345678")
                .build();
        connection.dao.insert(document);

        document = new EntityBuilder(EntityBuilder.BuildType.COMPANY)
                .name("Isaacs Waste Contractors Ltd")
                .companyNumber("00123456")
                .build();
        connection.dao.insert(document);
    }
}
