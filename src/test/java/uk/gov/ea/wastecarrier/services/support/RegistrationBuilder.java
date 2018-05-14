package uk.gov.ea.wastecarrier.services.support;

import uk.gov.ea.wastecarrier.services.core.*;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * A class which implements the builder pattern for generating test
 * registrations. Java does not have default parameters but instead utilises
 * method overloading to achieve the same effect. To make calling code more
 * explicit we implement the builder pattern e.g.

 * Registration reg1 = new RegistrationBuilder(RegistrationBuilder.BuildType.LOWER)
 *                          .build();
 * Registration reg2 = new RegistrationBuilder(RegistrationBuilder.BuildType.LOWER)
 *                          .regIdentifier("CBDL1")
 *                          .build();
 * Registration reg3 = new RegistrationBuilder(RegistrationBuilder.BuildType.LOWER)
 *                          .regIdentifier("CBDL1")
 *                          .accountEmail("joan@example.com")
 *                          .build();
 */
public class RegistrationBuilder {

    private BuildType buildType;
    private Boolean includeCopyCard = false;

    private String regIdentifier;
    private String businessType = "soleTrader";
    private String companyName;
    private String lastName = "Isaacs";
    private String registeredPostcode = "BS1 5AH";
    private String postalPostcode = "WA4 1HT";
    private Date dateRegistered = new Date();
    private MetaData.RegistrationStatus registrationStatus = MetaData.RegistrationStatus.ACTIVE;
    private MetaData.RouteType registrationRoute = MetaData.RouteType.DIGITAL;
    private String accountEmail = "jason@example.com";
    private String originalRegNumber = "CB/VM8888WW/A001";
    private String declaredConvictions = "no";
    private ConvictionSearchResult.MatchResult companyConvictionMatch = ConvictionSearchResult.MatchResult.NO;
    private ConvictionSearchResult.MatchResult keyPersonConvictionMatch = ConvictionSearchResult.MatchResult.NO;
    private Date orderCreatedDate = new Date();
    private Date orderUpdatedDate = this.orderCreatedDate;
    private Date paymentReceived = new Date();
    private Integer paymentAmount = 0;
    private Payment.PaymentType paymentType = Payment.PaymentType.WORLDPAY;

    public enum BuildType {
        LOWER,
        UPPER,
        IRRENEWAL
    }

    public RegistrationBuilder(BuildType buildType) {
        this.buildType = buildType;
    }

    public RegistrationBuilder(BuildType buildType, Boolean includeCopyCard) {
        this.buildType = buildType;
        this.includeCopyCard = includeCopyCard;
    }

    public RegistrationBuilder regIdentifier(String regIdentifier) {
        this.regIdentifier = regIdentifier;
        return this;
    }

    public RegistrationBuilder businessType(String businessType) {
        this.businessType = businessType;
        return this;
    }

    public RegistrationBuilder companyName(String companyName) {
        this.companyName = companyName;
        return this;
    }

    public RegistrationBuilder lastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public RegistrationBuilder registeredPostcode(String postcode) {
        this.registeredPostcode = postcode;
        return this;
    }

    public RegistrationBuilder postalPostcode(String postcode) {
        this.postalPostcode = postcode;
        return this;
    }

    public RegistrationBuilder dateRegistered(Date dateRegistered) {
        this.dateRegistered = dateRegistered;
        return this;
    }

    public RegistrationBuilder registrationStatus(MetaData.RegistrationStatus registrationStatus) {
        this.registrationStatus = registrationStatus;
        return this;
    }

    public RegistrationBuilder registrationRoute(MetaData.RouteType registrationRoute) {
        this.registrationRoute = registrationRoute;
        return this;
    }

    public RegistrationBuilder accountEmail(String accountEmail) {
        this.accountEmail = accountEmail;
        return this;
    }

    public RegistrationBuilder originalRegNumber(String originalRegNumber) {
        this.originalRegNumber = originalRegNumber;
        return this;
    }

    public RegistrationBuilder declaredConvictions(String declaredConvictions) {
        this.declaredConvictions = declaredConvictions;
        return this;
    }

    public RegistrationBuilder companyConvictionMatch(ConvictionSearchResult.MatchResult companyConvictionMatch) {
        this.companyConvictionMatch = companyConvictionMatch;
        return this;
    }

    public RegistrationBuilder keyPersonConvictionMatch(ConvictionSearchResult.MatchResult keyPersonConvictionMatch) {
        this.keyPersonConvictionMatch = keyPersonConvictionMatch;
        return this;
    }

