package uk.gov.ea.wastecarrier.services.support;

import uk.gov.ea.wastecarrier.services.core.mocks.WorldpayOrder;
import uk.gov.ea.wastecarrier.services.helper.mocks.WorldpayHelper;

import java.util.Date;

/**
 * A class which implements the builder pattern for generating test
 * entities. Java does not have default parameters but instead utilises
 * method overloading to achieve the same effect. To make calling code more
 * explicit we implement the builder pattern e.g.
 *
 * WorldpayOrder order1 = new WorldpayOrderBuilder()
 *                          .build();
 * WorldpayOrder order2 = new WorldpayOrderBuilder()
 *                          .orderCode("1234567")
 *                          .build();
 * WorldpayOrder order3 = new WorldpayOrderBuilder()
 *                          .orderCode("7654321")
 *                          .value(15400)
 *                          .build();
 */
public class WorldpayOrderBuilder {

    private String worldpayId;
    private String merchantCode = "EAWASTECARRIERS";
    private String orderCode;
    private String description = "Your Waste Carrier Registration CBDU999";
    private String currencyCode = "GBP";
    private Integer value = 15400;
    private String exponent = "2";
    private String orderContent = "Waste Carrier Registration registration: CBDU999 for Wittertainment Ltd";
    private String shopperEmailAddress = "jason.isaacs@example.com";

    private final WorldpayHelper helper = new WorldpayHelper("","", "");

    public WorldpayOrderBuilder worldpayId(String worldpayId) {
        this.worldpayId = worldpayId;
        return this;
    }

    public WorldpayOrderBuilder merchantCode(String merchantCode) {
        this.merchantCode = merchantCode;
        return this;
    }

    public WorldpayOrderBuilder orderCode(String orderCode) {
        this.orderCode = orderCode;
        return this;
    }

    public WorldpayOrderBuilder description(String description) {
        this.description = description;
        return this;
    }

    public WorldpayOrderBuilder currencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
        return this;
    }

    public WorldpayOrderBuilder value(Integer value) {
        this.value = value;
        return this;
    }

    public WorldpayOrderBuilder exponent(String exponent) {
        this.exponent = exponent;
        return this;
    }

    public WorldpayOrderBuilder orderContent(String orderContent) {
        this.orderContent = orderContent;
        return this;
    }

    public WorldpayOrderBuilder shopperEmailAddress(String shopperEmailAddress) {
        this.shopperEmailAddress = shopperEmailAddress;
        return this;
    }

    public WorldpayOrder build() {

        WorldpayOrder order = new WorldpayOrder();

        order.worldpayId = helper.generateWorldPayId();
        order.merchantCode = this.merchantCode;
        order.orderCode = generateOrderCode();
        order.description = this.description;
        order.currencyCode = this.currencyCode;
        order.value = this.value;
        order.exponent = this.exponent;
        order.orderContent = this.orderContent;
        order.shopperEmailAddress = this.shopperEmailAddress;

        return order;
    }

    private String generateOrderCode() {
        if (this.orderCode != null && !this.orderCode.isEmpty()) return this.orderCode;

        String timeInMilliseconds = String.valueOf(new Date().getTime());

        return timeInMilliseconds.substring(0, Math.min(timeInMilliseconds.length(), 10));
    }

}
