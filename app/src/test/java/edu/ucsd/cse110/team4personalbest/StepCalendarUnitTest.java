package edu.ucsd.cse110.team4personalbest;

import org.junit.Test;

import java.util.Calendar;

import edu.ucsd.cse110.team4personalbest.mock.StepCalendar;

import static org.junit.Assert.assertEquals;

public class StepCalendarUnitTest {
    @Test
    public void testGetInstance() {
        Calendar calendar = StepCalendar.getInstance();
        assertEquals(calendar.get(Calendar.YEAR), StepCalendar.appYear);
        assertEquals(calendar.get(Calendar.MONTH), StepCalendar.appMonth);
        assertEquals(calendar.get(Calendar.DAY_OF_MONTH), StepCalendar.appDate);
    }

    @Test
    public void testSet() {
        StepCalendar.set(2019, 2, 20);
        assertEquals(2019, StepCalendar.appYear);
        assertEquals(2, StepCalendar.appMonth);
        assertEquals(20, StepCalendar.appDate);
    }
}
