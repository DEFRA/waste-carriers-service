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
	private Logger log = Logger.getLogger(WasteCarrierService.class.getName());

	public Optional<String> fromDate;
	public Optional<String> toDate;
	public Set<String> route;
	public Set<String> status;
	public Set<String> businessType;
    public Set<String> tier;
    public Optional<String> declaredConvictions;
    public Optional<Boolean> criminallySuspect;

    public ReportingHelper(QueryHelper queryHelper) {

        this.queryHelper = queryHelper;
    }
	
	public List<Registration> getRegistrations() {
		
		Map<String, Object> queryProps = authorQueryProperties();

        BasicDBObject query = new BasicDBObject();

        applyFromFilter(query);
        applyToDateFilter(query);

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
		
		return queryHelper.toRegistrationList(cursor);
	}

    protected void applyFromFilter(BasicDBObject query) {

        if (fromDate.isPresent()) {
            Long from = QueryHelper.dateStringToLong(fromDate.get(), false);
            String fromString = QueryHelper.timeToDateTimeString(from);
            query.append(DATE_FILTER_PROPERTY, new BasicDBObject("$gt", fromString));
        }
    }

    protected void applyToDateFilter(BasicDBObject query) {

        if (toDate.isPresent()) {
            Long until = QueryHelper.dateStringToLong(toDate.get(), true);

            Long from = 0L;
            if (fromDate.isPresent()){
                from = QueryHelper.dateStringToLong(fromDate.get(), false);
            }

            if (from > 0 && from > until) {
                throw new IllegalArgumentException(
                        "from must not be greater than until");
            }

            String untilString = QueryHelper.timeToDateTimeString(until);
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
