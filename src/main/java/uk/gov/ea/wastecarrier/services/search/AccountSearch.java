package uk.gov.ea.wastecarrier.services.search;

import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import uk.gov.ea.wastecarrier.services.core.Registration;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class AccountSearch {

    private SearchHelper searchHelper;
    private Logger log = Logger.getLogger(AccountSearch.class.getName());

    private String accountEmail;

    public AccountSearch(SearchHelper searchHelper, String accountEmail) {
        this.searchHelper = searchHelper;
        this.accountEmail = accountEmail;
    }

    public List<Registration> execute() {

        JacksonDBCollection<Registration, String> registrations = this.searchHelper.registrationsCollection();

        // Query to find registrations with matching accountEmail
        DBQuery.Query query = DBQuery.is("accountEmail", this.accountEmail);

        List<Registration> results = new LinkedList<>();

        try {
            results = searchHelper.toList(registrations.find(query));
        } catch (IllegalArgumentException e) {
            log.severe("Caught exception: " + e.getMessage() + " - Cannot find accountEmail " + this.accountEmail);
        }

        return results;
    }
}
