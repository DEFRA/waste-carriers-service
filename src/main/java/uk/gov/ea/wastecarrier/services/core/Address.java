package uk.gov.ea.wastecarrier.services.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Address represents any one of the addresses enterable into the system
 * The two 'types' of address available when this class was created were 'registered' and 'postal'
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Address {

    public enum type {
        REGISTERED,
        POSTAL
    }

    @JsonProperty
    private type type;

    @JsonProperty
    private String addressMode;

    @JsonProperty
    private String houseNumber;

    @JsonProperty
    private String addressLine1;

    @JsonProperty
    private String addressLine2;

    @JsonProperty
    private String addressLine3;

    @JsonProperty
    private String addressLine4;

    @JsonProperty
    private String townCity;

    @JsonProperty
    private String postcode;
    @JsonProperty
    private String country;

    @JsonProperty
    private String easting;

    @JsonProperty
    private String northing;

    @JsonProperty
    private String dependentLocality;

    @JsonProperty
    private String dependentThroughfare;

    @JsonProperty
    private String administrativeArea;

    @JsonProperty
    private String localAuthorityUpdateDate;

    @JsonProperty
    private String royalMailUpdateDate;

    public Address() {

    }

    public Address(
            type type,
            String addressMode,
            String houseNumber,
            String addressLine1,
            String addressLine2,
            String addressLine3,
            String addressLine4,
            String townCity,) {

    }

    public Address.type getType() {
        return type;
    }

    public void setType(Address.type type) {
        this.type = type;
    }

    public String getAddressMode() {
        return addressMode;
    }

    public void setAddressMode(String addressMode) {
        this.addressMode = addressMode;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getAddressLine3() {
        return addressLine3;
    }

    public void setAddressLine3(String addressLine3) {
        this.addressLine3 = addressLine3;
    }

    public String getAddressLine4() {
        return addressLine4;
    }

    public void setAddressLine4(String addressLine4) {
        this.addressLine4 = addressLine4;
    }

    public String getTownCity() {
        return townCity;
    }

    public void setTownCity(String townCity) {
        this.townCity = townCity;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getEasting() {
        return easting;
    }

    public void setEasting(String easting) {
        this.easting = easting;
    }

    public String getNorthing() {
        return northing;
    }

    public void setNorthing(String northing) {
        this.northing = northing;
    }

    public String getDependentLocality() {
        return dependentLocality;
    }

    public void setDependentLocality(String dependentLocality) {
        this.dependentLocality = dependentLocality;
    }

    public String getDependentThroughfare() {
        return dependentThroughfare;
    }

    public void setDependentThroughfare(String dependentThroughfare) {
        this.dependentThroughfare = dependentThroughfare;
    }

    public String getAdministrativeArea() {
        return administrativeArea;
    }

    public void setAdministrativeArea(String administrativeArea) {
        this.administrativeArea = administrativeArea;
    }

    public String getLocalAuthorityUpdateDate() {
        return localAuthorityUpdateDate;
    }

    public void setLocalAuthorityUpdateDate(String localAuthorityUpdateDate) {
        this.localAuthorityUpdateDate = localAuthorityUpdateDate;
    }

    public String getRoyalMailUpdateDate() {
        return royalMailUpdateDate;
    }

    public void setRoyalMailUpdateDate(String royalMailUpdateDate) {
        this.royalMailUpdateDate = royalMailUpdateDate;
    }
}
