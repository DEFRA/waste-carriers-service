package uk.gov.ea.wastecarrier.services.search;

import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.helper.SearchHelper;

import java.util.logging.Logger;

public class OriginalRegNumberSearch {

    private SearchHelper searchHelper;
    private Logger log = Logger.getLogger(OriginalRegNumberSearch.class.getName());

    private String originalRegNumber;

    /**
     * Search for registrations in the database which have a matching accountEmail
     *
     * This search directly supports the `Registration.find_by_original_registration_no` method in
     * the front end project.
     *
     * @param searchHelper
     * @param originalRegNumber original IR number to search for
     */
    public OriginalRegNumberSearch(SearchHelper searchHelper, String originalRegNumber) {
        this.searchHelper = searchHelper;
        this.originalRegNumber = originalRegNumber;
    }

    public Registration execute() {

        JacksonDBCollection<Registration, String> registrations = this.searchHelper.getCollection();

        // Query to find registrations with matching accountEmail
        DBQuery.Query query = DBQuery.is("originalRegistrationNumber", this.originalRegNumber);

        Registration result = null;

        try {
            result = registrations.findOne(query);
        } catch (IllegalArgumentException e) {
            log.severe("Caught exception: " + e.getMessage() + " - Cannot find originalRegistrationNumber " + this.originalRegNumber);
        }

        return result;
    }
}
