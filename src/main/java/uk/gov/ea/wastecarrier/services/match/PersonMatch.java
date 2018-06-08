package uk.gov.ea.wastecarrier.services.match;

import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import uk.gov.ea.wastecarrier.services.core.Entity;
import uk.gov.ea.wastecarrier.services.helper.SearchHelper;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class PersonMatch {

    private SearchHelper helper;
    private Logger log = Logger.getLogger(PersonMatch.class.getName());

    private String firstName;
    private String lastName;
    private Date dateOfBirth;

    public PersonMatch(SearchHelper helper, String firstName, String lastName, Date dateOfBirth) {
        this.helper = helper;
        this.firstName = parseName(firstName);
        this.lastName = parseName(lastName);
        this.dateOfBirth = dateOfBirth;
    }

    public Entity execute() {

        if ((this.firstName + this.lastName).isEmpty()) return null;

        JacksonDBCollection<Entity, String> collection = this.helper.getCollection();

        Entity document = null;

        try {
            // If we have a date of birth run a search including that first
            if (this.dateOfBirth != null) document = nameAndDateOfBirthMatch(collection);

            // If no match try again this time without the date of birth
            if (document == null) document = nameMatch(collection);
        } catch (IllegalArgumentException e) {
            log.severe(String.format(
                    "Error matching person %s %s %s: %s",
                    this.firstName,
                    this.lastName,
                    this.dateOfBirth.toString(),
                    e.getMessage()
            ));
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        return document;
    }

    private Entity nameAndDateOfBirthMatch(JacksonDBCollection<Entity, String> collection) {

        DBQuery.Query query = DBQuery.and(DBQuery
                .is("dateOfBirth", this.dateOfBirth));

        // If you place these in the and() at the same time they seems to work as an OR
        // hence we add them separately
        query.and(DBQuery.regex("name", generateNameLikePattern(this.firstName)));
        query.and(DBQuery.regex("name", generateNameLikePattern(this.lastName)));

        return collection.findOne(query);
    }

    private Entity nameMatch(JacksonDBCollection<Entity, String> collection) {

        // If you place these in the and() at the same time they seems to work as an OR
        // hence we add them separately
        DBQuery.Query query = DBQuery.and(DBQuery
                .regex("name", generateNameLikePattern(this.firstName)));
        query.and(DBQuery.regex("name", generateNameLikePattern(this.lastName)));

        return collection.findOne(query);
    }

    private Pattern generateNameLikePattern(String name) {
        return Pattern.compile(
                String.format(".*%s.*", name),
                Pattern.CASE_INSENSITIVE);
    }

    private String parseName(String name) {

        if (name == null || name.trim().isEmpty()) return "";

        return name.trim();
    }
}
