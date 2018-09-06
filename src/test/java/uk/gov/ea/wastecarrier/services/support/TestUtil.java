package uk.gov.ea.wastecarrier.services.support;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TestUtil {

    public static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public static Date fromDate(Date startDate, Integer years, Integer months, Integer days) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(startDate);
        calendar.add(Calendar.YEAR, years);
        calendar.add(Calendar.MONTH, months);
        calendar.add(Calendar.DATE, days);

        return calendar.getTime();
    }

    public static Date todayPlus(int numberToAdd, int period) {
        TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
        Calendar today = Calendar.getInstance(utcTimeZone);
        today.add(period, numberToAdd);

        return today.getTime();
    }

    public static Date fromCurrentDate(Integer years, Integer months, Integer days) {
        return fromDate(new Date(), years, months, days);
    }

    public static String dateToday() {
        return dateFormat.format(new Date());
    }
}
