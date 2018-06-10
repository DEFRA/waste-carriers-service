package uk.gov.ea.wastecarrier.services;

import com.opencsv.CSVReader;
import uk.gov.ea.wastecarrier.services.core.Entity;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class EntityCsvReader {

    private static Logger log = Logger.getLogger(EntityCsvReader.class.getName());

    public List<Entity> read(String path) {
        List<Entity> entities = new ArrayList<>();

        CSVReader reader = null;
        String[] nextLine;

        try {
            reader = new CSVReader(new FileReader(path));
            while ((nextLine = reader.readNext()) != null) {
                if (skipLine(nextLine)) continue;

                entities.add(createEntity(nextLine));
            }
        } catch(FileNotFoundException e) {
            log.severe(String.format("EntityCsvReader - File %s not found: %s", path, e.getMessage()));
        } catch(IOException e) {
            log.severe("EntityCsvReader - Error reading file: " + e.getMessage());
        } finally {
            try {
                if (reader != null) reader.close();
            } catch(IOException e) {
                log.severe("EntityCsvReader - Error closing reader: " + e.getMessage());
            }
        }

        return entities;
    }

    private Boolean skipLine(String[] nextLine) {
        String firstValue = nextLine[0] == null ? "" : nextLine[0].trim();
        String secondValue = nextLine[1] == null ? "" : nextLine[1].trim();

        // Basically we have the header row. The problem is the header row is
        // not always the first row!
        if (firstValue.equalsIgnoreCase("offender") && secondValue.equalsIgnoreCase("birth date")) return true;

        // Only required field is the first field. If its empty ignore the line
        if (firstValue == null || firstValue.isEmpty()) return true;

        return false;
    }

    private Entity createEntity(String[] newLine) {

        Entity doc = new Entity();

        doc.name = newLine[0].trim();
        doc.dateOfBirth = parseDate(newLine[1].trim());
        doc.companyNumber = newLine[2].trim();
        doc.systemFlag = newLine[3].trim();
        doc.incidentNumber = newLine[4].trim();

        return doc;
    }

    private Date parseDate(String dob)
    {
        if (dob == null || dob.trim().isEmpty()) return null;

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        try {
            return formatter.parse(dob);
        }
        catch (ParseException e) {
            // Have a second attempt using a different pattern
            try {
                formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                return formatter.parse(dob);
            } catch (ParseException e1) {
                return null;
            }
        }
    }
}
