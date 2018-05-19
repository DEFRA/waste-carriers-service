package uk.gov.ea.wastecarrier.services.support;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.dao.EntityMatchingDao;
import uk.gov.ea.wastecarrier.services.helper.DatabaseHelper;
import uk.gov.ea.wastecarrier.services.helper.SearchHelper;

public class EntityMatchingConnectionUtil {

    public DatabaseHelper databaseHelper;
    public EntityMatchingDao dao;
    public SearchHelper searchHelper;

    public EntityMatchingConnectionUtil() {
        String host = System.getenv("WCRS_SERVICES_EM_HOST_TEST");
        int port = Integer.valueOf(System.getenv("WCRS_SERVICES_EM_PORT_TEST"));
        String name = System.getenv("WCRS_SERVICES_EM_NAME_TEST");
        String username = System.getenv("WCRS_SERVICES_EM_USER_TEST");
        String password = System.getenv("WCRS_SERVICES_EM_PASSWD_TEST");

        DatabaseConfiguration config = new DatabaseConfiguration(host, port, name, username, password);

        databaseHelper = new DatabaseHelper(config);
        dao = new EntityMatchingDao(config);
        searchHelper = new SearchHelper(databaseHelper, dao);
    }

    public void clean() {
        this.dao.getCollection().drop();
    }
}
