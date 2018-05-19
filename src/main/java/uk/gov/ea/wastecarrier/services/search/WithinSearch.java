package uk.gov.ea.wastecarrier.services.search;

import org.mongojack.DBQuery;
import org.mongojack.DBSort;
import org.mongojack.JacksonDBCollection;
import uk.gov.ea.wastecarrier.services.core.Registration;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class WithinSearch {

    private SearchHelper searchHelper;
    private Logger log = Logger.getLogger(WithinSearch.class.getName());

    private String searchValue;
    private SearchWithin searchWithin;
    private Pattern likePattern;
    private Integer resultCount;


    public enum SearchWithin {
        any,
        companyName,
        contactName,
        postcode
    }

    /**
     * Search for registrations in the database by looking within specific fields, namely companyName, contactName and
     * the postcode field in the addresses.
     *
     * This search directly supports the search run from the home screen of the back office.
     *
     * @param searchHelper
     * @param searchValue the value the user entered in the search box of the home screen
     * @param searchWithin if the user selected 'Refine your search' they can choose to just search within a specific
     *                     field by selecting from a dropdown. The values that translates to are 'any', 'companyName',
     *                     'contactName', and 'postcode'
     * @param resultCount the user doesn't get to specify this value, nor is it passed in the query like other searches.
     *                    However we do limit the results generated to just 100 as the back office does not support
     *                    paging, and exposing it as a param aids with unit testing.
     */
    public WithinSearch(
            SearchHelper searchHelper,
            String searchValue,
            String searchWithin,
            Integer resultCount
    ) {
        this.searchHelper = searchHelper;
        this.searchValue = searchValue;
        this.searchWithin = SearchWithin.valueOf(searchWithin);
        this.resultCount = resultCount;

        this.likePattern =  Pattern.compile(
                String.format(".*%s.*", this.searchValue),
                Pattern.CASE_INSENSITIVE);
    }

    /**
     * The order events in this search `execute()` is different from others hence this note. Based on the historic
     * behaviour before the search was refactored, if a the searchWithin value was 'any' then the first search was
     * actually for a matching regIdentifier. Only if no matches were found did it then search against the other fields.
     *
     * Hence if 'any' is selected there is a possibility of running 2 searches; the first for any registrations with a
     * regIdentifier 'like' the `searchValue` provided. If no results we then make a second which looks in all 3 of the
     * possible search within fields.
     * @return List of registrations that matched the search criteria
     */
    public List<Registration> execute() {

        List<Registration> results = executeAny();
        if (!results.isEmpty()) return results;

        JacksonDBCollection<Registration, String> registrations = this.searchHelper.getCollection();

        DBQuery.Query query = determineQuery();
        DBSort.SortBuilder sortBy = determineSort();

        try {
            results = searchHelper.toList(registrations.find(query).limit(this.resultCount).sort(sortBy));
        } catch (IllegalArgumentException e) {
            log.severe("Caught exception: " + e.getMessage() + " - Cannot find registrations within");
        }

        return results;
    }

    private List<Registration> executeAny() {

        JacksonDBCollection<Registration, String> registrations = this.searchHelper.getCollection();

        DBQuery.Query query = DBQuery.regex("regIdentifier", likePattern);
        DBSort.SortBuilder sortBy = DBSort.asc("regIdentifier");

        List<Registration> results = new LinkedList<>();

        try {
            results = searchHelper.toList(registrations.find(query).limit(this.resultCount).sort(sortBy));
        } catch (IllegalArgumentException e) {
            log.severe("Caught exception: " + e.getMessage() + " - Cannot find registrations within any");
        }

        return results;
    }

    private DBQuery.Query determineQuery() {
        DBQuery.Query query = null;

        switch(this.searchWithin) {
            case any:
                query = DBQuery.or(DBQuery.regex("companyName", likePattern));
                query.or(DBQuery.regex("lastName", likePattern));
                query.or(DBQuery.regex("addresses.postcode", likePattern));
                break;
            case companyName:
                query = DBQuery.regex("companyName", likePattern);
                break;
            case contactName:
                query = DBQuery.regex("lastName", likePattern);
                break;
            case postcode:
                query = DBQuery.regex("addresses.postcode", likePattern);
                break;
        }

        return query;
    }

    /**
     * Essentially we sort the results based on the following
     * - If the user selected search within 'contactName' then we sort by last name
     * - If the user selected search within 'postcode' then we sort by postcode (we don't try and do anything special
     *      like sort by registered addresses then postal addresses)
     * - Else sort by company name
     *
     * If a user selected search within Any then the first search is actually against the `regIdentifier` and the
     * `executeAny()` method handles specifying a sort by that field.
     *
     * If however no matches are found we sort by `companyName` as the assumption is they have just entered the
     * organisation's name and not bothered to select the search within company name option.
     *
     * @return sort to use based on the type of searchWithin requested
     */
    private DBSort.SortBuilder determineSort() {

        if (this.searchWithin == SearchWithin.contactName) return DBSort.asc("lastName");
        if (this.searchWithin == SearchWithin.postcode) return DBSort.asc("addresses.postcode");

        return DBSort.asc("companyName");
    }
}
