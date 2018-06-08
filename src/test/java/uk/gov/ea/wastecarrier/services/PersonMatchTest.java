package uk.gov.ea.wastecarrier.services;

import org.junit.*;
import uk.gov.ea.wastecarrier.services.core.Entity;
import uk.gov.ea.wastecarrier.services.match.PersonMatch;
import uk.gov.ea.wastecarrier.services.support.EntityBuilder;
import uk.gov.ea.wastecarrier.services.support.EntityMatchingConnectionUtil;

import java.util.Date;

import static org.junit.Assert.*;

public class PersonMatchTest {

    private static EntityMatchingConnectionUtil connection;

    @BeforeClass
    public static void setup() {
        connection = new EntityMatchingConnectionUtil();
        createTestData();
    }

    @AfterClass
    public static void tearDown() {
        connection.clean();
    }

    @Test
    public void nullArguments() {
        PersonMatch matcher = new PersonMatch(connection.searchHelper, null, null, null);

        Entity document = matcher.execute();

        assertNull("No match found", document);
    }

    @Test
    public void firstNameOnlyMatch() {
        PersonMatch matcher = new PersonMatch(connection.searchHelper, "Jason", null, null);

        Entity document = matcher.execute();

        assertEquals("Entity matched", "Isaacs, Jason", document.name);
    }

    @Test
    public void lastNameOnlyMatch() {
        PersonMatch matcher = new PersonMatch(connection.searchHelper, null, "Isaacs", null);

        Entity document = matcher.execute();

        assertEquals("Entity matched", "Isaacs, Jason", document.name);
    }

    @Test
    public void fullNameLastThenFirstMatch() {
        PersonMatch matcher = new PersonMatch(connection.searchHelper, "Jason", "Isaacs", null);

        Entity document = matcher.execute();

        assertEquals("Entity matched 'Isaacs, Jason'", "Isaacs, Jason", document.name);
    }

    @Test
    public void fullNameFirstThenLastMatch() {
        PersonMatch matcher = new PersonMatch(connection.searchHelper, "Mark", "Kermode", null);

        Entity document = matcher.execute();

        assertEquals("Entity matched 'Mark Kermode'", "Mark Kermode", document.name);
    }

    @Test
    public void partialNameMatch() {
        PersonMatch matcher = new PersonMatch(connection.searchHelper, "Mark", "Isaacs", null);

        Entity document = matcher.execute();

        assertNull("No match found", document);
    }

    @Test
    public void dateOfBirthOnlyMatch() {
        PersonMatch matcher = new PersonMatch(connection.searchHelper, null, null, new Date(257952324000L));

        Entity document = matcher.execute();

        assertNull("No match found", document);
    }

    @Test
    public void fullNameAndDateOfBirthMatch() {
        Date matchDate = new Date(257952324000L);
        PersonMatch matcher = new PersonMatch(connection.searchHelper, "Jason", "Isaacs", matchDate);

        Entity document = matcher.execute();

        assertEquals("Entity matched", matchDate, document.dateOfBirth);
    }

    private static void createTestData() {
        Entity document = new EntityBuilder(EntityBuilder.BuildType.PERSON)
                .build();
        connection.dao.insert(document);

        document = new EntityBuilder(EntityBuilder.BuildType.PERSON)
                .name("Mark Kermode")
                .build();
        connection.dao.insert(document);

    }
}
