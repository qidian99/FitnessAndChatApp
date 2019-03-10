package edu.ucsd.cse110.googlefitapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.SystemClock;
import android.view.MotionEvent;
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

import static android.content.Context.MODE_PRIVATE;
import static edu.ucsd.cse110.googlefitapp.MainActivity.KEY_STRIDE;
import static edu.ucsd.cse110.googlefitapp.MainActivity.SHARED_PREFERENCE_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class WeeklyStatsActivityUnitTest {
    private WeeklyStatsActivity weeklyStatsActivity;
    private Button button;
    private BarChart barChart;

    @Before
    public void setUp() throws Exception {
        Intent intent = new Intent(RuntimeEnvironment.application, WeeklyStatsActivity.class);
        intent.putExtra("testkey", true);
        weeklyStatsActivity = Robolectric.buildActivity(WeeklyStatsActivity.class, intent).create().get();
        button = weeklyStatsActivity.findViewById(R.id.backToHome);
        barChart = weeklyStatsActivity.findViewById(R.id.barGraph);
    }

    @Test
    public void testActiveStats() {
        weeklyStatsActivity.getWeeklyActiveSteps()[0] = 10000;
        weeklyStatsActivity.getWeeklyActiveSteps()[1] = 10000;
        weeklyStatsActivity.getWeeklyActiveSteps()[2] = 10000;
        weeklyStatsActivity.setGraph();

        ArrayList<BarEntry> barEntries = weeklyStatsActivity.getBarEntries();
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
        weeklyStatsActivity.getWeeklyTotalSteps()[0] = 10000;
        weeklyStatsActivity.getWeeklyTotalSteps()[1] = 10000;
        weeklyStatsActivity.getWeeklyTotalSteps()[2] = 10000;
        weeklyStatsActivity.setGraph();

        ArrayList<BarEntry> barEntries = weeklyStatsActivity.getBarEntries();
        // test set values
        assertEquals(10000, barEntries.get(0).getVals()[1], 1e-5);
        assertEquals(10000, barEntries.get(1).getVals()[1], 1e-5);
        assertEquals(10000, barEntries.get(2).getVals()[1], 1e-5);

        // test unset values
        assertEquals(0, barEntries.get(3).getVals()[1], 1e-5);
        assertEquals(0, barEntries.get(3).getVals()[1], 1e-5);

        // add some active steps
        weeklyStatsActivity.getWeeklyActiveSteps()[0] = 5000;
        weeklyStatsActivity.getWeeklyActiveSteps()[1] = 2000;
        weeklyStatsActivity.getWeeklyActiveSteps()[2] = 1000;
        weeklyStatsActivity.setGraph();

        barEntries = weeklyStatsActivity.getBarEntries();
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
        weeklyStatsActivity.getWeeklyActiveDistance()[1] = 2f;
        weeklyStatsActivity.getWeeklyActiveDistance()[2] = 4f;
        weeklyStatsActivity.getWeeklyActiveDistance()[3] = 5f;
        // set speed
        weeklyStatsActivity.getWeeklyActiveSpeed()[1] = 11.5f;
        weeklyStatsActivity.getWeeklyActiveSpeed()[2] = 12.3f;
        weeklyStatsActivity.getWeeklyActiveSpeed()[3] = 20.7f;
        // only active steps
        weeklyStatsActivity.getWeeklyActiveSteps()[1] = 1000;
        weeklyStatsActivity.getWeeklyActiveSteps()[2] = 300;
        weeklyStatsActivity.getWeeklyActiveSteps()[3] = 400;
        weeklyStatsActivity.setGraph();

        ArrayList<BarEntry> barEntries = weeklyStatsActivity.getBarEntries();

        // make sure that active steps set are correctly displayed
        assertEquals(1000, barEntries.get(1).getVals()[0], 1e-5);
        assertEquals(300, barEntries.get(2).getVals()[0], 1e-5);
        assertEquals(400, barEntries.get(3).getVals()[0], 1e-5);

//        for(int i = 0; i < 3; i++) {
//            long downTime = SystemClock.uptimeMillis();
//            long eventTime = SystemClock.uptimeMillis();
//            float x = 8f - (float)(i * 4);
//            float y = -15f;
//            int metaState = 0;
//            MotionEvent motionEvent = MotionEvent.obtain(
//                    downTime,
//                    eventTime,
//                    MotionEvent.ACTION_DOWN,
//                    x,
//                    y,
//                    metaState
//            );
//            // barChart.setOnChartGestureListener(new OnChartGestureListener()
//            barChart = weeklyStatsActivity.findViewById(R.id.barGraph);
//            barChart.getOnChartGestureListener().onChartSingleTapped(motionEvent);
//            assertEquals(String.format(toast, (float)(i+1), (float)(10*(i+1))), ShadowToast.getTextOfLatestToast());
//        }

        // check the first bar
        MotionEvent motionEvent = MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN,
                8f,
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
                4f,
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
                0f,
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
                0f,
                -15f,
                0
        );
        barChart.getOnChartGestureListener().onChartSingleTapped(motionEvent);
        assertEquals(String.format(activeToast, 5f, 20.7f), ShadowToast.getTextOfLatestToast());

        // add some incidental steps
        weeklyStatsActivity.getWeeklyTotalSteps()[1] = 2000;
        weeklyStatsActivity.getWeeklyTotalSteps()[2] = 500;
        weeklyStatsActivity.getWeeklyTotalSteps()[3] = 700;
        weeklyStatsActivity.setGraph();

        barEntries = weeklyStatsActivity.getBarEntries();

        // make sure that incidental steps set are correctly displayed
        assertEquals(1000, barEntries.get(1).getVals()[1], 1e-5);
        assertEquals(200, barEntries.get(2).getVals()[1], 1e-5);
        assertEquals(300, barEntries.get(3).getVals()[1], 1e-5);

        // make sure toasts are correctly displayed with incidental steps added
        motionEvent = MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN,
                8f,
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
                4f,
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
                0f,
                0f,
                0
        );

        dist = stepToDist(300);
        barChart.getOnChartGestureListener().onChartSingleTapped(motionEvent);
        assertEquals(String.format(inciToast, dist, dist), ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void testIncidentalToastOnly() {
        SharedPreferences sharedPref = weeklyStatsActivity.getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putFloat(KEY_STRIDE, 24f);
        editor.apply();

        String toast = "Incidental walk distance: %.1f miles \nfor a total of %.1f miles";
        // set steps
        weeklyStatsActivity.getWeeklyTotalSteps()[0] = 2000;
        weeklyStatsActivity.getWeeklyTotalSteps()[1] = 300;
        weeklyStatsActivity.setGraph();

        MotionEvent motionEvent = MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN,
                12f,
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
                8f,
                0f,
                0
        );

        dist = stepToDist(300);
        barChart.getOnChartGestureListener().onChartSingleTapped(motionEvent);
        assertEquals(String.format(toast, dist, dist), ShadowToast.getTextOfLatestToast());
    }

    private float stepToDist(int steps) {
        SharedPreferences sharedPref = weeklyStatsActivity.getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        float strideLength = sharedPref.getFloat(KEY_STRIDE, 0);
        return steps * strideLength / 63360.0f;
    }

    @Test
    public void testGoalLine() {
        // test default goal
        weeklyStatsActivity.setGraph();
        LimitLine goalLine = weeklyStatsActivity.getGoalLine();
        assertEquals(5000f, goalLine.getLimit(), 1e-5);

        // test update goal
        SharedPreferences sharedPref = weeklyStatsActivity.getSharedPreferences("user_data", weeklyStatsActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("goal", 5550);
        editor.apply();
        weeklyStatsActivity.updateGoal();
        weeklyStatsActivity.setGraph();

        goalLine = weeklyStatsActivity.getGoalLine();
        assertEquals(5550f, goalLine.getLimit(), 1e-5);

        editor.putInt("goal", 5555);
        editor.apply();
        weeklyStatsActivity.updateGoal();
        weeklyStatsActivity.setGraph();

        goalLine = weeklyStatsActivity.getGoalLine();
        assertEquals(5555f, goalLine.getLimit(), 1e-5);
    }

    @Test
    public void testBarData() {
        weeklyStatsActivity.setGraph();
        BarData barData = weeklyStatsActivity.getBarData();
        assertNotNull(barData);
        assertEquals("Sun", barData.getXVals().get(0));
        assertEquals("Mon", barData.getXVals().get(1));
        assertEquals("Tues", barData.getXVals().get(2));
        assertEquals(Color.rgb(204, 229, 255), barData.getColors()[0]);
        assertEquals(Color.rgb(255, 204, 204), barData.getColors()[1]);
    }

    @Test
    public void testActivityFinish() {
        button.performClick();
        assertTrue(weeklyStatsActivity.isFinishing());
    }
}
