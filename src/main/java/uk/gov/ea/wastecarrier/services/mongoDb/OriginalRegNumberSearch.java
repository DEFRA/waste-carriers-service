package uk.gov.ea.wastecarrier.services.mongoDb;

import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import uk.gov.ea.wastecarrier.services.core.Registration;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class OriginalRegNumberSearch {

    private SearchHelper searchHelper;
    private Logger log = Logger.getLogger(OriginalRegNumberSearch.class.getName());

    private String originalRegNumber;

    public OriginalRegNumberSearch(SearchHelper searchHelper, String originalRegNumber) {
        this.searchHelper = searchHelper;
        this.originalRegNumber = originalRegNumber;
    }

    public List<Registration> execute() {

        JacksonDBCollection<Registration, String> registrations = this.searchHelper.getRegistrations();

        // Query to find registrations with matching accountEmail
        DBQuery.Query query = DBQuery.is("originalRegistrationNumber", this.originalRegNumber);

        List<Registration> results = new LinkedList<>();

        try {
            results = searchHelper.toList(registrations.find(query));
        } catch (IllegalArgumentException e) {
            log.severe("Caught exception: " + e.getMessage() + " - Cannot find originalRegistrationNumber " + this.originalRegNumber);
        }

        return results;
    }
}
