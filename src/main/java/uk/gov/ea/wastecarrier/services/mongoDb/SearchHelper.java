package uk.gov.ea.wastecarrier.services.mongoDb;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import net.vz.mongodb.jackson.JacksonDBCollection;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import uk.gov.ea.wastecarrier.services.core.Registration;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.logging.Logger;

public class SearchHelper {

    private static Logger log = Logger.getLogger(SearchHelper.class.getName());
    private DatabaseHelper databaseHelper;

    public SearchHelper(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public List<Registration> toRegistrationList(DBCursor cursor) {
        JacksonDBCollection<Registration, Object> jackColl = JacksonDBCollection
                .wrap(cursor.getCollection(), Registration.class);
        net.vz.mongodb.jackson.DBCursor<Registration> jackCursor = new net.vz.mongodb.jackson.DBCursor<Registration>(
                jackColl, cursor);
        return toList(jackCursor);
    }

    private <T> List<T> toList(net.vz.mongodb.jackson.DBCursor<T> cursor) {
        List<T> returnList = new LinkedList<T>();
        for (T r : cursor) {
            returnList.add(r);
        }
        return returnList;
    }

    protected void addOptionalQueryProperty(
            String propertyName,
            Collection<String> propertyValue,
            Map<String, Object> queryProps) {

        if (propertyValue == null || propertyValue.size() == 0) {
            return;
        }

        if (propertyValue.size() == 1) {
            String value = propertyValue.iterator().next();
            if (value == null || "".equals(value)) {
                return;
            }
            queryProps.put(propertyName, processQueryValue(value));
            return;
        }

        String[] processed = new String[propertyValue.size()];
        int index = 0;
        for (String value : propertyValue) {
            processed[index] = processQueryValue(value);
            index++;
        }
        queryProps.put(propertyName, processed);
    }

    protected void addOptionalQueryProperty(
            String propertyName,
            Object propertyValue,
            Map<String, Object> queryProps) {

        if (propertyValue == null || "".equals(propertyValue)) {
            return;
        }

        if (propertyValue instanceof String) {
            queryProps.put(propertyName, processQueryValue((String) propertyValue));
        } else {
            queryProps.put(propertyName, propertyValue);
        }

    }

    protected void applyOptionalQueryLikeProperties(
            String propertyName,
            Collection<String> propertyValue,
            BasicDBObject query) {

        if (propertyValue == null || propertyValue.size() == 0) {
            return;
        }

        if (propertyValue.size() == 1) {
            String value = propertyValue.iterator().next();
            if (value == null || "".equals(value)) {
                return;
            }
            String parsedValue = processQueryValue(value);
            query.append(propertyName, java.util.regex.Pattern.compile(parsedValue));
            return;
        }

        for (String value : propertyValue) {

            String parsedValue = processQueryValue(value);

            // For like you have to pass the value in via a regex in order for
            // the resultant query to be properly formatted. Previously we concatenated
            // / + value + / which just resulted in Mongo looking for "/value/" and
            // obviously never finding anything. We essentially need to get it to just be
            // /value/ (no ") for the query to work and this should do that.
            BasicDBObject inQuery = new BasicDBObject(
                    "$in",
                    java.util.regex.Pattern.compile(parsedValue)
            );

            query.append(propertyName, inQuery);
        }
    }

    private String processQueryValue(String value) {
        return "NULL".equals(value) ? null : value;
    }

    protected DBCollection getRegistrationsCollection() {
        return getDatabase().getCollection(Registration.COLLECTION_NAME);
    }

    private DB getDatabase() {
        // TODO - Replace/refactor the DatabaseHelper
        DB db = databaseHelper.getConnection();

        if (db == null) {
            // Database connection is null - not available???
            log.severe("Database not available, check the database is running");
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
        }
        if (!db.isAuthenticated()) {
            log.info("Database not authenticated, access forbidden");
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        return db;
    }

    public static String timeToDateTimeString(long time) {

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        StringBuilder dateBuilder = new StringBuilder();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);

        dateBuilder.append(year + "/");
        if (month < 10) {
            dateBuilder.append("0");
        }
        dateBuilder.append(month + "/");
        if (day < 10) {
            dateBuilder.append("0");
        }
        dateBuilder.append(day + " ");
        if (hour < 10) {
            dateBuilder.append("0");
        }
        dateBuilder.append(hour + ":");
        if (minute < 10) {
            dateBuilder.append("0");
        }
        dateBuilder.append(minute + ":");
        if (second < 10) {
            dateBuilder.append("0");
        }
        dateBuilder.append(second);

        return dateBuilder.toString();
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
