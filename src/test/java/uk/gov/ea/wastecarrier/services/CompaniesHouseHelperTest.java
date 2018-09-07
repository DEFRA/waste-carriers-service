package uk.gov.ea.wastecarrier.services;

import org.junit.BeforeClass;
import org.junit.Test;
import uk.gov.ea.wastecarrier.services.helper.mocks.CompaniesHouseHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CompaniesHouseHelperTest {

    private static CompaniesHouseHelper helper;

    @BeforeClass
    public static void setup() {
        helper = new CompaniesHouseHelper();
    }

    @Test
    public void response() {
        String result = helper.response("00445790");
        assertTrue("Company name is TESCO PLC", result.contains("TESCO PLC"));
        assertTrue("Is active ",result.contains("active"));
    }

    @Test
    public void responseCompanyNumberIsNull() {
        assertEquals("{}", helper.response(null));
    }

    @Test
    public void companyCompanyNumberIsNotFound() {
        assertTrue(helper.response("99999999").contains("company-profile-not-found"));
    }
}
