package uk.gov.ea.wastecarrier.services.search;

import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import uk.gov.ea.wastecarrier.services.core.Registration;

import java.util.*;
import java.util.logging.Logger;

public class PaymentSearch
{
    private final static String HAS_PAYMENTS = "financeDetails.payments";
    private final static String REG_BALANCE_FILTER_PROPERTY = "financeDetails.balance";
    private final static String PAY_DATE_FILTER_PROPERTY = "financeDetails.payments.dateReceived";
    private final static String ORD_DATE_FILTER_PROPERTY = "financeDetails.orders.dateCreated";
    private final static String PAY_TYPE_FILTER_PROPERTY = "financeDetails.payments.paymentType";
    private final static String ORD_TYPE_FILTER_PROPERTY = "financeDetails.orders.orderItems.type";

    private SearchHelper searchHelper;
    private Logger log = Logger.getLogger(PaymentSearch.class.getName());

    private Date fromDate;
    private Date toDate;
    private PaymentStatus paymentStatus;
    private Set<String> paymentTypes;
    private Set<String> chargeTypes;
    private Integer resultCount;

    public enum PaymentStatus {
        AWAITING_PAYMENT,
        FULLY_PAID,
        OVERPAID
    }

    public PaymentSearch(SearchHelper searchHelper,
                         String fromDate,
                         String toDate,
                         String paymentStatus,
                         Set<String> paymentTypes,
                         Set<String> chargeTypes,
                         Integer resultCount
    ) {
        this.searchHelper = searchHelper;

        this.fromDate = this.searchHelper.dateStringToDate(fromDate, false).toDate();
        this.toDate = this.searchHelper.dateStringToDate(toDate, true).toDate();
        this.paymentStatus = PaymentStatus.valueOf(paymentStatus);
        this.paymentTypes = paymentTypes;
        this.chargeTypes = chargeTypes;
        this.resultCount = resultCount;
    }

    public List<Registration> execute() {

        JacksonDBCollection<Registration, String> registrations = this.searchHelper.registrationsCollection();

        DBQuery.Query query = DBQuery.and(DBQuery
                .exists(HAS_PAYMENTS)
                .greaterThanEquals(PAY_DATE_FILTER_PROPERTY, this.fromDate)
                .lessThanEquals(PAY_DATE_FILTER_PROPERTY, this.toDate)
                .greaterThanEquals(ORD_DATE_FILTER_PROPERTY, this.fromDate)
                .lessThanEquals(ORD_DATE_FILTER_PROPERTY, this.toDate)
        );

        if (!this.paymentTypes.isEmpty()) query.in(PAY_TYPE_FILTER_PROPERTY, this.paymentTypes);

        if (!this.chargeTypes.isEmpty()) query.in(ORD_TYPE_FILTER_PROPERTY, this.chargeTypes);


        query.and(paymentStatusQuery());

        List<Registration> results = new LinkedList<>();

        try {
            if (this.resultCount == 0) {
                results = searchHelper.toList(registrations.find(query));
            } else {
                results = searchHelper.toList(registrations.find(query).limit(this.resultCount));
            }
        } catch (IllegalArgumentException e) {
            log.severe("Caught exception: " + e.getMessage() + " - Cannot find payments");
        }

        return results;
    }

    private DBQuery.Query paymentStatusQuery() {

        DBQuery.Query query = null;

        switch (this.paymentStatus) {
            case AWAITING_PAYMENT:
                query = DBQuery.greaterThan(REG_BALANCE_FILTER_PROPERTY, 0);
                break;
            case FULLY_PAID:
                query = DBQuery.is(REG_BALANCE_FILTER_PROPERTY, 0);
                break;
            case OVERPAID:
                query = DBQuery.lessThan(REG_BALANCE_FILTER_PROPERTY, 0);
                break;
        }

        return query;
    }
}