    public RegistrationBuilder orderCreatedDate(Date orderCreatedDate) {
        this.orderCreatedDate = orderCreatedDate;
        return this;
    }

    public RegistrationBuilder orderUpdatedDate(Date orderUpdatedDate) {
        this.orderUpdatedDate = orderUpdatedDate;
        return this;
    }

    public RegistrationBuilder paymentReceived(Date paymentReceived) {
        this.paymentReceived = paymentReceived;
        return this;
    }

    public RegistrationBuilder paymentAmount(Integer paymentAmount) {
        this.paymentAmount = paymentAmount;
        return this;
    }

    public RegistrationBuilder paymentType(Payment.PaymentType paymentType) {
        this.paymentType = paymentType;
        return this;
    }

    public Registration build() {
        Registration reg = new Registration();

        reg.setUuid(generateUUID());
        reg.setBusinessType(this.businessType);
        reg.setCompanyName(generateCompanyName());
        reg.setFirstName("Jason");
        reg.setLastName(this.lastName);
        reg.setPhoneNumber("01179345400");
        reg.setContactEmail("jason@example.com");
        reg.setAccountEmail(this.accountEmail);
        reg.setDeclaration("1");

        ArrayList<Address> addresses = new ArrayList<>();
        addresses.add(generateAddress(Address.addressType.REGISTERED));
        addresses.add(generateAddress(Address.addressType.POSTAL));
        reg.setAddresses(addresses);

        reg.setMetaData(generateMetaData());
        reg.setFinanceDetails(generateFinanceDetails());

        switch(this.buildType) {
            case LOWER:
                buildLowerTier(reg);
                break;
            case UPPER:
                buildUpperTier(reg);
                break;
            case IRRENEWAL:
                buildIRRenewal(reg);
                break;
        }
        return reg;
    }

    private void buildLowerTier(Registration reg) {
        reg.setRegIdentifier(generateRegIdentifier(Registration.RegistrationTier.LOWER));
        reg.setTier(Registration.RegistrationTier.LOWER);

        reg.setOtherBusinesses("no");
        reg.setConstructionWaste("no");
    }

    private Registration buildUpperTier(Registration reg) {
        reg.setRegIdentifier(generateRegIdentifier(Registration.RegistrationTier.UPPER));
        reg.setTier(Registration.RegistrationTier.UPPER);

        reg.setOtherBusinesses("no");
        reg.setConstructionWaste("yes");
        reg.setRegistrationType("carrier_broker_dealer");

        reg.setDeclaredConvictions(this.declaredConvictions);
        reg.setExpires_on(generateExpiryDate());

        ArrayList<KeyPerson> persons = new ArrayList<>();
        persons.add(generateKeyPerson(KeyPerson.PersonType.KEY));
        reg.setKeyPeople(persons);

        reg.setConvictionSearchResult(generateConvictionSearchResult(this.companyConvictionMatch));

        return reg;
    }

