package uk.gov.ea.wastecarrier.services;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.gov.ea.wastecarrier.services.core.ConvictionSearchResult;
import uk.gov.ea.wastecarrier.services.core.Entity;
import uk.gov.ea.wastecarrier.services.resources.MatchResource;
import uk.gov.ea.wastecarrier.services.support.EntityBuilder;
import uk.gov.ea.wastecarrier.services.support.EntityMatchingConnectionUtil;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MatchResourceTest {

    private static EntityMatchingConnectionUtil connection;

    private MatchResource resource;

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

    @Before
    public void setupResource() {
        this.resource = new MatchResource(connection.databaseHelper.configuration());
    }

    @Test
    public void matchCompany() {
        ConvictionSearchResult result = this.resource.matchCompany("Isaac and sons", "");

        assertNotNull(result);
        assertEquals("Matched company 'Isaac and sons'", "Isaac and sons", result.matchedName);
    }

    @Test
    public void matchPerson() {
        ConvictionSearchResult result = this.resource.matchPerson("Mark", "Kermode", "");

        assertNotNull(result);
        assertEquals("Matched person 'Mark Kermode'", "Mark Kermode", result.matchedName);
    }

    @Test
    public void matchPersonWithDateOfBirth() {
        ConvictionSearchResult result = this.resource.matchPerson("Jason", "Isaacs", "04-04-1984");

        assertNotNull(result);
        assertEquals("Matched person 'Jason Isaacs'", "Jason Isaacs", result.matchedName);
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

        document = new EntityBuilder(EntityBuilder.BuildType.PERSON)
                .build();
        connection.dao.insert(document);

        document = new EntityBuilder(EntityBuilder.BuildType.PERSON)
                .name("Mark Kermode")
                .build();
        connection.dao.insert(document);

        document = new EntityBuilder(EntityBuilder.BuildType.PERSON)
                .name("Jason Isaacs")
                .dateOfBirth(new Date(449884800000l))
                .build();
        connection.dao.insert(document);
    }
}
