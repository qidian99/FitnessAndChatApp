package edu.ucsd.cse110.googlefitapp;

import android.annotation.SuppressLint;
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

import edu.ucsd.cse110.googlefitapp.adapter.MonthlyStatsAdapter;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessService;
import edu.ucsd.cse110.googlefitapp.mock.StepCalendar;
import edu.ucsd.cse110.googlefitapp.observer.Observer;

import static edu.ucsd.cse110.googlefitapp.MainActivity.KEY_STRIDE;
import static edu.ucsd.cse110.googlefitapp.MainActivity.SHARED_PREFERENCE_NAME;

public class MonthlyStatsActivity extends Activity {

    public static final String TAG = "MONTHLY_STATS";
    private static final String ACTIVE_WALK_FMT = "Active walk distance: %.1f miles\nAverage speed: %.1f MPH";
    private static final String INCIDENTAL_WALK_FMT = "Incidental walk distance: %.1f miles \nfor a total of %.1f miles";
    private static final String STATS_FMT = "speed: %.1f, distance: %.1f -> active: %.1f + incidental: %.1f";
    private static final String DAILY_STATS_FMT = "day: %d: incidental steps(%d), active steps(%d)";
    private int goal;
    private ArrayList<BarEntry> barEntries;
    private LimitLine goalLine;
    private BarData barData;
    private FitnessService fitnessService;
    private int[] monthlyTotalSteps = new int[28];
    private int[] monthlyActiveSteps = new int[28];
    private float[] monthlyActiveSpeed = new float[28];
    private float[] monthlyActiveDistance = new float[28];
    private boolean inactiveStepRead = false;
    private boolean activeStepRead = false;
    private Calendar tempCal;
    private boolean esTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_stats);

        boolean test = getIntent().getBooleanExtra("testkey", false);
        esTest = getIntent().getBooleanExtra("TEST", false);


        if (!test || !esTest) {
            fitnessService = new MonthlyStatsAdapter(this);
            fitnessService.setup();
        }

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

            @SuppressLint("DefaultLocale")
            @Override
            public void onChartSingleTapped(MotionEvent me) {
                int index = barChart.getHighlightByTouchPoint(me.getX(), me.getY()).getXIndex();
                if(index >= 28){
                    Log.d(TAG, "Invalid index touched.");
                    return;
                }
                int yInd = barChart.getHighlightByTouchPoint(me.getX(), me.getY()).getStackIndex();

                float speed = monthlyActiveSpeed[index];
                float activeDist = monthlyActiveDistance[index];
                float totalDist = stepToDistance(monthlyTotalSteps[index]);
                float inciDist = totalDist - activeDist;

                if(inciDist < 0) {
                    inciDist = 0;
                }

                Log.d(TAG, String.format(STATS_FMT, speed , totalDist, activeDist, inciDist));

                if (yInd == 1) {
                    Toast.makeText(MonthlyStatsActivity.this, String.format(INCIDENTAL_WALK_FMT, inciDist, totalDist), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MonthlyStatsActivity.this,
                            String.format(ACTIVE_WALK_FMT, activeDist, speed),
                            Toast.LENGTH_SHORT).show();
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

        for (int i = 0; i < 28; i++) {
            int activeSteps = monthlyActiveSteps[i];

            int inactiveSteps = monthlyTotalSteps[i] - activeSteps;

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
        if(tempCal == null) {
            tempCal = StepCalendar.getInstance();
        }

        for( int i = 0; i < 28; i ++ ){
            days.add(0, getDayOfMonthString(tempCal));
            tempCal.add(Calendar.DATE, -1);
        }

        barData = new BarData(days, barDataSet);

        barChart.setData(barData);

        barChart.animateY(2000);

        goalLine = new LimitLine(goal);

        barChart.getAxisLeft().addLimitLine(goalLine);
        Log.d(TAG, String.format("goal line set success: %d", goal));
    }

    public void setTestGraph() {
        BarChart barChart = findViewById(R.id.barGraph);
        barEntries = new ArrayList<>();

        int max = 0;

        int goal = 5000;

        int[] totalArr = new int[28];
        totalArr[27] = 4000;
        totalArr[26] = 5000;
        totalArr[25] = 3000;
        totalArr[24] = 6000;
        totalArr[23] = 7000;

        int[] activeArr = new int[28];
        activeArr[27] = 1000;
        activeArr[26] = 3000;
        activeArr[25] = 2000;
        activeArr[24] = 1000;
        activeArr[23] = 5000;

        for (int i = 0; i < 28; i++) {
            int activeSteps = activeArr[i];

            int inactiveSteps = totalArr[i] - activeSteps;

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
        if(tempCal == null) {
            tempCal = StepCalendar.getInstance();
        }

        for( int i = 0; i < 28; i ++ ){
            days.add(0, getDayOfMonthString(tempCal));
            tempCal.add(Calendar.DATE, -1);
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

    public static String getDayOfMonthString(Calendar cal){
        return String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
    }


    @Override
    public void updateAll(int num) {

    }

    @Override
    public void setStep(int currentStep) {

    }

    @Override
    public int getGoal() {
        return goal;
    }

    @Override
    public float getStrideLength() {
        return 0;
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

    public int[] getMonthlyTotalSteps() {
        return monthlyTotalSteps;
    }

    public int[] getMonthlyActiveSteps() {
        return monthlyActiveSteps;
    }

    public float[] getMonthlyActiveSpeed() {
        return monthlyActiveSpeed;
    }

    public float[] getMonthlyActiveDistance() {
        return monthlyActiveDistance;
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

    public void setTempCal(Calendar cal) {
        tempCal = cal;
    }

    @SuppressLint("StaticFieldLeak")
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
            if (MonthlyStatsActivity.this.activeStepRead && MonthlyStatsActivity.this.inactiveStepRead) {
                Log.d(MonthlyStatsActivity.TAG, "Successfully populate monthly stats arrays.");
                findViewById(R.id.barGraph).setVisibility(View.VISIBLE);
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                if (esTest) {
                    setTestGraph();
                } else {
                    setGraph();
                }
                isCancelled = true;
                cancel(true);

            } else {
                Log.d(MonthlyStatsActivity.TAG, "Fetching monthly stats from server.");
                // Set animation, etc.
            }
        }
    }
}
