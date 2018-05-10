package uk.gov.ea.wastecarrier.services.search;

import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import uk.gov.ea.wastecarrier.services.core.ConvictionSearchResult;
import uk.gov.ea.wastecarrier.services.core.OrderItem;
import uk.gov.ea.wastecarrier.services.core.Registration;

import java.util.*;
import java.util.logging.Logger;

public class CopyCardSearch {

    private final static String DATE_FILTER_PROPERTY = "financeDetails.orders.dateLastUpdated";
    private final static String COMPANY_CONVICTION_MATCH = "conviction_search_result.match_result";
    private final static String KEY_PEOPLE_CONVICTION_MATCH = "key_people.conviction_search_result.match_result";
    private final static String ORDER_ITEM_TYPE_MATCH = "financeDetails.orders.orderItems.type";

    private SearchHelper searchHelper;
    private Logger log = Logger.getLogger(CopyCardSearch.class.getName());

    private Date fromDate;
    private Date toDate;
    private Boolean declaredConvictions;
    private Boolean convictionCheckMatch;
    private Integer resultCount;

    /**
     * Search for registrations in the database which ordered copy cards between certain dates, and where specified they
     * match a given filter.
     * @param searchHelper
     * @param fromDate order last updated date is equal to or greater than
     * @param toDate order last updated date is equal to or less than
     * @param declaredConvictions registrations with a declared conviction
     * @param convictionCheckMatch registrations with a conviction match (either company or key person)
     * @param resultCount limit number of results to this
     */
    public CopyCardSearch(
            SearchHelper searchHelper,
            String fromDate,
            String toDate,
            Boolean declaredConvictions,
            Boolean convictionCheckMatch,
            Integer resultCount
    ) {
        this.searchHelper = searchHelper;

        this.fromDate = this.searchHelper.dateStringToDate(fromDate, false).toDate();
        this.toDate = this.searchHelper.dateStringToDate(toDate, true).toDate();
        this.declaredConvictions = declaredConvictions;
        this.convictionCheckMatch = convictionCheckMatch;
        this.resultCount = resultCount;
    }

    public List<Registration> execute() {

        JacksonDBCollection<Registration, String> registrations = this.searchHelper.registrationsCollection();

        DBQuery.Query query = DBQuery.and(DBQuery
                .greaterThanEquals(DATE_FILTER_PROPERTY, this.fromDate)
                .lessThanEquals(DATE_FILTER_PROPERTY, this.toDate)
                .is(ORDER_ITEM_TYPE_MATCH, OrderItem.OrderItemType.COPY_CARDS)
        );

        if (this.declaredConvictions) query.and(DBQuery.is("declaredConvictions", "yes"));

        if (this.convictionCheckMatch) {
            query.or(DBQuery.is(COMPANY_CONVICTION_MATCH, ConvictionSearchResult.MatchResult.YES));
            query.or(DBQuery.is(KEY_PEOPLE_CONVICTION_MATCH, ConvictionSearchResult.MatchResult.YES));
        }

        List<Registration> results = new LinkedList<>();

        try {
            if (this.resultCount == 0) {
                results = searchHelper.toList(registrations.find(query));
            } else {
                results = searchHelper.toList(registrations.find(query).limit(this.resultCount));
            }
        } catch (IllegalArgumentException e) {
            log.severe("Caught exception: " + e.getMessage() + " - Cannot find copy cards");
        }

        return results;
    }
}
