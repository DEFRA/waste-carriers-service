package uk.gov.ea.wastecarrier.services.support;

import uk.gov.ea.wastecarrier.services.core.Entity;

import java.util.Date;

/**
 * A class which implements the builder pattern for generating test
 * entities. Java does not have default parameters but instead utilises
 * method overloading to achieve the same effect. To make calling code more
 * explicit we implement the builder pattern e.g.
 *
 * Entity doc1 = new EntityBuilder(EntityBuilder.BuildType.COMPANY)
 *                          .build();
 * Entity doc2 = new EntityBuilder(EntityBuilder.BuildType.COMPANY)
 *                          .name("Isaacs Waste Services Ltd")
 *                          .build();
 * Entity doc3 = new EntityBuilder(EntityBuilder.BuildType.COMPANY)
 *                          .name("Isaacs Waste Services Ltd")
 *                          .systemFlag("FTW")
 *                          .build();
 */
public class EntityBuilder {

    private BuildType buildType;

    private String name;
    private Date dateOfBirth;
    private String companyNumber;
    private String systemFlag = "FTW";
    private String incidentNumber;

    public enum BuildType {
        COMPANY,
        PERSON
    }

    public EntityBuilder(BuildType buildType) {
        this.buildType = buildType;
    }

    public EntityBuilder name(String name) {
        this.name = name;
        return this;
    }

    public EntityBuilder dateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public EntityBuilder companyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
        return this;
    }

    public EntityBuilder systemFlag(String systemFlag) {
        this.systemFlag = systemFlag;
        return this;
    }

    public EntityBuilder incidentNumber(String incidentNumber) {
        this.incidentNumber = incidentNumber;
        return this;
    }

    public Entity build() {
        Entity doc = new Entity();

        doc.systemFlag = this.systemFlag;
        doc.incidentNumber = generateIncidentNumber();

        if (this.buildType == BuildType.COMPANY) {
            doc.companyNumber = this.companyNumber;
            doc.name = this.name == null ? "Isaacs Waste Services Ltd" : this.name;
        } else {
            doc.dateOfBirth = this.dateOfBirth == null ? new Date(257952324000L) : this.dateOfBirth;
            doc.name = this.name == null ? "Isaacs, Jason" : this.name;
        }

        return doc;
    }

    private String generateIncidentNumber() {
        if (this.incidentNumber != null) return this.incidentNumber;

        return Long.toHexString(Double.doubleToLongBits(Math.random()));
    }
}
