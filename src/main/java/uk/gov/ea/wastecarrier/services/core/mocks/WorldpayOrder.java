package uk.gov.ea.wastecarrier.services.core.mocks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.mongojack.Id;
import org.mongojack.ObjectId;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorldpayOrder {

    @JsonProperty
    @Id
    @ObjectId
    public String id;

    @JsonProperty
    public String worldpayId;

    @JsonProperty
    public String merchantCode;

    @JsonProperty
    public String orderCode;

    @JsonProperty
    public String description;

    @JsonProperty
    public String currencyCode;

    @JsonProperty
    public Integer value;

    @JsonProperty
    public String exponent;

    @JsonProperty
    public String orderContent;

    @JsonProperty
    public String shopperEmailAddress;
}
