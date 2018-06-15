package uk.gov.ea.wastecarrier.services;

import org.junit.*;
import uk.gov.ea.wastecarrier.services.core.Entity;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class EntityCsvReaderTest {

    @Test
    public void recordCountIsCorrect() {
        EntityCsvReader reader = new EntityCsvReader();

        List<Entity> results = reader.read("src/test/resources/entity_data/entities.csv");

        assertEquals("Will have read details for 8 entities", 8, results.size());
    }

    @Test
    public void readsCompanyEntriesCorrectly() {
        EntityCsvReader reader = new EntityCsvReader();

        List<Entity> results = reader.read("src/test/resources/entity_data/entities.csv");

        results.removeIf(e -> e.companyNumber.isEmpty());

        assertEquals("Will have read details for 4 companies", 4, results.size());
        assertEquals("The first is 'Test Waste Services Ltd.'", "Test Waste Services Ltd.", results.get(0).name);
    }

    @Test
    public void readsPersonEntriesCorrectly() {
        EntityCsvReader reader = new EntityCsvReader();

        List<Entity> results = reader.read("src/test/resources/entity_data/entities.csv");

        results.removeIf(e -> !e.companyNumber.isEmpty());

        assertEquals("Will have read details for 4 persons", 4, results.size());
        assertEquals("The last is 'Bobby Smith", "Bobby Smith", results.get(3).name);
    }

    @Test
    public void readsDateValuesCorrectly() {
        EntityCsvReader reader = new EntityCsvReader();

        List<Entity> results = reader.read("src/test/resources/entity_data/entities.csv");

        results.removeIf(e -> e.dateOfBirth == null);

        assertEquals("There will be 3 entities with dates of birth", 3, results.size());

        assertEquals(
                "The first has a date of 01/01/1981",
                new Date(347155200000L),
                results.get(0).dateOfBirth
        );
        assertEquals(
                "The second has a date of 02/02/1982",
                new Date(381456000000L),
                results.get(1).dateOfBirth
        );
        assertEquals(
                "The Third has a date of 03/03/1983",
                new Date(415497600000L),
                results.get(2).dateOfBirth
        );
    }
}
