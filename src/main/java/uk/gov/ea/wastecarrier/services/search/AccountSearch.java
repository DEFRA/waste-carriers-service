package uk.gov.ea.wastecarrier.services.search;

import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.helper.SearchHelper;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class AccountSearch {

    private SearchHelper helper;
    private Logger log = Logger.getLogger(AccountSearch.class.getName());

    private String accountEmail;

    /**
     * Search for registrations in the database which have a matching accountEmail
     *
     * This search directly supports the `Registration.find_by_email` method in
     * the front end project.
     *
     * @param helper
     * @param accountEmail email to search for
     */
    public AccountSearch(SearchHelper helper, String accountEmail) {
        this.helper = helper;
        this.accountEmail = accountEmail;
    }

    public List<Registration> execute() {

        JacksonDBCollection<Registration, String> registrations = this.helper.getCollection();

        // Query to find registrations with matching accountEmail
        DBQuery.Query query = DBQuery.is("accountEmail", this.accountEmail);

        List<Registration> results = new LinkedList<>();

        try {
            results = helper.toList(registrations.find(query));
        } catch (IllegalArgumentException e) {
            log.severe("Caught exception: " + e.getMessage() + " - Cannot find accountEmail " + this.accountEmail);
        }

        return results;
    }
}
