package uk.gov.ea.wastecarrier.services.mongoDb;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import net.vz.mongodb.jackson.JacksonDBCollection;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

import uk.gov.ea.wastecarrier.services.WasteCarrierService;
import uk.gov.ea.wastecarrier.services.core.Registration;

public class ReportingHelper {
	
	private Logger log = Logger.getLogger(WasteCarrierService.class.getName());
	private DatabaseHelper databaseHelper;
	
	public String fromDate;
	public String toDate;
	public Set<String> route;
	public Set<String> status;
	public Set<String> businessType;
	
	public ReportingHelper(DatabaseHelper databaseHelper) {
		this.databaseHelper = databaseHelper;
	}
	
	public List<Registration> getRegistrations() {
		
		Map<String, Object> queryProps = authorQueryProperties();
		Long from = dateStringToLong(fromDate, false);
		Long until = dateStringToLong(toDate, true);
		
		DBCursor cursor = getRegistrationCursorForDateRange("metaData.dateRegistered", from, until, queryProps);
		
		return toRegistrationList(cursor);
	}
	
	protected DBCursor getRegistrationCursorForDateRange(
			String dateProperty,
			long from,
			long until,
			Map<String, Object> queryProps) {
		
		if (dateProperty == null) {
			throw new IllegalArgumentException(
					"A property name must be supplied");
		}
		if (from > until) {
			throw new IllegalArgumentException(
					"from must not be greater than until");
		}
		
		// Date properties in DB are strings in the format: YYYY/MM/DD HH:MM:SS
		String fromString = timeToDateTimeString(from);
		String untilString = timeToDateTimeString(until);
		BasicDBObject query = 
				new BasicDBObject(dateProperty,
						new BasicDBObject("$gt", fromString)
							.append("$lte", untilString));
		
		if (queryProps != null) {
			for (Map.Entry<String, Object> entry : queryProps.entrySet()) {
				Object value = entry.getValue();
				if (value instanceof String[]) {
					BasicDBObject inQuery = new BasicDBObject("$in", value);
					query.append(entry.getKey(), inQuery);
				} else {
					query.append(entry.getKey(), value);
				}
			}
		}

		if (log.isLoggable(Level.INFO)) {
			log.info(query.toString());
		}
		return getRegistrationsCollection().find(query);
	}
	
	private static List<Registration> toRegistrationList(DBCursor cursor) {
	    JacksonDBCollection<Registration, Object> jackColl = JacksonDBCollection
	        .wrap(cursor.getCollection(), Registration.class);
	    net.vz.mongodb.jackson.DBCursor<Registration> jackCursor = new net.vz.mongodb.jackson.DBCursor<Registration>(
	        jackColl, cursor);
	    return toList(jackCursor);
	}
	
	private static <T> List<T> toList(net.vz.mongodb.jackson.DBCursor<T> cursor) {
	    List<T> returnList = new LinkedList<T>();
	    for (T r : cursor) {
	      returnList.add(r);
	    }
	    return returnList;
	}

	private Map<String, Object> authorQueryProperties() {
		
		Map<String, Object> queryProps = new HashMap<String, Object>();

		addOptionalQueryProperty("metaData.status", this.status, queryProps);
		addOptionalQueryProperty("metaData.route", this.route, queryProps);
		addOptionalQueryProperty("businessType", this.businessType, queryProps);

		return queryProps;
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
	
	protected String processQueryValue(String value) {
		return "NULL".equals(value) ? null : value;
	}
	
	private DBCollection getRegistrationsCollection() {
		return getDatabase().getCollection(Registration.COLLECTION_NAME);
	}
	
	private DB getDatabase() {
	    // TODO - Replace/refactor the DatabaseHelper
	    DB db = databaseHelper.getConnection();

	    if (db == null) {
	      // Database connection is null - not available???
	      log.severe("Database not available, check the database is running");
	      throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
	    }
	    if (!db.isAuthenticated()) {
	      log.info("Database not authenticated, access forbidden");
	      throw new WebApplicationException(Status.UNAUTHORIZED);
	    }
	    return db;
	}
	
	private static String timeToDateTimeString(long time) {

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
	private static long dateStringToLong(String date, boolean end) {
		
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
