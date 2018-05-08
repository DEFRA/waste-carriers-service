package uk.gov.ea.wastecarrier.services.support;

import uk.gov.ea.wastecarrier.services.core.*;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * A class which implements the builder pattern for generating test
 * registrations. Java does not have default parameters but instead utilises
 * method overloading to achieve the same effect. To make calling code more
 * explicit we implement the builder pattern e.g.

 * Registration reg1 = new RegistrationBuilder().buildLowerTier();
 * Registration reg2 = new RegistrationBuilder().regIdentifier("CBDL1").buildLowerTier();
 * Registration reg3 = new RegistrationBuilder()
 *                          .regIdentifier("CBDL1")
 *                          .accountEmail("joan@example.com")
 *                          .buildLowerTier();
 */
public class RegistrationBuilder {

    private String regIdentifier;
    private String accountEmail = "jason@example.com";
    private String originalRegNumber = "CB/VM8888WW/A001";
    private BuildType buildType;


    public enum BuildType {
        LOWER,
        UPPER,
        IRRENEWAL
    }

    public RegistrationBuilder(BuildType buildType) {
        this.buildType = buildType;
    }

    public RegistrationBuilder regIdentifier(String regIdentifier)
    {
        this.regIdentifier = regIdentifier;
        return this;
    }

    public RegistrationBuilder accountEmail(String accountEmail)
    {
        this.accountEmail = accountEmail;
        return this;
    }

    public RegistrationBuilder originalRegNumber(String originalRegNumber)
    {
        this.originalRegNumber = originalRegNumber;
        return this;
    }

    public Registration build() {
        Registration registration = null;

        switch(this.buildType) {
            case LOWER:
                registration = buildLowerTier();
                break;
            case UPPER:
                registration = buildUpperTier();
                break;
            case IRRENEWAL:
                registration = buildIRRenewal();
                break;
        }
        return registration;
    }

    private Registration buildLowerTier() {
        Registration reg = new Registration();

        reg.setUuid(generateUUID());
        reg.setRegIdentifier(generateRegIdentifier(Registration.RegistrationTier.LOWER));
        reg.setTier(Registration.RegistrationTier.LOWER);
        reg.setBusinessType("soleTrader");
        reg.setOtherBusinesses("no");
        reg.setConstructionWaste("no");
        reg.setCompanyName("WCR Service test LT");
        reg.setFirstName("Jason");
        reg.setLastName("Isaac");
        reg.setPhoneNumber("01179345400");
        reg.setContactEmail("jason@example.com");
        reg.setAccountEmail(this.accountEmail);
        reg.setDeclaration("1");

        ArrayList<Address> addresses = new ArrayList<>();
        addresses.add(generateAddress(Address.addressType.REGISTERED));
        addresses.add(generateAddress(Address.addressType.POSTAL));
        reg.setAddresses(addresses);

        reg.setMetaData(generateMetaData(MetaData.RegistrationStatus.PENDING));

        reg.setFinanceDetails(generateFinanceDetails());

        return reg;
    }

    private Registration buildUpperTier() {
        Registration reg = new Registration();

        reg.setUuid(generateUUID());
        reg.setRegIdentifier(generateRegIdentifier(Registration.RegistrationTier.UPPER));
        reg.setTier(Registration.RegistrationTier.UPPER);
        reg.setBusinessType("soleTrader");
        reg.setOtherBusinesses("no");
        reg.setConstructionWaste("yes");
        reg.setRegistrationType("carrier_broker_dealer");
        reg.setCompanyName("WCR Service test UT");
        reg.setFirstName("Jason");
        reg.setLastName("Isaacs");
        reg.setPhoneNumber("01179345400");
        reg.setContactEmail("jason@example.com");
        reg.setDeclaredConvictions("no");
        reg.setAccountEmail(this.accountEmail);
        reg.setDeclaration("1");
        reg.setExpires_on(generateExpiryDate());

        ArrayList<Address> addresses = new ArrayList<>();
        addresses.add(generateAddress(Address.addressType.REGISTERED));
        addresses.add(generateAddress(Address.addressType.POSTAL));
        reg.setAddresses(addresses);

        ArrayList<KeyPerson> persons = new ArrayList<>();
        persons.add(generateKeyPerson(KeyPerson.PersonType.KEY));
        reg.setKeyPeople(persons);

        reg.setMetaData(generateMetaData(MetaData.RegistrationStatus.PENDING));

        reg.setFinanceDetails(generateFinanceDetails());

        reg.setConvictionSearchResult(generateConvictionSearchResult());

        return reg;
    }

    private Registration buildIRRenewal() {
        Registration reg = buildUpperTier();

        reg.setOriginalDateExpiry(generateOriginalExpiryDate());
        reg.setOriginalRegistrationNumber(this.originalRegNumber);

        return reg;
    }

    private String generateRegIdentifier(Registration.RegistrationTier tierType) {
        if (this.regIdentifier != null) {
            return this.regIdentifier;
        }

        if (tierType == Registration.RegistrationTier.LOWER) {
            return "CBDL1";
        }

        return "CBDU1";
    }

    private Address generateAddress(Address.addressType type) {
        Address addr = new Address();

        addr.setAddressType(type);
        addr.setHouseNumber("123");
        addr.setAddressLine1("Upper Street");
        addr.setTownCity("Bristol");
        addr.setPostcode("BS1 5AH");

        if (type == Address.addressType.POSTAL) {
            addr.setFirstName("Jason");
            addr.setLastName("Isaacs");
        }

        return addr;
    }

