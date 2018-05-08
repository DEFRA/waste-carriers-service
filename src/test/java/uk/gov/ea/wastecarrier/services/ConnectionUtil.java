package uk.gov.ea.wastecarrier.services;

import com.mongodb.DBCollection;
import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;
import uk.gov.ea.wastecarrier.services.mongoDb.RegistrationsMongoDao;
import uk.gov.ea.wastecarrier.services.mongoDb.SearchHelper;

public class ConnectionUtil {

    public SearchHelper searchHelper;
    public DatabaseHelper databaseHelper;
    public RegistrationsMongoDao registrationsDao;

    public ConnectionUtil()  {
        String host = System.getenv("WCRS_SERVICES_DB_HOST");
        int port = Integer.valueOf(System.getenv("WCRS_SERVICES_DB_PORT"));
        String name = System.getenv("WCRS_SERVICES_DB_NAME");
        String username = System.getenv("WCRS_SERVICES_DB_USER");
        String password = System.getenv("WCRS_SERVICES_DB_PASSWD");

        DatabaseConfiguration config = new DatabaseConfiguration(host, port, name, username, password);

        databaseHelper = new DatabaseHelper(config);
        searchHelper = new SearchHelper(databaseHelper);
        registrationsDao = new RegistrationsMongoDao(config);
    }

    public void clean() {
        DBCollection registrations = this.searchHelper.getRegistrationsCollection();
        registrations.drop();
    }
}
