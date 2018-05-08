package uk.gov.ea.wastecarrier.services;

import uk.gov.ea.wastecarrier.services.core.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

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

    private String _regIdentifier = "CBDL1";
    private String _accountEmail = "jason@example.com";

    public RegistrationBuilder() {}

    public RegistrationBuilder regIdentifier(String _regIdentifier)
    {
        this._regIdentifier = _regIdentifier;
        return this;
    }

    public RegistrationBuilder accountEmail(String _accountEmail)
    {
        this._accountEmail = _accountEmail;
        return this;
    }

    public Registration buildLowerTier() {
        Registration reg = new Registration();

        reg.setUuid(generateUUID());
        reg.setRegIdentifier(this._regIdentifier);
        reg.setTier(Registration.RegistrationTier.LOWER);
        reg.setBusinessType("soleTrader");
        reg.setOtherBusinesses("no");
        reg.setConstructionWaste("no");
        reg.setCompanyName("WCR Service test LT");
        reg.setFirstName("Jason");
        reg.setLastName("Isaac");
        reg.setPhoneNumber("01179345400");
        reg.setContactEmail("jason@example.com");
        reg.setAccountEmail(this._accountEmail);
        reg.setDeclaration("1");

        ArrayList<Address> addresses = new ArrayList<Address>();
        addresses.add(generateAddress(Address.addressType.REGISTERED));
        addresses.add(generateAddress(Address.addressType.POSTAL));
        reg.setAddresses(addresses);

        reg.setMetaData(generateMetaData(MetaData.RegistrationStatus.PENDING));

        reg.setFinanceDetails(generateFinanceDetails());

        return reg;
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
            addr.setLastName("Isaac");
        }

        return addr;
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
        Order order = new Order();
        order.setOrderCode(Long.toString(new Date().getTime()));
        order.setOrderCode(order.getOrderId());
        order.setPaymentMethod(Order.PaymentMethod.ONLINE);
        order.setMerchantId("EASERRSIMECOM");
        order.setTotalAmount(0);
        order.setCurrency("GBP");
        order.setDateCreated(new Date());
        order.setWorldPayStatus("IN_PROGRESS");
        order.setDateLastUpdated(order.getDateCreated());
        order.setUpdatedByUser("agent@defra.gsi.gov.uk");

        ArrayList<Order> orders = new ArrayList<Order>();
        orders.add(order);
        details.setOrders(orders);

        details.setBalance(0);

        return details;
    }

    private String generateUUID() {
        String uuid = UUID.randomUUID().toString();

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(uuid.getBytes(StandardCharsets.UTF_8));
    }
}
