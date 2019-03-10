package edu.ucsd.cse110.googlefitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;

import java.util.ArrayList;
import java.util.Calendar;

import edu.ucsd.cse110.googlefitapp.adapter.WeeklyStatsAdapter;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessService;
import edu.ucsd.cse110.googlefitapp.mock.StepCalendar;
import edu.ucsd.cse110.googlefitapp.observer.Observer;

import static edu.ucsd.cse110.googlefitapp.MainActivity.KEY_STRIDE;
import static edu.ucsd.cse110.googlefitapp.MainActivity.SHARED_PREFERENCE_NAME;

public class WeeklyStatsActivity extends Activity {

    public static final String TAG = "WEEKLY_STATS";
    private static final String ACTIVE_WALK_FMT = "Active walk distance: %.1f miles\nAverage speed: %.1f MPH";
    private static final String INCIDENTAL_WALK_FMT = "Incidental walk distance: %.1f miles \nfor a total of %.1f miles";
    private static final String STATS_FMT = "speed: %.1f, distance: %.1f -> active: %.1f + incidental: %.1f";
    private static final String DAILY_STATS_FMT = "day: %d: incidental steps(%d), active steps(%d)";
    private int goal;
    private ArrayList<BarEntry> barEntries;
    private LimitLine goalLine;
    private BarData barData;
    private FitnessService fitnessService;
    private int[] weeklyTotalSteps = new int[7];
    private int[] weeklyActiveSteps = new int[7];
    private float[] weeklyActiveSpeed = new float[7];
    private float[] weeklyActiveDistance = new float[7];
    private boolean inactiveStepRead = false;
    private boolean activeStepRead = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_stats);

        boolean test = getIntent().getBooleanExtra("testkey", false);

        if (!test) {
            fitnessService = new WeeklyStatsAdapter(this);
            fitnessService.setup();
        }

        SharedPreferences stepPref = getSharedPreferences("weekly_steps", MODE_PRIVATE);
        SharedPreferences sharedPref = getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE);

        goal = sharedPref.getInt("goal", MainActivity.DEFAULT_GOAL);

        final BarChart barChart;

        barChart = findViewById(R.id.barGraph);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelsToSkip(0);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);

        barChart.getAxisLeft().setAxisMinValue(0);
        barChart.getAxisRight().setAxisMinValue(0);

        barChart.setDescription("");
        barChart.setAutoScaleMinMaxEnabled(true);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setScaleEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setTouchEnabled(true);
        barChart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
            }

            @Override
            public void onChartLongPressed(MotionEvent me) {
            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {
            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {
                int index = barChart.getHighlightByTouchPoint(me.getX(), me.getY()).getXIndex();
                if(index >= 7){
                    Log.d(TAG, "Invalid index touched.");
                    return;
                }
                int yInd = barChart.getHighlightByTouchPoint(me.getX(), me.getY()).getStackIndex();

                float speed = weeklyActiveSpeed[index];
                float activeDist = weeklyActiveDistance[index];
                float totalDist = stepToDistance(weeklyTotalSteps[index]);
                float inciDist = totalDist - activeDist;

                if(inciDist < 0) {
                    inciDist = 0;
                }

                Log.d(TAG, String.format(STATS_FMT, speed , totalDist, activeDist, inciDist));

                if (yInd == 1) {
                    Toast.makeText(WeeklyStatsActivity.this, String.format(INCIDENTAL_WALK_FMT, inciDist, totalDist), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(WeeklyStatsActivity.this,
                            String.format(ACTIVE_WALK_FMT, activeDist, speed),
                            Toast.LENGTH_LONG).show();
                }
            }

            private float stepToDistance(int steps) {
                SharedPreferences sharedPref = getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE);
                float strideLength = sharedPref.getFloat(KEY_STRIDE, 0);
                return steps * strideLength / 63360.0f;
            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {
            }
        });

//        setGraph();
        if(!test) {
            new setGraphAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(500));
        }

        Button button = findViewById(R.id.backToHome);
        button.setOnClickListener(view -> finish());
    }

    private float stepToDistance(int steps) {
        SharedPreferences sharedPref = getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        float strideLength = sharedPref.getFloat(KEY_STRIDE, 0);
        return steps * strideLength / 63360.0f;
    }

    public void setGraph() {
        BarChart barChart = findViewById(R.id.barGraph);
        barEntries = new ArrayList<>();

        int max = 0;

        for (int i = 0; i < 7; i++) {
            int activeSteps = weeklyActiveSteps[i];

            int inactiveSteps = weeklyTotalSteps[i] - activeSteps;

            if(inactiveSteps < 0) {
                inactiveSteps = 0;
            }

            if (inactiveSteps + activeSteps > max) {
                max = inactiveSteps + activeSteps;
            }
            Log.d(TAG, String.format(DAILY_STATS_FMT, i, inactiveSteps, activeSteps));

            barEntries.add(new BarEntry(new float[]{activeSteps, inactiveSteps}, i));
        }

        if (max < goal) {
            barChart.getAxisLeft().setAxisMaxValue(goal + 300);
            barChart.getAxisRight().setAxisMaxValue(goal + 300);
        } else {
            barChart.getAxisLeft().setAxisMaxValue(max + 300);
            barChart.getAxisRight().setAxisMaxValue(max + 300);
        }

        Log.d(TAG, String.format("graph maximum set success: %.1f", barChart.getAxisLeft().getAxisMaximum()));

        BarDataSet barDataSet = new BarDataSet(barEntries, "");
        barDataSet.setStackLabels(new String[]{"Active steps", "Incidental steps"});
        barDataSet.setColors(new int[]{Color.rgb(204, 229, 255), Color.rgb(255, 204, 204)});

        ArrayList<String> days = new ArrayList<>();
        Calendar tempCal = StepCalendar.getInstance();
        for( int i = 0; i < 7; i ++ ){
            tempCal.add(Calendar.DATE, 1);
            days.add(getDayOfWeekString(tempCal));
        }

        barData = new BarData(days, barDataSet);

        barChart.setData(barData);

        barChart.animateY(2000);

        goalLine = new LimitLine(goal);

        barChart.getAxisLeft().addLimitLine(goalLine);
        Log.d(TAG, String.format("goal line set success: %d", goal));
    }

    public ArrayList<BarEntry> getBarEntries() {
        return barEntries;
    }

    public LimitLine getGoalLine() {
        return goalLine;
    }

    public BarData getBarData() {
        return barData;
    }

    public static String getDayOfWeekString(Calendar cal){
        switch(cal.get(Calendar.DAY_OF_WEEK)){
            case 1:
                return "Sun";
            case 2:
                return "Mon";
            case 3:
                return "Tues";
            case 4:
                return "Wed";
            case 5:
                return "Thurs";
            case 6:
                return "Fri";
            case 7:
                return "Sat";
        }
        return null;
    }


    @Override
    public void updateAll(int num) {

    }

    @Override
    public void setStep(int currentStep) {

    }

    @Override
    public void registerObserver(Observer o) {

    }

    @Override
    public void removeObserver(Observer o) {

    }

    @Override
    public void notifyObservers() {

    }

    public int[] getWeeklyTotalSteps() {
        return weeklyTotalSteps;
    }

    public int[] getWeeklyActiveSteps() {
        return weeklyActiveSteps;
    }

    public float[] getWeeklyActiveSpeed() {
        return weeklyActiveSpeed;
    }

    public float[] getWeeklyActiveDistance() {
        return weeklyActiveDistance;
    }

    public void setInActiveStepRead(boolean b) {
        this.inactiveStepRead = b;
    }

    public void setActiveStepRead(boolean b) {
        this.activeStepRead = b;
    }

    public void updateGoal() {
        SharedPreferences sharedPref = getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        goal = sharedPref.getInt("goal", MainActivity.DEFAULT_GOAL);
    }

    private class setGraphAsyncTask extends AsyncTask<String, String, Void> {

        private boolean isCancelled = false;

        @Override
        protected Void doInBackground(String... sleepTime) {
            while (!isCancelled) {
                try {
                    Thread.sleep(Integer.valueOf(sleepTime[0]));
                    publishProgress();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(String... text) {
            if (WeeklyStatsActivity.this.activeStepRead && WeeklyStatsActivity.this.inactiveStepRead) {
                Log.d(WeeklyStatsActivity.TAG, "Sucessfully populate weekly stats arrays.");
                findViewById(R.id.barGraph).setVisibility(View.VISIBLE);
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                setGraph();
                isCancelled = true;
                cancel(true);

            } else {
                Log.d(WeeklyStatsActivity.TAG, "Fetching weekly stats from server.");
                // Set animation, etc.
            }
        }
    }
}
