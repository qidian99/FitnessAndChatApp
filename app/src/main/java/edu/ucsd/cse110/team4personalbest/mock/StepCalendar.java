package edu.ucsd.cse110.team4personalbest.mock;

import java.util.Calendar;

public class StepCalendar {
    public static int appYear = Calendar.getInstance().get(Calendar.YEAR);
    public static int appMonth = Calendar.getInstance().get(Calendar.MONTH);
    ;
    public static int appDate = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    ;

    public static Calendar getInstance() {
        Calendar tempCal = Calendar.getInstance();
        tempCal.set(appYear, appMonth, appDate);
        return tempCal;
    }

    public static void set(int year, int month, int dayOfMonth) {
        appYear = year;
        appMonth = month;
        appDate = dayOfMonth;
    }
}