    private void buildIRRenewal(Registration reg) {
        buildUpperTier(reg);

        reg.setOriginalDateExpiry(generateOriginalExpiryDate());
        reg.setOriginalRegistrationNumber(this.originalRegNumber);
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

    private String generateCompanyName() {
        if (this.companyName != null) return this.companyName;

        String companyName = "";
        switch(this.businessType) {
            case "soleTrader":
                companyName = "Isaacs & Sons";
                break;
            case "partnership":
                companyName = "Isaacs, Kermode & Mayo";
                break;
            case "limitedCompany":
                companyName = "Isaacs Waste Services Ltd";
                break;
            case "publicBody":
                companyName = "Department of Wittertainment";
                break;
            case "authority":
                companyName = "Witter City Council";
                break;
            case "other":
                companyName = "Morrissey Waste consultants";
                break;
        }

        return companyName;
    }

    private Address generateAddress(Address.addressType type) {
        Address addr = new Address();

        addr.setAddressType(type);
        addr.setHouseNumber("123");
        addr.setAddressLine1("Upper Street");
        addr.setTownCity("Bristol");

        if (type == Address.addressType.POSTAL) {
            addr.setPostcode(this.postalPostcode);
            addr.setFirstName("Jason");
            addr.setLastName("Isaacs");
        } else {
            addr.setPostcode(this.registeredPostcode);
        }

        return addr;
    }

    private KeyPerson generateKeyPerson(KeyPerson.PersonType personType) {
        KeyPerson person = new KeyPerson();

        person.setFirstName("Jason");
        person.setLastName("Isaacs");

        switch(this.businessType) {
            case "soleTrader":
                person.setPosition("Owner");
                break;
            case "partnership":
                person.setPosition("Partner");
                break;
            case "limitedCompany":
                person.setPosition("Director");
                break;
            case "publicBody":
                person.setPosition("Director");
                break;
            case "authority":
                person.setPosition("Manager");
                break;
            case "other":
                person.setPosition("Owner");
                break;
        }

        person.setDateOfBirth(new Date(-207404198000L));
        person.setPersonType(personType);
        person.setConvictionSearchResult(generateConvictionSearchResult(this.keyPersonConvictionMatch));

        return person;
    }

    private ConvictionSearchResult generateConvictionSearchResult(ConvictionSearchResult.MatchResult matchResult) {
        ConvictionSearchResult searchResult = new ConvictionSearchResult();

        searchResult.setMatchResult(matchResult);
        searchResult.setSearchedAt(new Date());
        searchResult.setConfirmed("no");

        return searchResult;
    }

    private MetaData generateMetaData() {
        MetaData data = new MetaData();

        data.setRoute(this.registrationRoute);
        data.setDateRegistered(this.dateRegistered);
        data.setLastModified(data.getDateRegistered());

        data.setStatus(this.registrationStatus);

        if (this.registrationStatus != MetaData.RegistrationStatus.PENDING) data.setDateActivated(new Date());

        return data;
    }

    private FinanceDetails generateFinanceDetails() {
        FinanceDetails details = new FinanceDetails();

        ArrayList<Order> orders = new ArrayList<>();
        orders.add(generateOrder());
        details.setOrders(orders);

        ArrayList<Payment> payments = new ArrayList<>();

        switch(this.buildType) {
            case UPPER:
                payments.add(generatePayment(details.getOrders().get(0).getOrderCode()));
                details.setPayments(payments);
                break;
            case IRRENEWAL:
                payments.add(generatePayment(details.getOrders().get(0).getOrderCode()));
                details.setPayments(payments);
                break;
        }

        // Doesn't actually matter what we set the balance to. When we save the
        // registration code in the Registration class will determine what the
        // balance is based on the value of the orders and payments it contains
        details.setBalance(0);

        return details;
    }

    private Order generateOrder() {
        Order order = new Order();

        order.setOrderCode(Long.toString(new Date().getTime()));
        order.setOrderCode(order.getOrderId());

        order.setPaymentMethod(Order.PaymentMethod.ONLINE);
        order.setMerchantId("EASERRSIMECOM");
        order.setCurrency("GBP");
        order.setDateCreated(this.orderCreatedDate);
        order.setDateLastUpdated(this.orderUpdatedDate);
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
                orderItems.add(generateOrderItem(OrderItem.OrderItemType.RENEW));
                order.setOrderItems(orderItems);
                break;
        }

        if (this.includeCopyCard) {
            order.setTotalAmount(order.getTotalAmount() + 500);
            orderItems.add(generateOrderItem(OrderItem.OrderItemType.COPY_CARDS));
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
            case COPY_CARDS:
                orderItem.setAmount(500);
                orderItem.setDescription("Copy card");
                orderItem.setReference("Copy card");
                break;
            case RENEW:
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
        payment.setDateReceived(this.paymentReceived);
        payment.setRegistrationReference("Worldpay");
        payment.setWorldPayPaymentStatus("AUTHORISED");
        payment.setUpdatedByUser("jason@example.com");
        payment.setComment("Paid via Worldpay");
        payment.setPaymentType(this.paymentType);

        if (this.paymentAmount != 0) {
            payment.setAmount(this.paymentAmount);
        } else {
            switch (this.buildType) {
                case UPPER:
                    payment.setAmount(15400);
                    break;
                case IRRENEWAL:
                    payment.setAmount(10500);
                    break;
            }
            if (this.includeCopyCard) {
                payment.setAmount(payment.getAmount() + 500);
            }
        }

        return payment;
    }

    private Date generateExpiryDate() {
        return generateExpiryDate(new Date());
    }

    private Date generateExpiryDate(Date startDate) {
        return TestUtil.fromDate(startDate, 3, 0, 0);
    }

    private Date generateOriginalExpiryDate() {
        return TestUtil.fromCurrentDate(-2, -10, 0);
    }

    private String generateUUID() {
        String uuid = UUID.randomUUID().toString();

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(uuid.getBytes(StandardCharsets.UTF_8));
    }
}
