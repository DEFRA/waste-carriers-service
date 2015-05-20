package uk.gov.ea.wastecarrier.services.mongoDb;

import com.google.common.base.Optional;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import org.joda.time.DateTime;
import uk.gov.ea.wastecarrier.services.core.FinanceDetails;
import uk.gov.ea.wastecarrier.services.core.Order;
import uk.gov.ea.wastecarrier.services.core.OrderItem;
import uk.gov.ea.wastecarrier.services.core.Registration;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by sammoth on 19/05/15.
 */
public class CopyCardSearch {
    private final static String COMPANY_CONVICTION_MATCH = "convictionSearchResult.matchResult";
    private final static String KEY_PEOPLE_CONVICTION_MATCH = "keyPeople.convictionSearchResult.matchResult";
    private final static String COPY_CARDS_MATCH = "financeDetails.orders.orderItems.type";

    private SearchHelper searchHelper;
    private Logger log = Logger.getLogger(RegistrationSearch.class.getName());

    public Optional<String> fromDate;
    public Optional<String> toDate;
    public Optional<String> declaredConvictions;
    public Optional<String> convictionCheckMatch;
    public Optional<Integer> resultCount;

    public CopyCardSearch(SearchHelper searchHelper) {

        this.searchHelper = searchHelper;
    }

    public List<Registration> getCopyCardRegistrations() {

        Map<String, Object> queryProps = authorQueryProperties();

        BasicDBObject query = new BasicDBObject();

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

        DBCursor cursor = searchHelper.getRegistrationsCollection().find(query);
        applyResultCount(cursor);

        List<Registration> registrationList = searchHelper.toRegistrationList(cursor);

        List<Registration> correctDateRegList = new ArrayList<Registration>();

        DateTime from = SearchHelper.dateStringToDate(fromDate.get(), false);
        Date dateFrom = from.toDate();
        DateTime until = SearchHelper.dateStringToDate(toDate.get(), true);
        Date dateUntil = until.toDate();

        for (Iterator<Registration> registration = registrationList.iterator(); registration.hasNext();) {
            Registration thisReg = registration.next();
            FinanceDetails financeDetails = thisReg.getFinanceDetails();
            List<Order> orderList = financeDetails.getOrders();
            for (Iterator<Order> order = orderList.iterator(); order.hasNext();) {
                Order thisOrder = order.next();
                List<OrderItem> orderItemList = thisOrder.getOrderItems();
                boolean hasCopyCards = false;
                for (Iterator<OrderItem> orderItem = orderItemList.iterator(); orderItem.hasNext();) {
                    OrderItem thisOrderItem = orderItem.next();
                     if (thisOrderItem.getType().equals(OrderItem.OrderItemType.COPY_CARDS)) {
                        hasCopyCards = true;
                    }
                }
                if (hasCopyCards) {
                    Date lastUpdatedDate = thisOrder.getDateLastUpdated();
                    if (lastUpdatedDate.after(dateFrom) && lastUpdatedDate.before(dateUntil)) {
                        correctDateRegList.add(thisReg);
                    }
                }
            }
        }

        return correctDateRegList;
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
            searchHelper.addOptionalQueryProperty("declaredConvictions", this.declaredConvictions.get(), queryProps);

        Set<String> copyCards = new HashSet<String>();
        copyCards.add("COPY_CARDS");
        searchHelper.addOptionalQueryProperty(COPY_CARDS_MATCH, copyCards, queryProps);

        return queryProps;
    }

}
