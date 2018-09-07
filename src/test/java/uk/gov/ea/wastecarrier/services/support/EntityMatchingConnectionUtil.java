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
        String uri = System.getenv("WCRS_TEST_REGSDB_URI");
        int timeout = Integer.valueOf(System.getenv("WCRS_TEST_MONGODB_SERVER_SEL_TIMEOUT"));

        DatabaseConfiguration config = new DatabaseConfiguration(uri, timeout);

        databaseHelper = new DatabaseHelper(config);
        dao = new EntityDao(config);
        searchHelper = new SearchHelper(databaseHelper, dao);
    }

    public void clean() {
        this.dao.getCollection().drop();
    }

    public EntityDao invalidCredentialsDao() {
        String validUsername = this.databaseHelper.configuration().getMongoClientURI().getUsername();
        String validUri = this.databaseHelper.configuration().getMongoClientURI().getURI();

        String invalidUri = validUri.replaceFirst(validUsername, "lockedOut");
        DatabaseConfiguration invalidConfig = new DatabaseConfiguration(
                invalidUri,
                this.databaseHelper.configuration().getServerSelectionTimeout()
        );
        return new EntityDao(invalidConfig);
    }
}
