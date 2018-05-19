package uk.gov.ea.wastecarrier.services.search;

import java.util.*;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.mongodb.DB;

import org.mongojack.JacksonDBCollection;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;

import uk.gov.ea.wastecarrier.services.dao.ICanGetCollection;
import uk.gov.ea.wastecarrier.services.dao.DatabaseHelper;

public class SearchHelper {

    private static Logger log = Logger.getLogger(SearchHelper.class.getName());

    private DatabaseHelper databaseHelper;
    private ICanGetCollection dao;

    public SearchHelper(DatabaseHelper databaseHelper, ICanGetCollection dao) {
        this.databaseHelper = databaseHelper;
        this.dao = dao;
    }

    public <T> JacksonDBCollection<T, String> getCollection() {
        return this.dao.getCollection();
    }

    public <T> List<T> toList(org.mongojack.DBCursor<T> cursor) {
        List<T> returnList = new LinkedList<>();
        for (T r : cursor) {
            returnList.add(r);
        }
        return returnList;
    }

    private DB getDatabase() {
        // TODO - Replace/refactor the DatabaseHelper
        DB db = databaseHelper.getConnection();

        if (db == null) {
            // Database connection is null - not available???
            log.severe("Database not available, check the database is running");
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
        }
        return db;
    }

    /**
     * Returns a DateTime for a date string formatted as dd/MM/yyyy, dd-MM-yyyy,
     * dd MM yyyy and ddMMyyyy.
     *
     * @param date
     *            The date string to be converted.
     * @param end
     *            Whether the returned time should be for the end of the day.
     * @return A DateTime representing the date string, set to either to start or
     * end of the day.
     */
    public static DateTime dateStringToDate(String date, boolean end) {

        DateTime result = null;

        if (end) {
            result = dateStringToDateTime(date).withTime(23, 59, 59, 999);
        }
        else {
            result = dateStringToDateTime(date).withTimeAtStartOfDay();
        }

        return result;
    }

    public static DateTime dateStringToDateTime(String date) {

        DateTimeParser[] parsers = {
                DateTimeFormat.forPattern("dd/MM/yyyy").getParser(),
                DateTimeFormat.forPattern("dd-MM-yyyy").getParser(),
                DateTimeFormat.forPattern("dd MM yyyy").getParser(),
                DateTimeFormat.forPattern("ddMMyyyy").getParser()
        };

        DateTimeFormatter formatter =
                new DateTimeFormatterBuilder().append(null, parsers).toFormatter();

        DateTime result = null;
        try {
            result = formatter.parseDateTime(date);
        } catch (Exception e) {
            log.warning("Failed to parse date " + date + ". " + e.getMessage());
        }

        return result;
    }

}
