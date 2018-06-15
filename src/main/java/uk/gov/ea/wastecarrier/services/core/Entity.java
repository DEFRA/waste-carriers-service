package uk.gov.ea.wastecarrier.services.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.mongojack.Id;
import org.mongojack.ObjectId;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Entity {

    @JsonProperty
    @Id
    @ObjectId
    public String id;

    // The full name, either of an organisation or an individual.
    @JsonProperty
    public String name;

    // The date of birth (if known and available)
    @JsonProperty
    public Date dateOfBirth;

    // The company number (if known and applicable)
    @JsonProperty
    public String companyNumber;

    // The system the record is recorded in
    @JsonProperty
    public String systemFlag;

    // The incident number or reference
    @JsonProperty
    public String incidentNumber;
}
