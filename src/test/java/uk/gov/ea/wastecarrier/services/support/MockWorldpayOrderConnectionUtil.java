package uk.gov.ea.wastecarrier.services.support;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.dao.MockWorldpayDao;
import uk.gov.ea.wastecarrier.services.helper.DatabaseHelper;
import uk.gov.ea.wastecarrier.services.helper.SearchHelper;

public class MockWorldpayOrderConnectionUtil {

    public DatabaseConfiguration databaseConfig;
    public DatabaseHelper databaseHelper;
    public MockWorldpayDao dao;
    public SearchHelper searchHelper;

    public MockWorldpayOrderConnectionUtil() {
        String url  = System.getenv("WCRS_TEST_REGSDB_URL1");
        String name = System.getenv("WCRS_TEST_REGSDB_NAME");
        String username = System.getenv("WCRS_TEST_REGSDB_USERNAME");
        String password = System.getenv("WCRS_TEST_REGSDB_PASSWORD");
        int timeout = Integer.valueOf(System.getenv("WCRS_TEST_REGSDB_SERVER_SEL_TIMEOUT"));

        this.databaseConfig = new DatabaseConfiguration(url, name, username, password, timeout);

        databaseHelper = new DatabaseHelper(this.databaseConfig);
        dao = new MockWorldpayDao(this.databaseConfig);
        searchHelper = new SearchHelper(databaseHelper, dao);
    }

    public void clean() {
        this.dao.getCollection().drop();
    }

    public MockWorldpayDao invalidCredentialsDao() {
        DatabaseConfiguration invalidConfig = new DatabaseConfiguration(
                this.databaseHelper.configuration().getUrl(),
                this.databaseHelper.configuration().getName(),
                this.databaseHelper.configuration().getUsername(),
                "Bl0wMeDownWithAFeather",
                this.databaseHelper.configuration().getServerSelectionTimeout()
        );
        return new MockWorldpayDao(invalidConfig);
    }
}
