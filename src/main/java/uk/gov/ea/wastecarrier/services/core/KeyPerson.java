package uk.gov.ea.wastecarrier.services.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * Simple POJO for containing details on key persons
 * e.g. directors and partners.
 */
public class KeyPerson {

    public enum PersonType {
        KEY, RELEVANT
    }

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("dob")
    private Date dateOfBirth;

    @JsonProperty()
    private String position;

    @JsonProperty("person_type")
    private PersonType personType;

    @JsonProperty("conviction_search_result")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private ConvictionSearchResult convictionSearchResult;

    public KeyPerson() {

    }

    public KeyPerson(
            String firstName,
            String lastName,
            Date dateOfBirth,
            String position,
            PersonType personType,
            ConvictionSearchResult convictionSearchResult) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.position = position;
        this.personType = personType;
        this.convictionSearchResult = convictionSearchResult;
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

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public PersonType getPersonType() {
        return personType;
    }

    public void setPersonType(PersonType personType) {
        this.personType = personType;
    }

    public ConvictionSearchResult getConvictionSearchResult() {
        return convictionSearchResult;
    }

    public void setConvictionSearchResult(ConvictionSearchResult convictionSearchResult) {
        this.convictionSearchResult = convictionSearchResult;
    }
}
