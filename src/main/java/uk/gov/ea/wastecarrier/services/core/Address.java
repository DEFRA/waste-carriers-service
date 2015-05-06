package uk.gov.ea.wastecarrier.services.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Address represents any one of the addresses enterable into the system
 * The two 'types' of address available when this class was created were 'registered' and 'postal'
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Address {

    public enum addressType {
        REGISTERED,
        POSTAL
    }

    @JsonProperty("address_type")
    private addressType addressType;

    @JsonProperty("address_mode")
    private String addressMode;

    @JsonProperty("house_number")
    private String houseNumber;

    @JsonProperty("address_line_1")
    private String addressLine1;

    @JsonProperty("address_line_2")
    private String addressLine2;

    @JsonProperty("address_line_3")
    private String addressLine3;

    @JsonProperty("address_line_4")
    private String addressLine4;

    @JsonProperty("town_city")
    private String townCity;

    @JsonProperty("post_code")
    private String postcode;

    @JsonProperty("country")
    private String country;

    @JsonProperty("dependent_locality")
    private String dependentLocality;

    @JsonProperty("dependent_thoroughfare")
    private String dependentThoroughfare;

    @JsonProperty("administrative_area")
    private String administrativeArea;

    @JsonProperty("local_authority_update_date")
    private String localAuthorityUpdateDate;

    @JsonProperty("royal_mail_update_date")
    private String royalMailUpdateDate;

    @JsonProperty("location")
    private Location location;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    public Address() {

    }

    public Address(
            addressType addressType,
            String addressMode,
            String houseNumber,
            String addressLine1,
            String addressLine2,
            String addressLine3,
            String addressLine4,
            String townCity,
            String postCode,
            String country,
            String dependentLocality,
            String dependentThoroughfare,
            String administrativeArea,
            String localAuthorityUpdateDate,
            String royalMailUpdateDate,
            Location location,
            String firstName,
            String lastName) {
        this.addressType = addressType;
        this.addressMode = addressMode;
        this.houseNumber = houseNumber;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.addressLine3 = addressLine3;
        this.addressLine4 = addressLine4;
        this.townCity = townCity;
        this.postcode = postCode;
        this.country = country;
        this.dependentLocality = dependentLocality;
        this.dependentThoroughfare = dependentThoroughfare;
        this.administrativeArea = administrativeArea;
        this.localAuthorityUpdateDate = localAuthorityUpdateDate;
        this.royalMailUpdateDate = royalMailUpdateDate;
        this.location = location;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Address.addressType getAddressType() {
        return addressType;
    }

    public void setAddressType(Address.addressType addressType) {
        this.addressType = addressType;
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

    public String getDependentLocality() {
        return dependentLocality;
    }

    public void setDependentLocality(String dependentLocality) {
        this.dependentLocality = dependentLocality;
    }

    public String getDependentThoroughfare() {
        return dependentThoroughfare;
    }

    public void setDependentThoroughfare(String dependentThoroughfare) {
        this.dependentThoroughfare = dependentThoroughfare;
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

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
