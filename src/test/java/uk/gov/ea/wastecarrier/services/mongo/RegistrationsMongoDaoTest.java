/**
 * 
 */
package uk.gov.ea.wastecarrier.services.mongo;

import java.util.ArrayList;
import java.util.Date;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.FinanceDetails;
import uk.gov.ea.wastecarrier.services.core.MetaData;
import uk.gov.ea.wastecarrier.services.core.MetaData.RegistrationStatus;
import uk.gov.ea.wastecarrier.services.core.MetaData.RouteType;
import uk.gov.ea.wastecarrier.services.core.Order;
import uk.gov.ea.wastecarrier.services.core.Payment;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;
import uk.gov.ea.wastecarrier.services.mongoDb.OrdersMongoDao;
import uk.gov.ea.wastecarrier.services.mongoDb.PaymentsMongoDao;
import uk.gov.ea.wastecarrier.services.mongoDb.RegistrationsMongoDao;
import junit.framework.TestCase;

/**
 * Tests (involving the MongoDB database) for the Registrations DAO.
 * @author gmueller
 *
 */
public class RegistrationsMongoDaoTest extends TestCase {

	private RegistrationsMongoDao dao;
	
	private OrdersMongoDao ordersDao;
	
	private PaymentsMongoDao paymentsDao;
	
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	//TODO RE-enable after variables have been set up on the Jenkins server
	protected void donNotSetUp() throws Exception {
		//super.setUp();
		
		//TODO Get config from DW config files
		String dbHost = System.getenv("WCRS_SERVICES_DB_HOST");
		int dbPort = Integer.valueOf(System.getenv("WCRS_SERVICES_DB_PORT"));
		String dbName = System.getenv("WCRS_SERVICES_DB_NAME");
		String dbUser = System.getenv("WCRS_SERVICES_DB_USER");
		String dbPassword = System.getenv("WCRS_SERVICES_DB_PASSWD");
		DatabaseConfiguration dbConfig = new DatabaseConfiguration(dbHost, dbPort, dbName, dbUser, dbPassword);
		DatabaseHelper databaseHelper = new DatabaseHelper(dbConfig);
		dao = new RegistrationsMongoDao(databaseHelper);
		
		ordersDao = new OrdersMongoDao(dbConfig);
		
		paymentsDao = new PaymentsMongoDao(dbConfig);
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link uk.gov.ea.wastecarrier.services.mongoDb.RegistrationsMongoDao#getNumberOfRegistrationsTotal()}.
	 */
	//TODO RE-enable tests
	public void testGetNumberOfRegistrationsTotal() {
		//long total = dao.getNumberOfRegistrationsTotal();
		//System.out.println("total = " + total);
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
	
	//TODO Re-enable tests
	public void doNotTestOrdersAndPayments()
	{
		Registration registration = dao.insertRegistration(initializeSampleUpperTierRegistration());
		String id = registration.getId();
		
		Order order = new Order();
		order.setPaymentMethod(Order.PaymentMethod.UNKNOWN);
		order.setDescription("Initial registration");
		order.setOrderCode("1234");
		order.setCurrency("GBP");
		order.setTotalAmount(15400);
		order.setWorldPayStatus("NEW");
		order.setDateCreated(new Date());
				
		ordersDao.addOrder(id, order);
		
		registration = dao.getRegistration(id);
		
		assertEquals("The registration must now have an order", registration.getFinanceDetails().getOrders().size(), 1);

		Order anotherOrder = new Order();
		anotherOrder.setPaymentMethod(Order.PaymentMethod.OFFLINE);
		anotherOrder.setDescription("Some other order for this registration");
		anotherOrder.setOrderCode("2345");
		anotherOrder.setCurrency("GBP");
		anotherOrder.setTotalAmount(2000);
		anotherOrder.setWorldPayStatus("NEW");
		anotherOrder.setDateCreated(new Date());
		ordersDao.addOrder(id, anotherOrder);

		Order savedOrder = registration.getFinanceDetails().getOrders().get(0);
		
		assertEquals("The order has a status", "NEW", savedOrder.getWorldPayStatus());
		
		savedOrder.setWorldPayStatus("UPDATED");
		savedOrder.setPaymentMethod(Order.PaymentMethod.ONLINE);
		
		ordersDao.updateOrder(id, "1234", savedOrder);
		
		registration = dao.getRegistration(id);
		assertEquals("The registration must still have two orders", registration.getFinanceDetails().getOrders().size(), 2);
		Order updatedOrder = registration.getFinanceDetails().getOrders().get(0);
		assertEquals("The order status has been updated in the database", "UPDATED", updatedOrder.getWorldPayStatus());
		assertEquals("The payment method has been updated", "ONLINE", updatedOrder.getPaymentMethod().toString());
		
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
		reg.setHouseNumber("123");
		reg.setStreetLine1("Upper Street");
		reg.setTownCity("Bristol");
		reg.setPostcode("BS1 5AH");
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
		reg.getMetaData().setDateRegistered(new Date().toString());
		reg.getMetaData().setStatus(RegistrationStatus.ACTIVE);
		reg.getMetaData().setRoute(RouteType.DIGITAL);
		return reg;
	}
	
}
