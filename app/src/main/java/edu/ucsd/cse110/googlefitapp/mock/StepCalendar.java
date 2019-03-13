package edu.ucsd.cse110.googlefitapp.mock;

import java.util.Calendar;
import java.util.TimeZone;

public class StepCalendar {
    public static int appYear = Calendar.getInstance().get(Calendar.YEAR);
    public static int appMonth = Calendar.getInstance().get(Calendar.MONTH);
    public static int appDate = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

    public static Calendar getInstance() {
        Calendar tempCal = Calendar.getInstance();
        tempCal.set(appYear, appMonth, appDate);
        tempCal.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        return tempCal;
    }

    public static void set(int year, int month, int dayOfMonth) {
        appYear = year;
        appMonth = month;
        appDate = dayOfMonth;
    }
}
