package uk.gov.ea.wastecarrier.services.mongoDb;

import com.google.common.base.Optional;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import uk.gov.ea.wastecarrier.services.WasteCarrierService;
import uk.gov.ea.wastecarrier.services.core.Registration;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportingHelper {

    private final static String DATE_FILTER_PROPERTY = "metaData.dateRegistered";

    private QueryHelper queryHelper;
	private Logger log = Logger.getLogger(ReportingHelper.class.getName());

	public Optional<String> fromDate;
	public Optional<String> toDate;
	public Set<String> route;
	public Set<String> status;
	public Set<String> businessType;
    public Set<String> tier;
    public Optional<String> declaredConvictions;
    public Optional<Boolean> criminallySuspect;
    public Optional<Integer> resultCount;

    public ReportingHelper(QueryHelper queryHelper) {

        this.queryHelper = queryHelper;
    }
	
	public List<Registration> getRegistrations() {
		
		Map<String, Object> queryProps = authorQueryProperties();

        BasicDBObject query = new BasicDBObject();

        applyDateFilters(query);

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
		
		DBCursor cursor = queryHelper.getRegistrationsCollection().find(query);
        applyResultCount(cursor);
		
		return queryHelper.toRegistrationList(cursor);
	}

    protected void applyResultCount(DBCursor cursor) {

        if (resultCount.isPresent())
        {
            Integer count = resultCount.get();
            if ( count != null && !count.equals(0))
            {
                cursor.limit(resultCount.get());
            }
        }
    }

    protected void applyDateFilters(BasicDBObject query) {

        String fromString = null;
        String untilString = null;

        if (fromDate.isPresent()) {
            Long from = QueryHelper.dateStringToDate(fromDate.get(), false).getMillis();
            fromString = QueryHelper.timeToDateTimeString(from);
        }

        if (toDate.isPresent()) {
            Long until = QueryHelper.dateStringToDate(toDate.get(), true).getMillis();
            untilString = QueryHelper.timeToDateTimeString(until);
        }

        if (fromString != null && untilString != null) {
            query.append(
                    DATE_FILTER_PROPERTY,
                    new BasicDBObject("$gt", fromString)
                            .append("$lte", untilString));
        } else if (fromString != null) {
            query.append(DATE_FILTER_PROPERTY, new BasicDBObject("$gt", fromString));
        } else if (untilString != null) {
            query.append(DATE_FILTER_PROPERTY, new BasicDBObject("$lte", untilString));
        }

    }

	private Map<String, Object> authorQueryProperties() {
		
		Map<String, Object> queryProps = new HashMap<String, Object>();

        if (declaredConvictions.isPresent())
            queryHelper.addOptionalQueryProperty(
                    "declaredConvictions",
                    this.declaredConvictions.get(),
                    queryProps);

        if (criminallySuspect.isPresent())
            queryHelper.addOptionalQueryProperty(
                    "criminallySuspect",
                    this.criminallySuspect.get(),
                    queryProps);

		queryHelper.addOptionalQueryProperty("metaData.status", this.status, queryProps);
        queryHelper.addOptionalQueryProperty("metaData.route", this.route, queryProps);
        queryHelper.addOptionalQueryProperty("businessType", this.businessType, queryProps);
        queryHelper.addOptionalQueryProperty("tier", this.tier, queryProps);

		return queryProps;
	}

}
