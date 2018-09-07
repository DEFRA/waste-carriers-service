package uk.gov.ea.wastecarrier.services;

import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.internal.bytebuddy.utility.RandomString;
import org.junit.*;
import uk.gov.ea.wastecarrier.services.core.Entity;
import uk.gov.ea.wastecarrier.services.match.CompanyMatch;
import uk.gov.ea.wastecarrier.services.match.PersonMatch;
import uk.gov.ea.wastecarrier.services.support.EntityBuilder;
import uk.gov.ea.wastecarrier.services.support.EntityMatchingConnectionUtil;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EntityDaoTest {

    private static EntityMatchingConnectionUtil connection;

    @BeforeClass
    public static void setup() {
        connection = new EntityMatchingConnectionUtil();
    }

    /**
     * Deletes any entities we have created during testing
     */
    @AfterClass
    public static void tearDown() {
        connection.clean();
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
    public void insert() {
        Entity document = new EntityBuilder(EntityBuilder.BuildType.COMPANY)
                .build();

        String id = this.connection.dao.insert(document).id;

        assertTrue("The entity is inserted", id != null && !id.isEmpty());
    }

    @Test
    public void find() {
        Entity document = new EntityBuilder(EntityBuilder.BuildType.COMPANY)
                .build();

        String id = this.connection.dao.insert(document).id;

        document = this.connection.dao.find(id);

        assertEquals("The entity is found", id, document.id);
    }

    @Test
    public void recreate() {
        List<Entity> entities = new ArrayList<>();

        String companyName = new RandomString().nextString();
        entities.add(new EntityBuilder(EntityBuilder.BuildType.COMPANY)
                .name(companyName)
                .companyNumber(String.valueOf(RandomUtils.nextInt()))
                .build()
        );

        String personName = new RandomString().nextString();
        entities.add(new EntityBuilder(EntityBuilder.BuildType.PERSON)
                .name("Jason " + personName)
                .build()
        );

        this.connection.dao.recreate(entities);

        CompanyMatch companyMatcher = new CompanyMatch(this.connection.searchHelper, companyName, null);
        Entity company = companyMatcher.execute();

        PersonMatch personMatcher = new PersonMatch(this.connection.searchHelper, "Jason", personName, null);
        Entity person = personMatcher.execute();

        assertEquals("The collection is recreated with only 2 entities", 2, this.connection.dao.getCollection().count());
        assertEquals("A company with the name '" + companyName + "'", companyName, company.name);
        assertEquals("A person with the name of 'Jason " + personName + "'", "Jason " + personName, person.name);
    }
}
