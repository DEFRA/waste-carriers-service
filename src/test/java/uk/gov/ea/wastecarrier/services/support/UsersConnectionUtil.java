package uk.gov.ea.wastecarrier.services.support;

import org.mongojack.JacksonDBCollection;
import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.User;
import uk.gov.ea.wastecarrier.services.dao.UserDao;
import uk.gov.ea.wastecarrier.services.helper.DatabaseHelper;

public class UsersConnectionUtil {

    public DatabaseHelper databaseHelper;
    public UserDao dao;

    public UsersConnectionUtil() {
        String host = System.getenv("WCRS_SERVICES_USR_HOST_TEST");
        int port = Integer.valueOf(System.getenv("WCRS_SERVICES_USR_PORT_TEST"));
        String name = System.getenv("WCRS_SERVICES_USR_NAME_TEST");
        String username = System.getenv("WCRS_SERVICES_USR_USER_TEST");
        String password = System.getenv("WCRS_SERVICES_USR_PASSWD_TEST");
        int timeout = Integer.valueOf(System.getenv("WCRS_SERVICES_USR_SERVER_SEL_TIMEOUT_TEST"));

        DatabaseConfiguration config = new DatabaseConfiguration(host, port, name, username, password, timeout);

        databaseHelper = new DatabaseHelper(config);
        dao = new UserDao(config);
    }

    public void clean() {
        this.dao.getCollection().drop();
    }

    /**
     * The UserDao class does not have an insert function as it is not required
     * by the app at this time (that's all done from the front end).
     *
     * However we need users in the db in order to test the findByEmail method
     * hence we implement an insert in this test support class.
     *
     * @param user User to insert into the test users collection
     */
    public void insert(User user) {
        JacksonDBCollection<User, String> collection = this.dao.getCollection();

        // Insert user into database
        collection.insert(user);
    }

    public UserDao invalidCredentialsDao() {
        DatabaseConfiguration invalidConfig = new DatabaseConfiguration(
                this.databaseHelper.configuration().getHost(),
                this.databaseHelper.configuration().getPort(),
                this.databaseHelper.configuration().getName(),
                this.databaseHelper.configuration().getUsername(),
                "Bl0wMeDownWithAFeather",
                this.databaseHelper.configuration().getServerSelectionTimeout()
        );
        return new UserDao(invalidConfig);
    }
}
