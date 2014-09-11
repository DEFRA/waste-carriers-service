package uk.gov.ea.wastecarrier.services.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.joda.time.DateTime;
import uk.gov.ea.wastecarrier.services.core.KeyPerson;
import uk.gov.ea.wastecarrier.services.core.Registration;

import java.util.ArrayList;
import java.util.List;

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
		reg.setRegistrationType("carrier_dealer");
		reg.setBusinessType("soleTrader");
		reg.setOtherBusinesses("no");
		reg.setConstructionWaste("yes");
		reg.setCompanyName("Acme Waste");
		reg.setHouseNumber("1");
		reg.setStreetLine1("Deanery Road");
		reg.setStreetLine2("Bristol");
		reg.setPostcode("BS1 5AH");
		reg.setFirstName("Jane");
		reg.setLastName("Smith");
		reg.setPosition("Owner");
		reg.setPhoneNumber("01179345400");
		reg.setContactEmail("info@example.com");
		reg.setAccessCode("ABCDEF");
		reg.setTotalFee("164");
		reg.setRegistrationFee("154");
		reg.setCopyCardFee("5");
		reg.setCopyCards("2");
		reg.setAccountEmail("jane@example.com");
        reg.setDeclaredConvictions("no");
		reg.setDeclaration("on");
		reg.setExpiresOn(new DateTime(2017, 11, 30, 0, 0).toDate());
		reg.setOriginalRegistrationNumber("OLDREG123");

        List<KeyPerson> keyPeople = new ArrayList<KeyPerson>();
        keyPeople.add(
                new KeyPerson(
                        "John",
                        "Smith",
                        new DateTime(1970, 1, 1, 0, 0).toDate(),
                        "Director",
                        KeyPerson.PersonType.KEY,
                        null)
        );
        reg.setKeyPeople(keyPeople);

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
	    System.out.println(jsonFixture("fixtures/emptyRegistration.json"));
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
	    System.out.println(jsonFixture("fixtures/fullRegistration.json"));
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
        String asJson = asJson(reg);
        System.out.println(asJson);
        System.out.println(jsonFixture("fixtures/fullRegistration.json"));
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
        String asJson = asJson(reg);
        System.out.println(asJson);
        System.out.println(jsonFixture("fixtures/fullRegistration.json"));
        Registration test = fromJson(jsonFixture("fixtures/fullRegistration.json"), Registration.class);
        System.out.println(test.equals(reg));
		assertFalse("a Registration can be deserialized from JSON", 
				fromJson(jsonFixture("fixtures/fullRegistration.json"), Registration.class).equals(reg));
	}

}
