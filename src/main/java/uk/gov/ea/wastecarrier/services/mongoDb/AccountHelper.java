package uk.gov.ea.wastecarrier.services.mongoDb;


import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import uk.gov.ea.wastecarrier.services.core.Registration;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AccountHelper {

    private QueryHelper queryHelper;
    private Logger log = Logger.getLogger(AccountHelper.class.getName());

    public String accountEmail;
    public Set<String> status;

    public AccountHelper(QueryHelper queryHelper) {

        this.queryHelper = queryHelper;
    }

    public List<Registration> getRegistrations() {

        Map<String, Object> queryProps = authorQueryProperties();

        DBCursor cursor = getRegistrationCursorForAccount(queryProps);

        return queryHelper.toRegistrationList(cursor);
    }

    private DBCursor getRegistrationCursorForAccount(Map<String, Object> queryProps) {

        BasicDBObject query = new BasicDBObject();

        if (queryProps != null) {
            for (Map.Entry<String, Object> entry : queryProps.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof String[]) {
                    BasicDBObject inQuery = new BasicDBObject("$in", value);
                    query.append(entry.getKey(), inQuery);
                } else {
                    query.append(entry.getKey(), value);
                }
            }
        }

        if (log.isLoggable(Level.INFO)) {
            log.info(query.toString());
        }
        return queryHelper.getRegistrationsCollection().find(query);
    }

    private Map<String, Object> authorQueryProperties() {

        Map<String, Object> queryProps = new HashMap<String, Object>();
        Collection<String> accountEmailProp = new ArrayList<String>();
        accountEmailProp.add(accountEmail);

        queryHelper.addOptionalQueryProperty("accountEmail", accountEmailProp, queryProps);
        queryHelper.addOptionalQueryProperty("metaData.status", this.status, queryProps);

        return queryProps;
    }

}
