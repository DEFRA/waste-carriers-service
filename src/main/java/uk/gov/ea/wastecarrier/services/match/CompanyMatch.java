package uk.gov.ea.wastecarrier.services.match;

import org.mongojack.DBQuery;
import org.mongojack.DBSort;
import org.mongojack.JacksonDBCollection;
import uk.gov.ea.wastecarrier.services.core.Entity;
import uk.gov.ea.wastecarrier.services.helper.SearchHelper;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class CompanyMatch {

    private SearchHelper helper;
    private Logger log = Logger.getLogger(CompanyMatch.class.getName());

    // Create a list of words we IGNORE when searching for company names.
    // Make sure to only use lower-case.
    private List<String> ignoredNameWords = Arrays.asList(
            // Company types / suffixes.
            "limited", "ltd", "plc", "inc", "incorporated", "llp", "lp", "company",
            "co", "holdings", "investments", "services", "technologies", "solutions",
            "group", "cyf", "cyfyngedig", "ccc", "cic", "cio", "ag", "corp", "eurl",
            "gmbh", "sa", "sarl", "sp", "prc", "partners", "lc",
            // Locations.
            "uk", "gb", "europe", "intl", "international", "england", "wales",
            "scotland", "cymru"
    );

    private String name;
    private String number;

    public CompanyMatch(SearchHelper helper, String name, String number) {
        this.helper = helper;
        this.name = parseName(name);
        this.number = parseNumber(number);
    }

    public Entity execute() {

        if (this.name.isEmpty() && this.number.isEmpty()) return null;

        JacksonDBCollection<Entity, String> collection = this.helper.getCollection();
        Entity document = null;

        try {
            // If we have a number search by that first
            if (!this.number.isEmpty()) {
                document = numberMatch(collection);

                // If no match try again this time removing any leading zeroes
                if (document == null) document = numberMatchIgnoreZeroes(collection);
            }
            // If here all number searches have failed or we never had one in
            // the first place. In which case try a search by name
            if (!this.name.isEmpty() && document == null) document = nameMatch(collection);
        } catch (IllegalArgumentException e) {
            log.severe(String.format(
                    "Error matching company/number %s/%s: %s",
                    this.name,
                    this.number,
                    e.getMessage()
            ));
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        return document;
    }

    private Entity nameMatch(JacksonDBCollection<Entity, String> collection) {

        Pattern likePattern = Pattern.compile(
                String.format(".*%s.*", this.name),
                Pattern.CASE_INSENSITIVE);

        DBQuery.Query query = DBQuery.and(DBQuery
                .is("dateOfBirth", null)
                .regex("name", likePattern)
        );

        return collection.findOne(query);
    }

    private Entity numberMatch(JacksonDBCollection<Entity, String> collection) {

        DBQuery.Query query = DBQuery.is("companyNumber", this.number);

        return collection.findOne(query);
    }

    private Entity numberMatchIgnoreZeroes(JacksonDBCollection<Entity, String> collection) {

        String noLeadingZeroes = this.number.replaceFirst("^0+(?!$)", "");

        Pattern likePattern = Pattern.compile(
                String.format(".*%s", noLeadingZeroes),
                Pattern.CASE_INSENSITIVE);

        DBQuery.Query query = DBQuery.regex("companyNumber", likePattern);
        DBSort.SortBuilder sortBy = DBSort.asc("companyNumber");

        return collection.find(query).limit(1).sort(sortBy).next();
    }

    private String parseName(String name) {

        if (name == null || name.trim().isEmpty()) return "";

        StringBuilder parsedName = new StringBuilder();

        for (String word: Arrays.asList(name.toLowerCase().split(" "))) {
            if (!word.isEmpty() && !ignoredNameWords.contains(word)) {
                parsedName.append(word + ' ');
            }
        }

        return parsedName.toString().trim();
    }

    private String parseNumber(String number) {
        if (number == null) return "";

        return number.trim();
    }
}
