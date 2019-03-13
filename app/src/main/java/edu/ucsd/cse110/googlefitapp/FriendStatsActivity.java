package edu.ucsd.cse110.googlefitapp;

import android.annotation.SuppressLint;
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

import edu.ucsd.cse110.googlefitapp.adapter.FriendStatsAdapter;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessService;
import edu.ucsd.cse110.googlefitapp.mock.StepCalendar;
import edu.ucsd.cse110.googlefitapp.observer.Observer;

public class FriendStatsActivity extends Activity {

    public static final String TAG = "MONTHLY_STATS";
    private ArrayList<BarEntry> barEntries;
    private LimitLine goalLine;
    private BarData barData;
    private FitnessService fitnessService;
    private int[] monthlyTotalSteps = new int[28];
    private int friendsGoal = 5000;
    private float friendStrideLength;
    private int[] monthlyActiveSteps = new int[28];
    private float[] monthlyActiveSpeed = new float[28];
    private float[] monthlyActiveDistance = new float[28];
    private boolean[] inactiveStepRead = new boolean[30];
    private boolean[] activeStepRead = new boolean[30];
    private Calendar tempCal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_stats);

        boolean test = getIntent().getBooleanExtra("testkey", false);
        String friendEmail = getIntent().getStringExtra("friendEmail");

        if(!test) {
            fitnessService = new FriendStatsAdapter(this, friendEmail);
            fitnessService.setup();
        }

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
                int totalStep = monthlyTotalSteps[index];
                float totalDist =  totalStep * friendStrideLength / 63360.0f;
                float inciDist = totalDist - activeDist;

                if(inciDist < 0) {
                    inciDist = 0;
                }

                Log.d(TAG, String.format("speed: %.1f, distance: %.1f -> active: %.1f + incidental: %.1f", speed , totalDist, activeDist, inciDist));

                if (yInd == 1) {
                    Toast.makeText(FriendStatsActivity.this, String.format("Incidental walk distance: %.1f miles \nfor a total of %.1f miles", inciDist, totalDist), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FriendStatsActivity.this,
                            String.format("Active walk distance: %.1f miles\nAverage speed: %.1f MPH", activeDist, speed),
                            Toast.LENGTH_SHORT).show();
                }
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

        if(!test) {
            new setGraphAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(500));
        }


        Button button = findViewById(R.id.backToHome);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void setGraph() {
        BarChart barChart = findViewById(R.id.barGraph);
        barEntries = new ArrayList<>();

        ArrayList<BarEntry> tmp = new ArrayList<>();

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
            Log.d(TAG, String.format("day: %d: incidental steps(%d), active steps(%d)", i, inactiveSteps, activeSteps));

            if(i < 4) {
                tmp.add(new BarEntry(new float[]{activeSteps, inactiveSteps}, i));
            }
            barEntries.add(new BarEntry(new float[]{activeSteps, inactiveSteps}, i));
        }

        if (max < friendsGoal) {
            barChart.getAxisLeft().setAxisMaxValue(friendsGoal + 300);
            barChart.getAxisRight().setAxisMaxValue(friendsGoal + 300);
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

        goalLine = new LimitLine(friendsGoal);
        barChart.getAxisLeft().addLimitLine(goalLine);
        Log.d(TAG, String.format("goal line set success: %d", friendsGoal));
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
        return 0;
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

    public void setFriendGoal(int goal) {
        this.friendsGoal = goal;
    }

    public void setFriendStrideLength(float length) {
        this.friendStrideLength = length;
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

    public void setInActiveStepRead(int index, boolean b) {
        this.inactiveStepRead[index] = b;
    }

    public void setActiveStepRead(int index, boolean b) {
        this.activeStepRead[index] = b;
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
            boolean activeSetUp = true;
            boolean inactiveSetUp = true;
            for(int i = 0; i < 28; i ++) {
                if(!FriendStatsActivity.this.activeStepRead[i]) {
                    activeSetUp = false;
                    break;
                }
                if(!FriendStatsActivity.this.inactiveStepRead[i]) {
                    inactiveSetUp = false;
                    break;
                }

            }
            if (activeSetUp && inactiveSetUp) {
                Log.d(FriendStatsActivity.TAG, "Sucessfully populate monthly stats arrays.");
                findViewById(R.id.barGraph).setVisibility(View.VISIBLE);
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                setGraph();
                isCancelled = true;
                cancel(true);
            } else {
                Log.d(FriendStatsActivity.TAG, "Fetching monthly stats from server.");
                // Set animation, etc.
            }
        }
    }
}
