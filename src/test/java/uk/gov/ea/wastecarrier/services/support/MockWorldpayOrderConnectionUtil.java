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
        String uri = System.getenv("WCRS_TEST_REGSDB_URI");
        int timeout = Integer.valueOf(System.getenv("WCRS_TEST_MONGODB_SERVER_SEL_TIMEOUT"));

        this.databaseConfig = new DatabaseConfiguration(uri, timeout);

        databaseHelper = new DatabaseHelper(this.databaseConfig);
        dao = new MockWorldpayDao(this.databaseConfig);
        searchHelper = new SearchHelper(databaseHelper, dao);
    }

    public void clean() {
        this.dao.getCollection().drop();
    }

    public MockWorldpayDao invalidCredentialsDao() {
        String validUsername = this.databaseHelper.configuration().getMongoClientURI().getUsername();
        String validUri = this.databaseHelper.configuration().getMongoClientURI().getURI();

        String invalidUri = validUri.replaceFirst(validUsername, "lockedOut");
        DatabaseConfiguration invalidConfig = new DatabaseConfiguration(
                invalidUri,
                this.databaseHelper.configuration().getServerSelectionTimeout()
        );
        return new MockWorldpayDao(invalidConfig);
    }
}
