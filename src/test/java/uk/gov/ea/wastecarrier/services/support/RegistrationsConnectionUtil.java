package uk.gov.ea.wastecarrier.services.support;

import com.mongodb.DBCollection;
import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;
import uk.gov.ea.wastecarrier.services.mongoDb.RegistrationsMongoDao;
import uk.gov.ea.wastecarrier.services.search.SearchHelper;

public class RegistrationsConnectionUtil {

    public SearchHelper searchHelper;
    public DatabaseHelper databaseHelper;
    public RegistrationsMongoDao registrationsDao;

    public RegistrationsConnectionUtil()  {
        String host = System.getenv("WCRS_SERVICES_DB_HOST TEST");
        int port = Integer.valueOf(System.getenv("WCRS_SERVICES_DB_PORT_TEST"));
        String name = System.getenv("WCRS_SERVICES_DB_NAME_TEST");
        String username = System.getenv("WCRS_SERVICES_DB_USER_TEST");
        String password = System.getenv("WCRS_SERVICES_DB_PASSWD_TEST");

        DatabaseConfiguration config = new DatabaseConfiguration(host, port, name, username, password);

        databaseHelper = new DatabaseHelper(config);
        searchHelper = new SearchHelper(databaseHelper);
        registrationsDao = new RegistrationsMongoDao(config);
    }

    public void clean() {
        DBCollection registrations = this.databaseHelper.getCollection(Registration.COLLECTION_NAME);
        registrations.drop();
    }
}
