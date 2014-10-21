package uk.gov.ea.wastecarrier.services.mongoDb;

import com.google.common.base.Optional;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import org.joda.time.DateTime;
import uk.gov.ea.wastecarrier.services.WasteCarrierService;
import uk.gov.ea.wastecarrier.services.core.Registration;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PaymentSearch {

    private final static String REG_DATE_FILTER_PROPERTY = "metaData.dateRegistered";
    private final static String REG_BALANCE_FILTER_PROPERTY = "financeDetails.balance";
    private final static String PAY_DATE_FILTER_PROPERTY = "financeDetails.payments.dateReceived";
    private final static String ORD_DATE_FILTER_PROPERTY = "financeDetails.orders.orderItems.dateCreated";
    private final static String PAY_TYPE_FILTER_PROPERTY = "financeDetails.payments.paymentType";
    private final static String ORD_TYPE_FILTER_PROPERTY = "financeDetails.orders.orderItems.description";

    private QueryHelper queryHelper;
    private Logger log = Logger.getLogger(PaymentSearch.class.getName());

    public Optional<String> fromDate;
    public Optional<String> toDate;
    public Set<String> paymentStatuses;
    public Set<String> paymentTypes;
    public Set<String> chargeTypes;
    public Optional<Integer> resultCount;

    public enum PaymentStatus {
        AWAITING_PAYMENT,
        FULLY_PAID,
        OVERPAID
    }

    public PaymentSearch(QueryHelper queryHelper) {
        this.queryHelper = queryHelper;
    }

    public List<Registration> getRegistrations() {

        Map<String, Object> queryProps = authorQueryProperties();

        BasicDBObject query = new BasicDBObject();

        applyDateFilters(query);
        applyPaymentStatusFilters(query);

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

    protected void applyPaymentStatusFilters(BasicDBObject query) {

        if (paymentStatuses == null || paymentStatuses.isEmpty()) {
            return;
        }

        BasicDBList or = new BasicDBList();
        for (String value : paymentStatuses) {
            if (value.equals(PaymentStatus.AWAITING_PAYMENT.name())) {
                or.add(new BasicDBObject(REG_BALANCE_FILTER_PROPERTY, new BasicDBObject("$gt", 0)));
            } else if (value.equals(PaymentStatus.FULLY_PAID.name())) {
                or.add(new BasicDBObject(REG_BALANCE_FILTER_PROPERTY, 0));
            } else if (value.equals(PaymentStatus.OVERPAID.name())) {
                or.add(new BasicDBObject(REG_BALANCE_FILTER_PROPERTY, new BasicDBObject("$lte", 0)));
            }
        }

        if (!or.isEmpty()) {
            query.append("$or", or);
        }
    }

    protected void applyDateFilters(BasicDBObject query) {

        Date from = null;
        Date until = null;

        if (fromDate.isPresent()) {
            from = QueryHelper.dateStringToDate(fromDate.get(), false).toDate();
        }

        if (toDate.isPresent()) {
            until = QueryHelper.dateStringToDate(toDate.get(), true).toDate();
        }

        if (from != null && until != null) {
            query.append(
                    PAY_DATE_FILTER_PROPERTY,
                    new BasicDBObject("$gte", from).append("$lte", until));
        } else if (from != null) {
            query.append(PAY_DATE_FILTER_PROPERTY, new BasicDBObject("$gte", from));
        } else if (until != null) {
            query.append(PAY_DATE_FILTER_PROPERTY, new BasicDBObject("$lte", until));
        }

    }

    private Map<String,Object> authorQueryProperties() {

        Map<String, Object> queryProps = new HashMap<String, Object>();

        queryHelper.addOptionalQueryProperty(PAY_TYPE_FILTER_PROPERTY, this.paymentTypes, queryProps);
        queryHelper.addOptionalQueryLikeProperty(ORD_TYPE_FILTER_PROPERTY, this.chargeTypes, queryProps);

        return queryProps;
    }
}
