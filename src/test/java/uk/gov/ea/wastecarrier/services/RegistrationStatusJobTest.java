package uk.gov.ea.wastecarrier.services;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.gov.ea.wastecarrier.services.backgroundJobs.RegistrationStatusJob;
import uk.gov.ea.wastecarrier.services.core.MetaData;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.support.RegistrationBuilder;
import uk.gov.ea.wastecarrier.services.support.RegistrationsConnectionUtil;
import uk.gov.ea.wastecarrier.services.support.TestUtil;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class RegistrationStatusJobTest {

    private static RegistrationsConnectionUtil connection;

    @BeforeClass
    public static void setup() {
        connection = new RegistrationsConnectionUtil();
        createRegistrations();
    }

    @AfterClass
    public static void tearDown() {
        connection.clean();
    }

    @Test
    public void expireRegistrations() {
        RegistrationStatusJob job = new RegistrationStatusJob();
        job.expireRegistrations(connection.dao.getCollection());

        Registration reg = connection.dao.findByRegIdentifier("CBDU100");
        assertEquals("CBDU100 is expired", MetaData.RegistrationStatus.EXPIRED, reg.getMetaData().getStatus());

        reg = connection.dao.findByRegIdentifier("CBDU200");
        assertEquals("CBDU200 is expired", MetaData.RegistrationStatus.EXPIRED, reg.getMetaData().getStatus());

        reg = connection.dao.findByRegIdentifier("CBDU300");
        assertNotEquals("CBDU300 is not expired", MetaData.RegistrationStatus.EXPIRED, reg.getMetaData().getStatus());

        reg = connection.dao.findByRegIdentifier("CBDU400");
        assertNotEquals("CBDU300 is not expired", MetaData.RegistrationStatus.EXPIRED, reg.getMetaData().getStatus());
    }

    private static void createRegistrations() {
        // Expires today
        Registration reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER)
                .regIdentifier("CBDU100")
                .build();
        reg.setExpires_on(new Date());
        connection.dao.insert(reg);

        // Expires today
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER)
                .regIdentifier("CBDU200")
                .build();
        reg.setExpires_on(new Date());
        connection.dao.insert(reg);

        // Expires tomorrow
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER)
                .regIdentifier("CBDU300")
                .build();
        reg.setExpires_on(TestUtil.todayPlus(1, Calendar.DATE));
        connection.dao.insert(reg);

        // Expires in a year
        reg = new RegistrationBuilder(RegistrationBuilder.BuildType.UPPER)
                .regIdentifier("CBDU400")
                .build();
        reg.setExpires_on(TestUtil.todayPlus(1, Calendar.YEAR));
        connection.dao.insert(reg);
    }
}
