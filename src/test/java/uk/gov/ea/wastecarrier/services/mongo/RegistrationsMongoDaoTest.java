package uk.gov.ea.wastecarrier.services.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.*;
import uk.gov.ea.wastecarrier.services.core.MetaData.RegistrationStatus;
import uk.gov.ea.wastecarrier.services.core.MetaData.RouteType;
import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;
import uk.gov.ea.wastecarrier.services.mongoDb.RegistrationsMongoDao;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * Tests (involving the MongoDB database) for the Registrations DAO.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RegistrationsMongoDaoTest {

	private static RegistrationsMongoDao dao;
	private static DatabaseHelper helper;

	private static final String lowerTierRegNumber = "CBDL99999";

	@BeforeClass
    public static void runOnceBefore() {
	    String host = System.getenv("WCRS_SERVICES_DB_HOST");
        int port = Integer.valueOf(System.getenv("WCRS_SERVICES_DB_PORT"));
		String name = System.getenv("WCRS_SERVICES_DB_NAME");
		String username = System.getenv("WCRS_SERVICES_DB_USER");
		String password = System.getenv("WCRS_SERVICES_DB_PASSWD");

		DatabaseConfiguration config = new DatabaseConfiguration(host, port, name, username, password);

		helper = new DatabaseHelper(config);
		dao = new RegistrationsMongoDao(config);
	}

    /**
     * Deletes any registrations we have created during testing
     * @throws Exception
     */
	@AfterClass
    public static void runOnceAfter() {
		DB db = helper.getConnection();
        DBCollection collection = db.getCollection("registrations");

        BasicDBObject lowerTierQuery = new BasicDBObject("regIdentifier", lowerTierRegNumber);
        collection.remove(lowerTierQuery);
	}

    /**
     * Check we can connect to MongoDb. Simply creating a connection is not a
     * guarantee that everything is working. So we also call
     * getNumberOfRegistrationsTotal() as this performs a query against the DB,
     * and checks we can connect, authenticate, query and return a result
     */
	@Test
	public void test1_getNumberOfRegistrationsTotal() {
		dao.getNumberOfRegistrationsTotal();
	}

    /**
     * Check we can insert a new lower tier registration into the registrations
     * collection. This goes beyond simply connecting and querying, and
     * checks that the Monjojack POJO mapping is working correctly.
     */
    @Test
    public void test2_insertRegistration() {
        Registration reg = dao.insertRegistration(generateLowerTier());
        String id = reg.getId();
        assertTrue("The registration is inserted", id != null && !id.isEmpty());
    }

    /**
     * Check we can find our registration
     */
    @Test
    public void test3_findRegistration() {
        Registration reg = dao.findRegistration(lowerTierRegNumber);
        String regNo = reg.getRegIdentifier();
        assertEquals("The registration is found", lowerTierRegNumber, regNo);
    }

    /**
     * Check we can update our registration
     */
    @Test
    public void test4_updateRegistration() {
        Registration reg = dao.findRegistration(lowerTierRegNumber);
        reg.getMetaData().setStatus(RegistrationStatus.ACTIVE);
        reg.getMetaData().setDateActivated(new Date());
        reg = dao.updateRegistration(reg);
        assertEquals(
                "The registration is updated",
                RegistrationStatus.ACTIVE,
                reg.getMetaData().getStatus()
        );
    }

    /**
     * Test method for {@link uk.gov.ea.wastecarrier.services.mongoDb.RegistrationsMongoDao#getNumberOfRegistrationsPending()}.
     */
    //TODO Re-enable tests
    public void doNotTestGetNumberOfRegistrationsPending() {
        //long total = dao.getNumberOfRegistrationsPending();
        //System.out.println("total = " + total);
    }

	//TODO Re-enable tests
	public void doNotTestInsertAndGetRegistration()
	{
		Registration registration = initializeSampleUpperTierRegistration();
		Registration savedRegistration = dao.insertRegistration(registration);
		String id = savedRegistration.getId();
		assertTrue("The registration must have an id", id != null && !id.isEmpty());

		Registration foundRegistration = dao.getRegistration(id);
		assertNotNull("The registrationmust not be null", foundRegistration);
	}

	private Registration initializeSampleUpperTierRegistration()
	{
		Registration reg = new Registration();
		reg.setTier(Registration.RegistrationTier.UPPER);
		reg.setRegistrationType("carrier_dealer");
		reg.setBusinessType("soleTrader");
		reg.setOtherBusinesses("yes");
		reg.setIsMainService("yes");
		reg.setOnlyAMF("no");
		reg.setCompanyName("Upper Waste Carriers");
		ArrayList<Address> addresses = new ArrayList<Address>();
		Address regAddress = new Address();
		regAddress.setAddressType(Address.addressType.REGISTERED);
		regAddress.setHouseNumber("123");
		regAddress.setAddressLine1("Upper Street");
		regAddress.setTownCity("Bristol");
		regAddress.setPostcode("BS1 5AH");
		reg.setAddresses(addresses);
		reg.setFirstName("Joe");
		reg.setLastName("Bloggs");
		reg.setPosition("Chief Waster");
		reg.setPhoneNumber("0123 456 789");
		reg.setRegIdentifier("CBDU" + System.currentTimeMillis());
        reg.setDeclaredConvictions("no");
		reg.setDeclaration("1");
		reg.setAccountEmail("upper@example.com");
		reg.setFinanceDetails(new FinanceDetails());
		reg.getFinanceDetails().setOrders(new ArrayList<Order>());
		reg.getFinanceDetails().setPayments(new ArrayList<Payment>());
		reg.setMetaData(new MetaData());
		reg.getMetaData().setDateRegistered(new Date());
		reg.getMetaData().setStatus(RegistrationStatus.ACTIVE);
		reg.getMetaData().setRoute(RouteType.DIGITAL);
		return reg;
	}

	private Registration generateLowerTier() {
	    Registration reg = new Registration();

	    reg.setUuid(generateUUID());
	    reg.setRegIdentifier(lowerTierRegNumber);
	    reg.setTier(Registration.RegistrationTier.LOWER);
	    reg.setBusinessType("soleTrader");
	    reg.setOtherBusinesses("no");
	    reg.setConstructionWaste("no");
	    reg.setCompanyName("WCR Service test LT");
	    reg.setFirstName("Jason");
	    reg.setLastName("Isaac");
	    reg.setPhoneNumber("01179345400");
	    reg.setContactEmail("jason@example.com");
	    reg.setAccountEmail("jason@example.com");
	    reg.setDeclaration("1");

        ArrayList<Address> addresses = new ArrayList<Address>();
        addresses.add(generateAddress(Address.addressType.REGISTERED));
        addresses.add(generateAddress(Address.addressType.POSTAL));
        reg.setAddresses(addresses);

        reg.setMetaData(generateMetaData(RegistrationStatus.PENDING));

        reg.setFinanceDetails(generateFinanceDetails());

	    return reg;
    }

    private Address generateAddress(Address.addressType type) {
	    Address addr = new Address();

	    addr.setAddressType(type);
        addr.setHouseNumber("123");
        addr.setAddressLine1("Upper Street");
        addr.setTownCity("Bristol");
        addr.setPostcode("BS1 5AH");

        if (type == Address.addressType.POSTAL) {
            addr.setFirstName("Jason");
            addr.setLastName("Isaac");
        }

        return addr;
    }

    private MetaData generateMetaData(RegistrationStatus status) {
	    MetaData data = new MetaData();

        data.setRoute(RouteType.DIGITAL);
        data.setDateRegistered(new Date());
        data.setLastModified(data.getDateRegistered());

	    if (status == RegistrationStatus.ACTIVE) {
            data.setStatus(RegistrationStatus.PENDING);
            data.setDateActivated(new Date());
        } else {
            data.setStatus(RegistrationStatus.PENDING);
        }

	    return data;
    }

    private FinanceDetails generateFinanceDetails() {
	    FinanceDetails details = new FinanceDetails();
        Order order = new Order();
        order.setOrderCode(Long.toString(new Date().getTime()));
        order.setOrderCode(order.getOrderId());
        order.setPaymentMethod(Order.PaymentMethod.ONLINE);
        order.setMerchantId("EASERRSIMECOM");
        order.setTotalAmount(0);
        order.setCurrency("GBP");
        order.setDateCreated(new Date());
        order.setWorldPayStatus("IN_PROGRESS");
        order.setDateLastUpdated(order.getDateCreated());
        order.setUpdatedByUser("agent@defra.gsi.gov.uk");

        ArrayList<Order> orders = new ArrayList<Order>();
        orders.add(order);
        details.setOrders(orders);

	    details.setBalance(0);

	    return details;
    }

    private String generateUUID() {
        String uuid = UUID.randomUUID().toString();

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(uuid.getBytes(StandardCharsets.UTF_8));
    }
	
}
