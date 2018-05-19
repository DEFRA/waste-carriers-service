package uk.gov.ea.wastecarrier.services.support;

import com.mongodb.DBCollection;
import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;

public class EntityMatchingConnectionUtil {
    public DatabaseHelper databaseHelper;

    public EntityMatchingConnectionUtil() {
        String host = System.getenv("WCRS_SERVICES_EM_HOST_TEST");
        int port = Integer.valueOf(System.getenv("WCRS_SERVICES_EM_PORT_TEST"));
        String name = System.getenv("WCRS_SERVICES_EM_NAME_TEST");
        String username = System.getenv("WCRS_SERVICES_EM_USER_TEST");
        String password = System.getenv("WCRS_SERVICES_EM_PASSWD_TEST");

        DatabaseConfiguration config = new DatabaseConfiguration(host, port, name, username, password);

        databaseHelper = new DatabaseHelper(config);
    }

    public void clean() {
        DBCollection registrations = this.databaseHelper.getCollection("entities");
        registrations.drop();
    }
}
