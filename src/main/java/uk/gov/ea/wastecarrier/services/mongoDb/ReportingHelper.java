package uk.gov.ea.wastecarrier.services.mongoDb;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import uk.gov.ea.wastecarrier.services.WasteCarrierService;
import uk.gov.ea.wastecarrier.services.core.Registration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportingHelper {

    private QueryHelper queryHelper;
	private Logger log = Logger.getLogger(WasteCarrierService.class.getName());

	public String fromDate;
	public String toDate;
	public Set<String> route;
	public Set<String> status;
	public Set<String> businessType;
    public Set<String> tier;

    public ReportingHelper(QueryHelper queryHelper) {

        this.queryHelper = queryHelper;
    }
	
	public List<Registration> getRegistrations() {
		
		Map<String, Object> queryProps = authorQueryProperties();
		Long from = QueryHelper.dateStringToLong(fromDate, false);
		Long until = QueryHelper.dateStringToLong(toDate, true);
		
		DBCursor cursor = getRegistrationCursorForDateRange("metaData.dateRegistered", from, until, queryProps);
		
		return queryHelper.toRegistrationList(cursor);
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
		String fromString = QueryHelper.timeToDateTimeString(from);
		String untilString = QueryHelper.timeToDateTimeString(until);
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
		return queryHelper.getRegistrationsCollection().find(query);
	}

	private Map<String, Object> authorQueryProperties() {
		
		Map<String, Object> queryProps = new HashMap<String, Object>();

		queryHelper.addOptionalQueryProperty("metaData.status", this.status, queryProps);
        queryHelper.addOptionalQueryProperty("metaData.route", this.route, queryProps);
        queryHelper.addOptionalQueryProperty("businessType", this.businessType, queryProps);
        queryHelper.addOptionalQueryProperty("tier", this.tier, queryProps);

		return queryProps;
	}
	

}
