package uk.gov.ea.wastecarrier.services.mongoDb;

import com.google.common.base.Optional;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import uk.gov.ea.wastecarrier.services.core.Registration;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PaymentSearch
{
    private final static String REG_DATE_FILTER_PROPERTY = "metaData.dateRegistered";
    private final static String REG_BALANCE_FILTER_PROPERTY = "financeDetails.balance";
    private final static String PAY_DATE_FILTER_PROPERTY = "financeDetails.payments.dateReceived";
    private final static String ORD_DATE_FILTER_PROPERTY = "financeDetails.orders.dateCreated";
    private final static String PAY_TYPE_FILTER_PROPERTY = "financeDetails.payments.paymentType";
    private final static String ORD_TYPE_FILTER_PROPERTY = "financeDetails.orders.orderItems.type";

    private SearchHelper searchHelper;
    private Logger log = Logger.getLogger(PaymentSearch.class.getName());

    public Optional<String> fromDate;
    public Optional<String> toDate;
    public Set<String> paymentStatuses;
    public Set<String> paymentTypes;
    public Set<String> chargeTypes;
    public Optional<Integer> resultCount;

    public enum PaymentStatus
    {
        AWAITING_PAYMENT,
        FULLY_PAID,
        OVERPAID
    }

    public PaymentSearch(SearchHelper searchHelper)
    {
        this.searchHelper = searchHelper;
    }

    public List<Registration> getRegistrations()
    {
        Map<String, Object> queryProps = authorQueryProperties();

        BasicDBObject query = new BasicDBObject();

        BasicDBList dateFilter = getDateFilter();
        BasicDBList paymentStatusFilter = getPaymentStatusFilter();
        boolean haveDateFilter = !dateFilter.isEmpty();
        boolean havePaymentStatusFilter = !paymentStatusFilter.isEmpty();
        
        if (haveDateFilter && havePaymentStatusFilter)
        {
            BasicDBList list = new BasicDBList();
            list.add(new BasicDBObject("$or", dateFilter));
            list.add(new BasicDBObject("$or", paymentStatusFilter));
            query.append("$and", list);
        }
        else if (haveDateFilter)
        {
            query.append("$or", dateFilter);
        }
        else if (havePaymentStatusFilter)
        {
            query.append("$or", paymentStatusFilter);
        }

        if (queryProps != null)
        {
            for (Map.Entry<String, Object> entry : queryProps.entrySet())
            {
                Object value = entry.getValue();
                if (value instanceof String[])
                {
                    BasicDBObject inQuery = new BasicDBObject("$in", value);
                    query.append(entry.getKey(), inQuery);
                }
                else
                {
                    query.append(entry.getKey(), value);
                }
            }
        }

        if (log.isLoggable(Level.INFO))
        {
            log.info(query.toString());
        }

        DBCursor cursor = searchHelper.getRegistrationsCollection().find(query);
        applyResultCount(cursor);

        return searchHelper.toRegistrationList(cursor);
    }

    protected void applyResultCount(DBCursor cursor)
    {
        if (resultCount.isPresent())
        {
            Integer count = resultCount.get();
            if ( count != null && !count.equals(0))
            {
                cursor.limit(resultCount.get());
            }
        }
    }

    protected BasicDBList getPaymentStatusFilter()
    {
        BasicDBList filter = new BasicDBList();
        if ((paymentStatuses != null) && !paymentStatuses.isEmpty())
        {
            for (String value : paymentStatuses) {
                if (value.equals(PaymentStatus.AWAITING_PAYMENT.name())) {
                    filter.add(new BasicDBObject(REG_BALANCE_FILTER_PROPERTY, new BasicDBObject("$gt", 0)));
                } else if (value.equals(PaymentStatus.FULLY_PAID.name())) {
                    filter.add(new BasicDBObject(REG_BALANCE_FILTER_PROPERTY, 0));
                } else if (value.equals(PaymentStatus.OVERPAID.name())) {
                    filter.add(new BasicDBObject(REG_BALANCE_FILTER_PROPERTY, new BasicDBObject("$lt", 0)));
                }
            }
        }

        return filter;
    }

    protected BasicDBList getDateFilter()
    {
        Date from = null;
        Date until = null;
        BasicDBObject paymentsClause = null;
        BasicDBObject ordersClause = null;

        if (fromDate.isPresent())
        {
            from = SearchHelper.dateStringToDate(fromDate.get(), false).toDate();
        }

        if (toDate.isPresent())
        {
            until = SearchHelper.dateStringToDate(toDate.get(), true).toDate();
        }

        if (from != null && until != null)
        {
            paymentsClause = new BasicDBObject(
                    PAY_DATE_FILTER_PROPERTY,
                    new BasicDBObject("$gte", from).append("$lte", until));
            ordersClause = new BasicDBObject(
                    ORD_DATE_FILTER_PROPERTY,
                    new BasicDBObject("$gte", from).append("$lte", until));
        }
        else if (from != null)
        {
            paymentsClause = new BasicDBObject(
                    PAY_DATE_FILTER_PROPERTY, new BasicDBObject("$gte", from));
            ordersClause = new BasicDBObject(
                    ORD_DATE_FILTER_PROPERTY, new BasicDBObject("$gte", from));

        }
        else if (until != null)
        {
            paymentsClause = new BasicDBObject(
                    PAY_DATE_FILTER_PROPERTY, new BasicDBObject("$lte", until));
            ordersClause = new BasicDBObject(
                    ORD_DATE_FILTER_PROPERTY, new BasicDBObject("$lte", until));
        }

        BasicDBList filter = new BasicDBList();
        if (paymentsClause != null) {
            filter.add(paymentsClause);
        }
        if (ordersClause != null) {
            filter.add(ordersClause);
        }

        return filter;
    }

    private Map<String,Object> authorQueryProperties()
    {
        Map<String, Object> queryProps = new HashMap<String, Object>();

        searchHelper.addOptionalQueryProperty(PAY_TYPE_FILTER_PROPERTY, this.paymentTypes, queryProps);
        searchHelper.addOptionalQueryProperty(ORD_TYPE_FILTER_PROPERTY, this.chargeTypes, queryProps);

        return queryProps;
    }
}