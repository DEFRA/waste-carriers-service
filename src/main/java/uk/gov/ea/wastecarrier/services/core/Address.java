package uk.gov.ea.wastecarrier.services.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    @JsonProperty
    private String uprn;
    @JsonProperty
    private addressType addressType;
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
    private String dependentLocality;
    @JsonProperty
    private String dependentThoroughfare;
    @JsonProperty
    private String administrativeArea;
    @JsonProperty
    private String localAuthorityUpdateDate;
    @JsonProperty
    private String royalMailUpdateDate;
    @JsonProperty
    private String easting;
    @JsonProperty
    private String northing;
    @JsonProperty
    private Location location;
    @JsonProperty
    private String firstName;
    @JsonProperty
    private String lastName;

    public Address() {

    }

    public Address(
            String uprn,
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
            String firstName,
            String lastName,
            String easting,
            String northing,
            Location location) {
        this.uprn = uprn;
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
        this.easting = easting;
        this.northing = northing;
        this.location = location;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getUprn() {
        return uprn;
    }

    public void setUprn(String uprn) {
        this.uprn = uprn;
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

    /**
     * Helper that returns the input string up to the first occurrence of the
     * specified character, or the full string if the character does not occur.
     * @param s The string to scan.
     * @param c The character to scan for.
     * @return A string as described above.
     */
    private String getStringUpToChar(String s, Character c)
    {
        String result = s;
        if (result != null)
        {
            int endIndex = result.indexOf(c);
            if (endIndex != -1)
            {
                result = result.substring(0, endIndex);
            }
        }
        return result;
    }
    
    public String getEasting() {
        return easting;
    }
    
    @JsonIgnore
    public String getFirstOrOnlyEasting() {
        return getStringUpToChar(easting, '|');
    }

    public void setEasting(String easting) {
        this.easting = easting;
    }
    
    public String getNorthing() {
        return northing;
    }
    
    @JsonIgnore
    public String getFirstOrOnlyNorthing() {
        return getStringUpToChar(northing, '|');
    }

    public void setNorthing(String northing) {
        this.northing = northing;
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
