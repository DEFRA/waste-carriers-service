package uk.gov.ea.wastecarrier.services.mongoDb;

import com.google.common.base.Optional;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import uk.gov.ea.wastecarrier.services.core.Registration;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RegistrationSearch {

    private final static String DATE_FILTER_PROPERTY = "metaData.dateRegistered";
    private final static String COMPANY_CONVICTION_MATCH = "convictionSearchResult.matchResult";
    private final static String KEY_PEOPLE_CONVICTION_MATCH = "keyPeople.convictionSearchResult.matchResult";

    private SearchHelper searchHelper;
	private Logger log = Logger.getLogger(RegistrationSearch.class.getName());

	public Optional<String> fromDate;
	public Optional<String> toDate;
	public Set<String> route;
	public Set<String> status;
	public Set<String> businessType;
    public Set<String> tier;
    public Optional<String> declaredConvictions;
    public Optional<String> convictionCheckMatch;
    public Optional<Integer> resultCount;

    public RegistrationSearch(SearchHelper searchHelper) {

        this.searchHelper = searchHelper;
    }
	
	public List<Registration> getRegistrations() {
		
		Map<String, Object> queryProps = authorQueryProperties();

        BasicDBObject query = new BasicDBObject();

        applyDateFilters(query);
        applyConvictionMatchFilter(query);

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
		
		DBCursor cursor = searchHelper.getRegistrationsCollection().find(query);
        applyResultCount(cursor);
		
		return searchHelper.toRegistrationList(cursor);
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
            Long from = SearchHelper.dateStringToDate(fromDate.get(), false).getMillis();
            fromString = SearchHelper.timeToDateTimeString(from);
        }

        if (toDate.isPresent()) {
            Long until = SearchHelper.dateStringToDate(toDate.get(), true).getMillis();
            untilString = SearchHelper.timeToDateTimeString(until);
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

    protected void applyConvictionMatchFilter(BasicDBObject query) {

        if (!convictionCheckMatch.isPresent()) {
            return;
        }

        BasicDBList or = new BasicDBList();
        or.add(new BasicDBObject(COMPANY_CONVICTION_MATCH, convictionCheckMatch.get()));
        or.add(new BasicDBObject(KEY_PEOPLE_CONVICTION_MATCH, convictionCheckMatch.get()));

        query.append("$or", or);
    }

	private Map<String, Object> authorQueryProperties() {
		
		Map<String, Object> queryProps = new HashMap<String, Object>();

        if (declaredConvictions.isPresent())
            searchHelper.addOptionalQueryProperty(
                    "declaredConvictions",
                    this.declaredConvictions.get(),
                    queryProps);

		searchHelper.addOptionalQueryProperty("metaData.status", this.status, queryProps);
        searchHelper.addOptionalQueryProperty("metaData.route", this.route, queryProps);
        searchHelper.addOptionalQueryProperty("businessType", this.businessType, queryProps);
        searchHelper.addOptionalQueryProperty("tier", this.tier, queryProps);

		return queryProps;
	}

}
