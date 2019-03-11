package edu.ucsd.cse110.googlefitapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.animation.ScaleAnimation;
import android.widget.Button;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowToast;

import java.util.ArrayList;
import java.util.Calendar;

import edu.ucsd.cse110.googlefitapp.mock.StepCalendar;

import static android.content.Context.MODE_PRIVATE;
import static edu.ucsd.cse110.googlefitapp.MainActivity.KEY_STRIDE;
import static edu.ucsd.cse110.googlefitapp.MainActivity.SHARED_PREFERENCE_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class MonthlyStatsActivityUnitTest {
    private MonthlyStatsActivity monthlyStatsActivity;
    private Button button;
    private BarChart barChart;
    private Calendar myCalander;

    @Before
    public void setUp() throws Exception {
        StepCalendar.set(2019, 2, 8);
        myCalander = StepCalendar.getInstance();
        Intent intent = new Intent(RuntimeEnvironment.application, WeeklyStatsActivity.class);
        intent.putExtra("testkey", true);
        monthlyStatsActivity = Robolectric.buildActivity(MonthlyStatsActivity.class, intent).create().get();
        monthlyStatsActivity.setTempCal(myCalander);
        button = monthlyStatsActivity.findViewById(R.id.backToHome);
        barChart = monthlyStatsActivity.findViewById(R.id.barGraph);
    }

    @Test
    public void testActiveStats() {
        monthlyStatsActivity.getMonthlyActiveSteps()[0] = 10000;
        monthlyStatsActivity.getMonthlyActiveSteps()[1] = 10000;
        monthlyStatsActivity.getMonthlyActiveSteps()[2] = 10000;
        monthlyStatsActivity.setGraph();

        ArrayList<BarEntry> barEntries = monthlyStatsActivity.getBarEntries();
        // test set values
        assertEquals(10000, barEntries.get(0).getVals()[0], 1e-5);
        assertEquals(10000, barEntries.get(1).getVals()[0], 1e-5);
        assertEquals(10000, barEntries.get(2).getVals()[0], 1e-5);

        // test unset values
        assertEquals(0, barEntries.get(3).getVals()[0], 1e-5);
        assertEquals(0, barEntries.get(3).getVals()[0], 1e-5);
    }

    @Test
    public void testIncidentalStats() {
        // all incidental
        monthlyStatsActivity.getMonthlyTotalSteps()[0] = 10000;
        monthlyStatsActivity.getMonthlyTotalSteps()[1] = 10000;
        monthlyStatsActivity.getMonthlyTotalSteps()[2] = 10000;
        monthlyStatsActivity.setGraph();

        ArrayList<BarEntry> barEntries = monthlyStatsActivity.getBarEntries();
        // test set values
        assertEquals(10000, barEntries.get(0).getVals()[1], 1e-5);
        assertEquals(10000, barEntries.get(1).getVals()[1], 1e-5);
        assertEquals(10000, barEntries.get(2).getVals()[1], 1e-5);

        // test unset values
        assertEquals(0, barEntries.get(3).getVals()[1], 1e-5);
        assertEquals(0, barEntries.get(3).getVals()[1], 1e-5);

        // add some active steps
        monthlyStatsActivity.getMonthlyActiveSteps()[0] = 5000;
        monthlyStatsActivity.getMonthlyActiveSteps()[1] = 2000;
        monthlyStatsActivity.getMonthlyActiveSteps()[2] = 1000;
        monthlyStatsActivity.setGraph();

        barEntries = monthlyStatsActivity.getBarEntries();
        // test incidental values
        assertEquals(5000, barEntries.get(0).getVals()[1], 1e-5);
        assertEquals(8000, barEntries.get(1).getVals()[1], 1e-5);
        assertEquals(9000, barEntries.get(2).getVals()[1], 1e-5);

        // other unset values unchanged
        assertEquals(0, barEntries.get(3).getVals()[1], 1e-5);
        assertEquals(0, barEntries.get(3).getVals()[1], 1e-5);
    }

    @Test
    public void testBothToasts() {
        String activeToast = "Active walk distance: %.1f miles\nAverage speed: %.1f MPH";
        String inciToast = "Incidental walk distance: %.1f miles \nfor a total of %.1f miles";

        // set distance
        monthlyStatsActivity.getMonthlyActiveDistance()[1] = 2f;
        monthlyStatsActivity.getMonthlyActiveDistance()[2] = 4f;
        monthlyStatsActivity.getMonthlyActiveDistance()[3] = 5f;
        // set speed
        monthlyStatsActivity.getMonthlyActiveSpeed()[1] = 11.5f;
        monthlyStatsActivity.getMonthlyActiveSpeed()[2] = 12.3f;
        monthlyStatsActivity.getMonthlyActiveSpeed()[3] = 20.7f;
        // only active steps
        monthlyStatsActivity.getMonthlyActiveSteps()[1] = 1000;
        monthlyStatsActivity.getMonthlyActiveSteps()[2] = 300;
        monthlyStatsActivity.getMonthlyActiveSteps()[3] = 400;
        monthlyStatsActivity.setGraph();

        ArrayList<BarEntry> barEntries = monthlyStatsActivity.getBarEntries();

        // make sure that active steps set are correctly displayed
        assertEquals(1000, barEntries.get(1).getVals()[0], 1e-5);
        assertEquals(300, barEntries.get(2).getVals()[0], 1e-5);
        assertEquals(400, barEntries.get(3).getVals()[0], 1e-5);

        // check the first bar
        MotionEvent motionEvent = MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN,
                13f,
                -15f,
                0
        );
        barChart.getOnChartGestureListener().onChartSingleTapped(motionEvent);
        assertEquals(String.format(activeToast, 2f, 11.5f), ShadowToast.getTextOfLatestToast());

        // check the second bar
        motionEvent = MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN,
                12f,
                -15f,
                0
        );
        barChart.getOnChartGestureListener().onChartSingleTapped(motionEvent);
        assertEquals(String.format(activeToast, 4f, 12.3f), ShadowToast.getTextOfLatestToast());

        // check the third bar
        motionEvent = MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN,
                11f,
                -15f,
                0
        );
        barChart.getOnChartGestureListener().onChartSingleTapped(motionEvent);
        assertEquals(String.format(activeToast, 5f, 20.7f), ShadowToast.getTextOfLatestToast());

        // if tap is not on a bar, then toast should be the same
        motionEvent = MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN,
                15f,
                -20f,
                0
        );
        barChart.getOnChartGestureListener().onChartSingleTapped(motionEvent);
        assertEquals(String.format(activeToast, 0f, 0.0f), ShadowToast.getTextOfLatestToast());

        // add some incidental steps
        monthlyStatsActivity.getMonthlyTotalSteps()[1] = 2000;
        monthlyStatsActivity.getMonthlyTotalSteps()[2] = 500;
        monthlyStatsActivity.getMonthlyTotalSteps()[3] = 700;
        monthlyStatsActivity.setGraph();

        barEntries = monthlyStatsActivity.getBarEntries();

        // make sure that incidental steps set are correctly displayed
        assertEquals(1000, barEntries.get(1).getVals()[1], 1e-5);
        assertEquals(200, barEntries.get(2).getVals()[1], 1e-5);
        assertEquals(300, barEntries.get(3).getVals()[1], 1e-5);

        // make sure toasts are correctly displayed with incidental steps added
        motionEvent = MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN,
                13f,
                0f,
                0
        );
        barChart.getOnChartGestureListener().onChartSingleTapped(motionEvent);

        float dist = stepToDist(1000);
        assertEquals(String.format(inciToast, dist, dist), ShadowToast.getTextOfLatestToast());

        motionEvent = MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN,
                12f,
                0f,
                0
        );

        dist = stepToDist(200);
        barChart.getOnChartGestureListener().onChartSingleTapped(motionEvent);
        assertEquals(String.format(inciToast, dist, dist), ShadowToast.getTextOfLatestToast());

        motionEvent = MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN,
                11f,
                0f,
                0
        );

        dist = stepToDist(300);
        barChart.getOnChartGestureListener().onChartSingleTapped(motionEvent);
        assertEquals(String.format(inciToast, dist, dist), ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void testIncidentalToastOnly() {
        SharedPreferences sharedPref = monthlyStatsActivity.getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putFloat(KEY_STRIDE, 24f);
        editor.apply();

        String toast = "Incidental walk distance: %.1f miles \nfor a total of %.1f miles";
        // set steps
        monthlyStatsActivity.getMonthlyTotalSteps()[0] = 2000;
        monthlyStatsActivity.getMonthlyTotalSteps()[1] = 300;
        monthlyStatsActivity.setGraph();

        MotionEvent motionEvent = MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN,
                14f,
                0f,
                0
        );
        barChart.getOnChartGestureListener().onChartSingleTapped(motionEvent);

        float dist = stepToDist(2000);
        assertEquals(String.format(toast, dist, dist), ShadowToast.getTextOfLatestToast());

        motionEvent = MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN,
                13f,
                0f,
                0
        );

        dist = stepToDist(300);
        barChart.getOnChartGestureListener().onChartSingleTapped(motionEvent);
        assertEquals(String.format(toast, dist, dist), ShadowToast.getTextOfLatestToast());
    }

    private float stepToDist(int steps) {
        SharedPreferences sharedPref = monthlyStatsActivity.getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        float strideLength = sharedPref.getFloat(KEY_STRIDE, 0);
        return steps * strideLength / 63360.0f;
    }

    @Test
    public void testGoalLine() {
        // test default goal
        monthlyStatsActivity.setGraph();
        LimitLine goalLine = monthlyStatsActivity.getGoalLine();
        assertEquals(5000f, goalLine.getLimit(), 1e-5);

        // test update goal
        SharedPreferences sharedPref = monthlyStatsActivity.getSharedPreferences("user_data", monthlyStatsActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("goal", 5550);
        editor.apply();
        monthlyStatsActivity.updateGoal();
        monthlyStatsActivity.setGraph();

        goalLine = monthlyStatsActivity.getGoalLine();
        assertEquals(5550f, goalLine.getLimit(), 1e-5);

        editor.putInt("goal", 5555);
        editor.apply();
        monthlyStatsActivity.updateGoal();
        monthlyStatsActivity.setGraph();

        goalLine = monthlyStatsActivity.getGoalLine();
        assertEquals(5555f, goalLine.getLimit(), 1e-5);
    }

    @Test
    public void testBarData() {
        monthlyStatsActivity.setGraph();

        BarData barData = monthlyStatsActivity.getBarData();
        assertNotNull(barData);

        assertEquals("8", barData.getXVals().get(27));
        assertEquals("7", barData.getXVals().get(26));
        assertEquals("6", barData.getXVals().get(25));
        assertEquals("28", barData.getXVals().get(19));
        assertEquals("27", barData.getXVals().get(18));
        assertEquals("26", barData.getXVals().get(17));
        assertEquals("11", barData.getXVals().get(2));
        assertEquals("10", barData.getXVals().get(1));
        assertEquals("9", barData.getXVals().get(0));

        assertEquals(Color.rgb(204, 229, 255), barData.getColors()[0]);
        assertEquals(Color.rgb(255, 204, 204), barData.getColors()[1]);
    }

    @Test
    public void testActivityFinish() {
        button.performClick();
        assertTrue(monthlyStatsActivity.isFinishing());
    }
}
