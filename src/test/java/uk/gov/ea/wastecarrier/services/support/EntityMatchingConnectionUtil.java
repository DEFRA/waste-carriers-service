package uk.gov.ea.wastecarrier.services.support;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.dao.EntityDao;
import uk.gov.ea.wastecarrier.services.helper.DatabaseHelper;
import uk.gov.ea.wastecarrier.services.helper.SearchHelper;

public class EntityMatchingConnectionUtil {

    public DatabaseHelper databaseHelper;
    public EntityDao dao;
    public SearchHelper searchHelper;

    public EntityMatchingConnectionUtil() {
        String host = System.getenv("WCRS_SERVICES_EM_HOST_TEST");
        int port = Integer.valueOf(System.getenv("WCRS_SERVICES_EM_PORT_TEST"));
        String name = System.getenv("WCRS_SERVICES_EM_NAME_TEST");
        String username = System.getenv("WCRS_SERVICES_EM_USER_TEST");
        String password = System.getenv("WCRS_SERVICES_EM_PASSWD_TEST");
        int timeout = Integer.valueOf(System.getenv("WCRS_SERVICES_EM_SERVER_SEL_TIMEOUT_TEST"));

        DatabaseConfiguration config = new DatabaseConfiguration(host, port, name, username, password, timeout);

        databaseHelper = new DatabaseHelper(config);
        dao = new EntityDao(config);
        searchHelper = new SearchHelper(databaseHelper, dao);
    }

    public void clean() {
        this.dao.getCollection().drop();
    }

    public EntityDao invalidCredentialsDao() {
        DatabaseConfiguration invalidConfig = new DatabaseConfiguration(
                this.databaseHelper.configuration().getHost(),
                this.databaseHelper.configuration().getPort(),
                this.databaseHelper.configuration().getName(),
                this.databaseHelper.configuration().getUsername(),
                "Bl0wMeDownWithAFeather",
                this.databaseHelper.configuration().getServerSelectionTimeout()
        );
        return new EntityDao(invalidConfig);
    }
}
