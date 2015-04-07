package uk.gov.ea.wastecarrier.services.mongoDb;

import com.google.common.base.Optional;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import org.joda.time.DateTime;
import uk.gov.ea.wastecarrier.services.core.Registration;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RegistrationSearch {

    private final static String DATE_FILTER_PROPERTY = "metaData.dateRegistered";
    private final static String COMPANY_CONVICTION_MATCH = "convictionSearchResult.matchResult";
    private final static String KEY_PEOPLE_CONVICTION_MATCH = "keyPeople.convictionSearchResult.matchResult";
    private final static String COPY_CARDS_MATCH = "financeDetails.orders.orderItems.type";

    private SearchHelper searchHelper;
	private Logger log = Logger.getLogger(RegistrationSearch.class.getName());

	public Optional<String> fromDate;
	public Optional<String> toDate;
	public Set<String> route;
	public Set<String> status;
	public Set<String> businessType;
    public Set<String> tier;
    public Set<String> copyCards;
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
                    String key = entry.getKey();
                    BasicDBObject inQuery;
                    //Sam Griffiths 01-04-2015 check here for Copy Cards requests.
                    // requires an $all request instead of an $in
                    if (key.equals(COPY_CARDS_MATCH)) {
                        inQuery = new BasicDBObject("$all", value);
                    } else {
                        inQuery = new BasicDBObject("$in", value);
                    }
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

        DateTime from;
        Date dateFrom = null;
        DateTime until;
        Date dateUntil = null;

        if (fromDate.isPresent()) {
            from = SearchHelper.dateStringToDate(fromDate.get(), false);
            dateFrom = from.toDate();
        }

        if (toDate.isPresent()) {
            until = SearchHelper.dateStringToDate(toDate.get(), true);
            dateUntil = until.toDate();
        }

        if (dateFrom != null && dateUntil != null) {
            query.append(
                    DATE_FILTER_PROPERTY,
                    new BasicDBObject("$gt", dateFrom)
                            .append("$lte", dateUntil));
        } else if (dateFrom != null) {
            query.append(DATE_FILTER_PROPERTY, new BasicDBObject("$gt", dateFrom));
        } else if (dateUntil != null) {
            query.append(DATE_FILTER_PROPERTY, new BasicDBObject("$lte", dateUntil));
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

        //Sam Griffiths add Copy Cards search
        if (!copyCards.isEmpty()) {
            //if the any selection has been made it overrides the others and just returns all instances of copy cards
            if (copyCards.contains("ANY")) {
                copyCards.clear();
            }
            copyCards.add("COPY_CARDS");

        }
        //only perform the query for Copy cards if the None selection hasn't been made
        if (!copyCards.contains("NONE")) {
            searchHelper.addOptionalQueryProperty(COPY_CARDS_MATCH, this.copyCards, queryProps);
        }

		return queryProps;
	}

}
