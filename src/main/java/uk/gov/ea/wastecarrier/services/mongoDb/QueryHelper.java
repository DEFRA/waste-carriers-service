package uk.gov.ea.wastecarrier.services.mongoDb;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import net.vz.mongodb.jackson.JacksonDBCollection;
import uk.gov.ea.wastecarrier.services.core.Registration;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.logging.Logger;

public class QueryHelper {

    private Logger log = Logger.getLogger(QueryHelper.class.getName());
    private DatabaseHelper databaseHelper;

    public QueryHelper(DatabaseHelper databaseHelper) {
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

    protected void addOptionalQueryLikeProperty(
            String propertyName,
            Object propertyValue,
            Map<String, Object> queryProps) {

        if (propertyValue == null || "".equals(propertyValue)) {
            return;
        }

        if (propertyValue instanceof String) {
            String parsedValue = processQueryValue((String) propertyValue);
            queryProps.put(propertyName, "/" + parsedValue + "/");
        } else {
            queryProps.put(propertyName, propertyValue);
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
     * Returns the time in milliseconds for a date in ddmmyyyy format.
     *
     * @param date
     *            The date in ddmmyyyy or dd/mm/yyyy format.
     * @param end
     *            Whether the returned time should be for the end of the day.
     * @return The time in milliseconds for a date in ddmmyyyy or dd/mm/yyyy
     *         format.
     */
    public static long dateStringToLong(String date, boolean end) {

        if (date == null || (date.length() != 8 && date.length() != 10)) {
            throw new IllegalArgumentException("Invalid date string: " + date);
        }
        int day = Integer.parseInt(date.substring(0, 2));
        int start = 2;
        if (date.charAt(start) == '/') {
            start++;
        }
        int month = Integer.parseInt(date.substring(start, start + 2)) - 1;
        start += 2;
        if (date.charAt(start) == '/') {
            start++;
        }
        int year = Integer.parseInt(date.substring(start, start + 4));
        Calendar cal = Calendar.getInstance();

        if (end) {
            cal.set(Calendar.MILLISECOND, 999);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.HOUR_OF_DAY, 23);
        } else {
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.HOUR_OF_DAY, 0);
        }
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.YEAR, year);

        return cal.getTimeInMillis();
    }
}
