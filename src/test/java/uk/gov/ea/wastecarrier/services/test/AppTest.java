package uk.gov.ea.wastecarrier.services.test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.gov.ea.wastecarrier.services.core.Registration;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import static com.yammer.dropwizard.testing.JsonHelpers.*;

/**
 * Unit test for simple App. This stub class should be expanded upon when functionality is available
 */
public class AppTest extends TestCase
{
	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public AppTest(String testName)
	{
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite()
	{
		return new TestSuite(AppTest.class);
	}

	/**
	 * Rigourous Test :-)
	 */
	public void testApp()
	{
		//TODO What test?
		assertTrue(true);
	}
	
	/**
	 * Test of the Service to perform an action
	 */
	public void testService()
	{
		//TODO What test?
		assertTrue(true);
	}
	
	private Registration getFullRegistrationDetails(final Registration reg)
	{
		// Setup Matching Registration object to match full File defined in JSON
		reg.setTier(Registration.RegistrationTier.UPPER);
		reg.setBusinessType("businessTypeVal");
		reg.setCompanyName("testJSONcompanyName");
		reg.setIndividualsType("indivTypeVal");
		reg.setHouseNumber("1");
		reg.setPostcode("BS3 3GE");
		reg.setTitle("Other");
		reg.setOtherTitle("Lord");
		reg.setFirstName("firstname");
		reg.setLastName("lastname");
		reg.setPosition("CEO");
		reg.setPhoneNumber("34534654");
		reg.setContactEmail("email@you.com");
		reg.setAccountEmail("account@you.com");
		reg.setDeclaration("on");
		reg.setAccessCode("ABCDEF");
		reg.setExpiresOn(new Date(2017, 11, 31));
		return reg;
	}
	
	/**
	 * Tests an Empty Registration object can be converted to JSON
	 * 
	 * @throws Exception
	 */
	public void testSerializesToEmptyJSON() throws Exception 
	{
	    final Registration reg = new Registration();
	    String asJson = asJson(reg);
	    System.out.println(asJson);
	    assertEquals("a Registration can be serialized to JSON",
	               asJson,
	               jsonFixture("fixtures/emptyRegistration.json") );
	}
	
	/**
	 * Tests an Full Registration object can be converted to JSON
	 * 
	 * @throws Exception
	 */
	public void testSerializesToFullJSON() throws Exception 
	{
	    final Registration reg = getFullRegistrationDetails(new Registration());
	    String asJson = asJson(reg);
	    System.out.println(asJson);
	    assertEquals("a Registration can be serialized to JSON",
	               asJson,
	               jsonFixture("fixtures/fullRegistration.json") );
	}
	
	/**
	 * Tests an Empty JSON object can be converted to Registration details 
	 * 
	 * @throws Exception
	 */
	public void testDeserializesFromEmptyJSON() throws Exception 
	{
	    final Registration reg = new Registration();
	    assertEquals("a Registration can be deserialized from JSON",
	               fromJson(jsonFixture("fixtures/emptyRegistration.json"), Registration.class),
	               reg);
	}
	
	/**
	 * Tests an Full JSON object can be converted to Registration details and matches expected
	 * 
	 * @throws Exception
	 */
	public void testDeserializesFromFullJSON() throws Exception 
	{
		final Registration reg = getFullRegistrationDetails(new Registration());
	    assertEquals("a Registration can be deserialized from JSON",
	               fromJson(jsonFixture("fixtures/fullRegistration.json"), Registration.class),
	               reg);
	}
	
	/**
	 * Tests an changed JSON object doesn't match the specified full registration information
	 * 
	 * @throws Exception
	 */
	public void testFailureDeserializesFromFullJSON() throws Exception 
	{
		Registration reg = getFullRegistrationDetails(new Registration());
		reg.setBusinessType("changedBusinessName");
		assertFalse("a Registration can be deserialized from JSON", 
				fromJson(jsonFixture("fixtures/fullRegistration.json"), Registration.class).equals(reg));
	}

}
