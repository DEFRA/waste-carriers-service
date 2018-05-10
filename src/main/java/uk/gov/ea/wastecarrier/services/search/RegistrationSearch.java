package uk.gov.ea.wastecarrier.services.search;

import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import uk.gov.ea.wastecarrier.services.core.ConvictionSearchResult;
import uk.gov.ea.wastecarrier.services.core.OrderItem;
import uk.gov.ea.wastecarrier.services.core.Registration;

import java.util.*;
import java.util.logging.Logger;

public class RegistrationSearch {

    private final static String COMPANY_CONVICTION_MATCH = "conviction_search_result.match_result";
    private final static String KEY_PEOPLE_CONVICTION_MATCH = "key_people.conviction_search_result.match_result";
    private final static String ORDER_ITEM_TYPE_MATCH = "financeDetails.orders.orderItems.type";

    private SearchHelper searchHelper;
    private Logger log = Logger.getLogger(RegistrationSearch.class.getName());

    private Date fromDate;
    private Date toDate;
    private Set<String> routes;
    private Set<String> tiers;
    private Set<String> statuses;
    private Set<String> businessTypes;
    private String copyCardsFilter;
    private Boolean declaredConvictions;
    private Boolean convictionCheckMatch;
    private Integer resultCount;

    public RegistrationSearch(
            SearchHelper searchHelper,
            String fromDate,
            String toDate,
            Set<String> routes,
            Set<String> tiers,
            Set<String> statuses,
            Set<String> businessTypes,
            String copyCardsFilter,
            Boolean declaredConvictions,
            Boolean convictionCheckMatch,
            Integer resultCount
    ) {
        this.searchHelper = searchHelper;

        this.fromDate = this.searchHelper.dateStringToDate(fromDate, false).toDate();
        this.toDate = this.searchHelper.dateStringToDate(toDate, true).toDate();

        this.routes = routes;
        this.tiers = tiers;
        this.statuses = statuses;
        this.businessTypes = businessTypes;
        this.copyCardsFilter = copyCardsFilter;

        this.declaredConvictions = declaredConvictions;
        this.convictionCheckMatch = convictionCheckMatch;
        this.resultCount = resultCount;
    }

    public List<Registration> execute() {

        JacksonDBCollection<Registration, String> registrations = this.searchHelper.registrationsCollection();

        List<Registration> results = new LinkedList<>();

        DBQuery.Query query = DBQuery.and(DBQuery
                .greaterThanEquals("metaData.dateRegistered", this.fromDate)
                .lessThanEquals("metaData.dateRegistered", this.toDate)
        );

        if (!this.routes.isEmpty()) query.in("metaData.route", this.routes);
        if (!this.tiers.isEmpty()) query.in("tier", this.tiers);
        if (!this.statuses.isEmpty()) query.in("metaData.status", this.statuses);
        if (!this.businessTypes.isEmpty()) query.in("businessType", this.businessTypes);
        if (this.copyCardsFilter != null) copyCardsQuery(query);

        if (this.declaredConvictions) query.and(DBQuery.is("declaredConvictions", "yes"));

        if (this.convictionCheckMatch) {
            query.or(DBQuery.is(COMPANY_CONVICTION_MATCH, ConvictionSearchResult.MatchResult.YES));
            query.or(DBQuery.is(KEY_PEOPLE_CONVICTION_MATCH, ConvictionSearchResult.MatchResult.YES));
        }

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

    /**
     * From the frontend registrations reporting screen a user can select one of the following options for the copy card
     * filter
     *
     * - New Registration [NEW]
     * - Any Time [ANY]
     * - Renewal [RENEW]
     * - Ignore Copy Cards [NONE]
     *
     * The values in brackets are what actually gets sent to the waste-carriers-service. (They also have the option to
     * leave it blank, in which case this method should not be called and no filter applied.) What this translates as is
     *
     * - Return only registrations where copy cards were ordered at the same time as a new registration
     * - Return any registrations where copy cards were ordered
     * - Return only registrations where copy cards were ordered at the same time as an IR renewal
     * - Return only registrations where there has been an order but it does not include copy cards
     *
     * This method builds up the necessary query to support all 4 scenarios, then applies it to the current
     * query which is passed in.
     */
    private void copyCardsQuery(DBQuery.Query currentQuery) {

        switch(this.copyCardsFilter) {
            case "NEW":
                currentQuery.and(DBQuery
                        .is(ORDER_ITEM_TYPE_MATCH, OrderItem.OrderItemType.COPY_CARDS)
                );
                currentQuery.and(DBQuery
                        .is(ORDER_ITEM_TYPE_MATCH, OrderItem.OrderItemType.NEW)
                );
                break;
            case "ANY":
                currentQuery.and(
                        DBQuery.is(ORDER_ITEM_TYPE_MATCH, OrderItem.OrderItemType.COPY_CARDS)
                );
                break;
            case "RENEW":
                currentQuery.and(DBQuery
                        .is(ORDER_ITEM_TYPE_MATCH, OrderItem.OrderItemType.COPY_CARDS)
                );
                currentQuery.and(DBQuery
                        .is(ORDER_ITEM_TYPE_MATCH, OrderItem.OrderItemType.RENEW)
                );
                break;
            case "NONE":
                currentQuery.and(DBQuery
                        .exists(ORDER_ITEM_TYPE_MATCH)
                );
                currentQuery.and(DBQuery
                        .notEquals(ORDER_ITEM_TYPE_MATCH, OrderItem.OrderItemType.COPY_CARDS)
                );
                break;
        }
    }
}
