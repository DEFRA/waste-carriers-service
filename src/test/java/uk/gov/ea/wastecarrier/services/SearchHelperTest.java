package uk.gov.ea.wastecarrier.services;

import org.joda.time.DateTime;
import org.junit.*;
import uk.gov.ea.wastecarrier.services.helper.SearchHelper;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SearchHelperTest {

    @Test
    public void dateStringToDateTime() {
        DateTime testDate = new DateTime(new Date(1514764800000L));

        assertEquals("dd/MM/yyyy", testDate, SearchHelper.dateStringToDateTime("01/01/2018"));
        assertEquals("dd-MM-yyyy", testDate, SearchHelper.dateStringToDateTime("01-01-2018"));
        assertEquals("dd MM yyyy", testDate, SearchHelper.dateStringToDateTime("01 01 2018"));
        assertEquals("ddMMyyyy", testDate, SearchHelper.dateStringToDateTime("01012018"));
        assertEquals("yyyy-MM-dd", testDate, SearchHelper.dateStringToDateTime("2018-01-01"));

        assertNull("Returns null when cannot parse datey", SearchHelper.dateStringToDateTime("ticktock"));
    }
}
