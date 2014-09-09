package uk.gov.ea.wastecarrier.services.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;

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
    private String convictionSearchResult;

    @JsonProperty("conviction_system_flag")
    private String convictionSystemFlag;

    @JsonProperty("last_conviction_search")
    private Date lastConvictionSearch;

    public KeyPerson() {

    }

    public KeyPerson(
            String firstName,
            String lastName,
            Date dateOfBirth,
            String position,
            PersonType personType,
            String convictionSearchResult,
            String convictionSystemFlag,
            Date lastConvictionSearch) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.position = position;
        this.personType = personType;
        this.convictionSearchResult = convictionSearchResult;
        this.convictionSystemFlag = convictionSystemFlag;
        this.lastConvictionSearch = lastConvictionSearch;
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

    public String getConvictionSearchResult() {
        return convictionSearchResult;
    }

    public void setConvictionSearchResult(String convictionSearchResult) {
        this.convictionSearchResult = convictionSearchResult;
    }

    public String getConvictionSystemFlag() {
        return convictionSystemFlag;
    }

    public void setConvictionSystemFlag(String convictionSystemFlag) {
        this.convictionSystemFlag = convictionSystemFlag;
    }

    public Date getLastConvictionSearch() {
        return lastConvictionSearch;
    }

    public void setLastConvictionSearch(Date lastConvictionSearch) {
        this.lastConvictionSearch = lastConvictionSearch;
    }
}
