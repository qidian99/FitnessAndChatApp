package edu.ucsd.cse110.googlefitapp;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.widget.Button;

import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class WeeklyStatsUnitTest {
    private WeeklyStats weeklyStats;
    private Button button;

    @Before
    public void setUp() throws Exception {
        weeklyStats = Robolectric.buildActivity(WeeklyStats.class).create().get();
        button = weeklyStats.findViewById(R.id.backToHome);
    }

    @Test
    public void testActivityFinish() {
        button.performClick();
        assertTrue(weeklyStats.isFinishing());
    }

    @Test
    public void testStepStats() {
        SharedPreferences sharedPref = weeklyStats.getSharedPreferences("weekly_steps", weeklyStats.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        // total steps for Sunday
        editor.putInt("1", 5000);
        // active steps for Sunday
        editor.putInt("8", 1500);
        editor.apply();

        weeklyStats = Robolectric.buildActivity(WeeklyStats.class).create().get();

        ArrayList<BarEntry> barEntries = weeklyStats.getBarEntries();
        assertEquals(1500f, barEntries.get(0).getVals()[0], 1e-5);
        assertEquals(3500f, barEntries.get(0).getVals()[1], 1e-5);
    }

    @Test
    public void testGoalLine() {
        SharedPreferences sharedPref = weeklyStats.getSharedPreferences("weekly_steps", weeklyStats.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("goal", 5000);
        editor.apply();

        weeklyStats = Robolectric.buildActivity(WeeklyStats.class).create().get();

        LimitLine goalLine = weeklyStats.getGoalLine();
        assertEquals(5000f, goalLine.getLimit(), 1e-5);
    }

    @Test
    public void testBarData() {
        BarData barData = weeklyStats.getBarData();
        assertNotNull(barData);
        assertEquals("Sun", barData.getXVals().get(0));
        assertEquals("Mon", barData.getXVals().get(1));
        assertEquals("Tues", barData.getXVals().get(2));
        assertEquals(Color.rgb(204, 229, 255), barData.getColors()[0]);
    }
}
