package edu.ucsd.cse110.team4personalbest;

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
public class WeeklyStatsActivityUnitTest {
    private WeeklyStatsActivity weeklyStatsActivity;
    private Button button;

    @Before
    public void setUp() throws Exception {
        weeklyStatsActivity = Robolectric.buildActivity(WeeklyStatsActivity.class).create().get();
        button = weeklyStatsActivity.findViewById(R.id.backToHome);
    }

    @Test
    public void testActivityFinish() {
        button.performClick();
        assertTrue(weeklyStatsActivity.isFinishing());
    }

    @Test
    public void testStepStats() {
        SharedPreferences sharedPref = weeklyStatsActivity.getSharedPreferences("weekly_steps", weeklyStatsActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        // total steps for Sunday
        editor.putInt("1", 5000);
        // active steps for Sunday
        editor.putInt("8", 1500);
        editor.apply();

        weeklyStatsActivity = Robolectric.buildActivity(WeeklyStatsActivity.class).create().get();

        ArrayList<BarEntry> barEntries = weeklyStatsActivity.getBarEntries();
        assertEquals(1500f, barEntries.get(0).getVals()[0], 1e-5);
        assertEquals(3500f, barEntries.get(0).getVals()[1], 1e-5);
    }

    @Test
    public void testGoalLine() {
        SharedPreferences sharedPref = weeklyStatsActivity.getSharedPreferences("weekly_steps", weeklyStatsActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("goal", 5000);
        editor.apply();

        weeklyStatsActivity = Robolectric.buildActivity(WeeklyStatsActivity.class).create().get();

        LimitLine goalLine = weeklyStatsActivity.getGoalLine();
        assertEquals(5000f, goalLine.getLimit(), 1e-5);
    }

    @Test
    public void testBarData() {
        BarData barData = weeklyStatsActivity.getBarData();
        assertNotNull(barData);
        assertEquals("Sun", barData.getXVals().get(0));
        assertEquals("Mon", barData.getXVals().get(1));
        assertEquals("Tues", barData.getXVals().get(2));
        assertEquals(Color.rgb(204, 229, 255), barData.getColors()[0]);
    }
}
