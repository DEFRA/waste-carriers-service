package uk.gov.ea.wastecarrier.services;

import org.junit.*;
import uk.gov.ea.wastecarrier.services.core.Payment;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.search.PaymentSearch;
import uk.gov.ea.wastecarrier.services.support.RegistrationsConnectionUtil;
import uk.gov.ea.wastecarrier.services.support.RegistrationBuilder;
import uk.gov.ea.wastecarrier.services.support.TestUtil;

import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PaymentSearchTest {

    private static RegistrationsConnectionUtil connection;

    @BeforeClass
    public static void setup() {
        connection = new RegistrationsConnectionUtil();
        createRegistrations();
    }

    /**
     * Deletes any registrations we have created during testing
     */
    @AfterClass
    public static void tearDown() {
        connection.clean();
    }

    @Test
    public void searchWithNoFiltersForFullyPaid() {
        PaymentSearch search = new PaymentSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                "FULLY_PAID",
                new HashSet<>(),
                new HashSet<>(),
                0
        );

        List<Registration> results = search.execute();

        assertEquals("4 registrations are returned", 4, results.size());
        assertEquals("One of them is CBDU4", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDU4"::equals));
        assertEquals("One is CBDU7", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDU7"::equals));
        assertEquals("One is CBDU8", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDU8"::equals));
        assertEquals("And the other is CBDU9", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDU9"::equals));
    }

    @Test
    public void searchWithNoFiltersForAwaitingPayment() {
        PaymentSearch search = new PaymentSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                "AWAITING_PAYMENT",
                new HashSet<>(),
                new HashSet<>(),
                0
        );

        List<Registration> results = search.execute();

        assertEquals("1 registration is returned", 1, results.size());
        assertEquals("Matching registration is CBDU5", "CBDU5", results.get(0).getRegIdentifier());
    }

    @Test
    public void searchWithNoFiltersForOverpaid() {
        PaymentSearch search = new PaymentSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                "OVERPAID",
                new HashSet<>(),
                new HashSet<>(),
                0
        );

        List<Registration> results = search.execute();

        assertEquals("1 registration is returned", 1, results.size());
        assertEquals("Matching registration is CBDU6", "CBDU6", results.get(0).getRegIdentifier());
    }

    @Test
    public void searchForChargeTypeNew() {
        HashSet<String> chargeTypes = new HashSet<>();
        chargeTypes.add("NEW");

        PaymentSearch search = new PaymentSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                "FULLY_PAID",
                new HashSet<>(),
                chargeTypes,
                0
        );

        List<Registration> results = search.execute();

        assertEquals("2 registrations are returned", 3, results.size());
        assertEquals("One of them is CBDU4", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDU4"::equals));
        assertEquals("One is CBDU8", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDU8"::equals));
        assertEquals("And the other is CBDU9", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDU9"::equals));
    }

    @Test
    public void searchForChargeTypeRenew() {
        HashSet<String> chargeTypes = new HashSet<>();
        chargeTypes.add("RENEW");

        PaymentSearch search = new PaymentSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                "FULLY_PAID",
                new HashSet<>(),
                chargeTypes,
                0
        );

        List<Registration> results = search.execute();

        assertEquals("1 registration is returned", 1, results.size());
        assertEquals("Matching registration is CBDU7", "CBDU7", results.get(0).getRegIdentifier());
    }

    @Test
    public void searchForChargeTypeCopyCards() {
        HashSet<String> chargeTypes = new HashSet<>();
        chargeTypes.add("COPY_CARDS");

        PaymentSearch search = new PaymentSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                "FULLY_PAID",
                new HashSet<>(),
                chargeTypes,
                0
        );

        List<Registration> results = search.execute();

        assertEquals("1 registration is returned", 1, results.size());
        assertEquals("Matching registration is CBDU8", "CBDU8", results.get(0).getRegIdentifier());
    }

    @Test
    public void searchForMultipleChargeTypes() {
        HashSet<String> chargeTypes = new HashSet<>();
        chargeTypes.add("NEW");
        chargeTypes.add("RENEW");
        chargeTypes.add("COPY_CARDS");

        PaymentSearch search = new PaymentSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                "FULLY_PAID",
                new HashSet<>(),
                chargeTypes,
                0
        );

        List<Registration> results = search.execute();

        assertEquals("4 registrations are returned", 4, results.size());
        assertEquals("One of them is CBDU4", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDU4"::equals));
        assertEquals("One is CBDU7", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDU7"::equals));
        assertEquals("One is CBDU8", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDU8"::equals));
        assertEquals("And the other is CBDU9", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDU9"::equals));
    }

    @Test
    public void searchForPaymentTypeWorldPay() {
        HashSet<String> paymentTypes = new HashSet<>();
        paymentTypes.add("WORLDPAY");

        PaymentSearch search = new PaymentSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                "FULLY_PAID",
                paymentTypes,
                new HashSet<>(),
                0
        );

        List<Registration> results = search.execute();

        assertEquals("1 registration is returned", 1, results.size());
        assertEquals("Matching registration is CBDU4", "CBDU4", results.get(0).getRegIdentifier());
    }

    @Test
    public void searchForPaymentTypeWorldPayMissed() {
        HashSet<String> paymentTypes = new HashSet<>();
        paymentTypes.add("WORLDPAY_MISSED");

        PaymentSearch search = new PaymentSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                "FULLY_PAID",
                paymentTypes,
                new HashSet<>(),
                0
        );

        List<Registration> results = search.execute();

        assertEquals("2 registrations are returned", 2, results.size());
        assertEquals("One of them is CBDU7", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDU7"::equals));
        assertEquals("And the other is CBDU9", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDU9"::equals));
    }

    @Test
    public void searchForPaymentTypeBankTransfer() {
        HashSet<String> paymentTypes = new HashSet<>();
        paymentTypes.add("BANKTRANSFER");

        PaymentSearch search = new PaymentSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                "FULLY_PAID",
                paymentTypes,
                new HashSet<>(),
                0
        );

        List<Registration> results = search.execute();

        assertEquals("1 registration is returned", 1, results.size());
        assertEquals("Matching registration is CBDU8", "CBDU8", results.get(0).getRegIdentifier());
    }

    @Test
    public void searchForMultiplePaymentTypes() {
        HashSet<String> paymentTypes = new HashSet<>();
        paymentTypes.add("WORLDPAY");
        paymentTypes.add("WORLDPAY_MISSED");
        paymentTypes.add("BANKTRANSFER");

        PaymentSearch search = new PaymentSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                "FULLY_PAID",
                paymentTypes,
                new HashSet<>(),
                0
        );

        List<Registration> results = search.execute();

        assertEquals("4 registrations are returned", 4, results.size());
        assertEquals("One of them is CBDU4", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDU4"::equals));
        assertEquals("One is CBDU7", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDU7"::equals));
        assertEquals("One is CBDU8", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDU8"::equals));
        assertEquals("And the other is CBDU9", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDU9"::equals));
    }

    @Test
    public void searchForMultipleTypes() {
        HashSet<String> paymentTypes = new HashSet<>();
        paymentTypes.add("WORLDPAY");
        paymentTypes.add("WORLDPAY_MISSED");

        HashSet<String> chargeTypes = new HashSet<>();
        chargeTypes.add("NEW");
        chargeTypes.add("RENEW");

        PaymentSearch search = new PaymentSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                "FULLY_PAID",
                paymentTypes,
                chargeTypes,
                0
        );

        List<Registration> results = search.execute();

        assertEquals("2 registrations are returned", 3, results.size());
        assertEquals("One is CBDU4", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDU4"::equals));
        assertEquals("And the other is CBDU7", true, results.stream().map(Registration::getRegIdentifier).anyMatch("CBDU7"::equals));
    }

    @Test
    public void limitResults() {
        PaymentSearch search = new PaymentSearch(
                connection.searchHelper,
                TestUtil.dateToday(),
                TestUtil.dateToday(),
                "FULLY_PAID",
                new HashSet<>(),
                new HashSet<>(),
                1
        );

        List<Registration> results = search.execute();

        assertEquals("Only one registration is returned", 1, results.size());
    }

    /**
     * Create test registrations
     *
     * We use this in our tests to check the search functionality
     */
    private static void createRegistrations() {
        // These first 3 registrations should never be included in our results
        // Create a new registration with no payments.
        Registration reg = new RegistrationBuilder(RegistrationBuilder.BuildType.LOWER)
                .regIdentifier("CBDL1")
                .build();
        connection.registrationsDao.insertRegistration(reg);

        // Create a new registration with a payment, but its dates are before the
        // date filters in PaymentSearch
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER)
                .regIdentifier("CBDU2")
                .orderCreatedDate(TestUtil.fromCurrentDate(0,0,-1))
                .paymentReceived(TestUtil.fromCurrentDate(0,0,-1))
                .build();
        connection.registrationsDao.insertRegistration(reg);

        // Create a new registration with a payment, but its dates are after the
        // date filters in PaymentSearch
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER)
                .regIdentifier("CBDU3")
                .orderCreatedDate(TestUtil.fromCurrentDate(0,0,1))
                .paymentReceived(TestUtil.fromCurrentDate(0,0,1))
                .build();
        connection.registrationsDao.insertRegistration(reg);

        // Create a new registration with a payment (fully paid)
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER)
                .regIdentifier("CBDU4")
                .build();
        connection.registrationsDao.insertRegistration(reg);

        // Create a new registration with an under payment (awaiting payment)
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER)
                .regIdentifier("CBDU5")
                .paymentAmount(10000)
                .build();
        connection.registrationsDao.insertRegistration(reg);

        // Create a new registration with an over payment (overpaid)
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER)
                .regIdentifier("CBDU6")
                .paymentAmount(16000)
                .build();
        connection.registrationsDao.insertRegistration(reg);

        // Create an IR renewal registration with a WORLDPAY_MISSED payment (fully paid)
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.IRRENEWAL)
                .regIdentifier("CBDU7")
                .paymentType(Payment.PaymentType.WORLDPAY_MISSED)
                .build();
        connection.registrationsDao.insertRegistration(reg);

        // Create a new registration with a copy card order and a payment (fully paid)
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER, true)
                .regIdentifier("CBDU8")
                .paymentType(Payment.PaymentType.BANKTRANSFER)
                .build();
        connection.registrationsDao.insertRegistration(reg);

        // Create a new registration with a WORLDPAY_MISSED payment (fully paid)
        // Specifically created for searchForMultipleTypes(), we DO NOT expect to
        // see this in its results
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER)
                .regIdentifier("CBDU9")
                .paymentType(Payment.PaymentType.WORLDPAY_MISSED)
                .build();
        connection.registrationsDao.insertRegistration(reg);

    }
}