    private KeyPerson generateKeyPerson(KeyPerson.PersonType personType) {
        KeyPerson person = new KeyPerson();

        person.setFirstName("Jason");
        person.setLastName("Isaacs");
        person.setPosition("Owner");
        person.setDateOfBirth(new Date(-207404198000L));
        person.setPersonType(personType);
        person.setConvictionSearchResult(generateConvictionSearchResult());

        return person;
    }

    private ConvictionSearchResult generateConvictionSearchResult() {
        ConvictionSearchResult searchResult = new ConvictionSearchResult();

        searchResult.setMatchResult(ConvictionSearchResult.MatchResult.NO);
        searchResult.setSearchedAt(new Date());
        searchResult.setConfirmed("no");

        return searchResult;
    }

    private MetaData generateMetaData(MetaData.RegistrationStatus status) {
        MetaData data = new MetaData();

        data.setRoute(MetaData.RouteType.DIGITAL);
        data.setDateRegistered(new Date());
        data.setLastModified(data.getDateRegistered());

        if (status == MetaData.RegistrationStatus.ACTIVE) {
            data.setStatus(MetaData.RegistrationStatus.PENDING);
            data.setDateActivated(new Date());
        } else {
            data.setStatus(MetaData.RegistrationStatus.PENDING);
        }

        return data;
    }

    private FinanceDetails generateFinanceDetails() {
        FinanceDetails details = new FinanceDetails();

        ArrayList<Order> orders = new ArrayList<>();
        orders.add(generateOrder());
        details.setOrders(orders);

        ArrayList<Payment> payments = new ArrayList<>();

        switch(this.buildType) {
            case LOWER:
                details.setBalance(0);
                break;
            case UPPER:
                details.setBalance(15400);
                payments.add(generatePayment(details.getOrders().get(0).getOrderCode()));
                details.setPayments(payments);
                break;
            case IRRENEWAL:
                details.setBalance(10500);
                payments.add(generatePayment(details.getOrders().get(0).getOrderCode()));
                details.setPayments(payments);
                break;
        }

        return details;
    }

    private Order generateOrder() {
        Order order = new Order();

        order.setOrderCode(Long.toString(new Date().getTime()));
        order.setOrderCode(order.getOrderId());

        order.setPaymentMethod(Order.PaymentMethod.ONLINE);
        order.setMerchantId("EASERRSIMECOM");
        order.setCurrency("GBP");
        order.setDateCreated(new Date());
        order.setDateLastUpdated(order.getDateCreated());
        order.setUpdatedByUser("jason@example.com");

        ArrayList<OrderItem> orderItems = new ArrayList<>();

        switch(this.buildType) {
            case LOWER:
                order.setTotalAmount(0);
                order.setWorldPayStatus("IN_PROGRESS");
                break;
            case UPPER:
                order.setTotalAmount(15400);
                order.setWorldPayStatus("AUTHORISED");
                orderItems.add(generateOrderItem(OrderItem.OrderItemType.NEW));
                order.setOrderItems(orderItems);
                break;
            case IRRENEWAL:
                order.setTotalAmount(10500);
                order.setWorldPayStatus("AUTHORISED");
                orderItems.add(generateOrderItem(OrderItem.OrderItemType.IRRENEW));
                order.setOrderItems(orderItems);
                break;
        }

        return order;
    }

    private OrderItem generateOrderItem(OrderItem.OrderItemType itemType) {
        OrderItem orderItem = new OrderItem();

        orderItem.setCurrency("GBP");
        orderItem.setType(itemType);

        switch(itemType) {
            case NEW:
                orderItem.setAmount(15400);
                orderItem.setDescription("Initial Registration");
                orderItem.setReference("Initial Registration");
                break;
            case IRRENEW:
                orderItem.setAmount(10500);
                orderItem.setDescription("Renewal of Registration");
                orderItem.setReference("Renewal of Registration");
                break;
        }

        return orderItem;
    }

    private Payment generatePayment(String orderKey) {
        Payment payment = new Payment();

        payment.setOrderKey(orderKey);
        payment.setCurrency("GBP");
        payment.setMac_code("21412936c7dac4ddfa1d60499e20eda7");
        payment.setDateEntered(new Date());
        payment.setDateReceived(new Date());
        payment.setRegistrationReference("Worldpay");
        payment.setWorldPayPaymentStatus("AUTHORISED");
        payment.setUpdatedByUser("jason@example.com");
        payment.setComment("Paid via Worldpay");
        payment.setPaymentType(Payment.PaymentType.WORLDPAY);

        if (this.buildType == BuildType.UPPER) {
            payment.setAmount(15400);
        } else {
            payment.setAmount(10500);
        }

        return payment;
    }

    private Date generateExpiryDate() {
        return generateExpiryDate(new Date());
    }

    private Date generateExpiryDate(Date startDate) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(startDate);
        calendar.add(Calendar.YEAR, 3);

        return calendar.getTime();
    }

    private Date generateOriginalExpiryDate() {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(new Date());
        calendar.add(Calendar.YEAR, -2);
        calendar.add(Calendar.MONTH, -10);

        return calendar.getTime();
    }

    private String generateUUID() {
        String uuid = UUID.randomUUID().toString();

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(uuid.getBytes(StandardCharsets.UTF_8));
    }
}
