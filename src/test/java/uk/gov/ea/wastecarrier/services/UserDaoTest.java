package uk.gov.ea.wastecarrier.services;

import org.junit.*;
import uk.gov.ea.wastecarrier.services.core.User;
import uk.gov.ea.wastecarrier.services.support.UserBuilder;
import uk.gov.ea.wastecarrier.services.support.UsersConnectionUtil;

import static org.junit.Assert.*;

public class UserDaoTest {

    private static UsersConnectionUtil connection;

    @BeforeClass
    public static void setup() {
        connection = new UsersConnectionUtil();
        createTestData();
    }

    /**
     * Deletes any users we have created during testing
     */
    @AfterClass
    public static void tearDown() {
//        connection.clean();
    }

    @Test
    public void checkConnection() {
        assertTrue("Returns true when credentials are valid", connection.dao.checkConnection());
    }

    @Test(expected = Exception.class)
    public void checkConnectionThrowsExceptionWhenConfigInvalid() {
        connection.invalidCredentialsDao().checkConnection();
    }

    @Test
    public void findByEmail() {
        User document = connection.dao.findByEmail("mark.kermode@example.com");

        assertEquals("We can find 'mark.kermode@example.com'", "mark.kermode@example.com", document.getEmail());
    }

    @Test
    public void findByEmailReturnsNullWhenNotFound() {
        User document = connection.dao.findByEmail("robintheproducer@example.com");

        assertNull("We cannot find 'robintheproducer@example.com'", document);
    }

    private static void createTestData() {
        User document = new UserBuilder()
                .build();
        connection.insert(document);

        document = new UserBuilder()
                .email("mark.kermode@example.com")
                .build();
        connection.insert(document);

        document = new UserBuilder()
                .email("simon.mayo@example.com")
                .build();
        connection.insert(document);
    }
}
