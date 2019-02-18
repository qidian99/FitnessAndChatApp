package edu.ucsd.cse110.googlefitapp;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

public class WeeklyStats extends AppCompatActivity {

    private int goal;
    public static final String TAG = "WEEKLY_STATS";
    private ArrayList<BarEntry> barEntries;
    private LimitLine goalLine;
    private BarData barData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_stats);

        SharedPreferences sharedPref = getSharedPreferences("weekly_steps", MODE_PRIVATE);
        goal = sharedPref.getInt("goal", 0);

        final BarChart barChart;

        barChart = (BarChart) findViewById(R.id.barGraph);
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
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {}

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) { }

            @Override
            public void onChartLongPressed(MotionEvent me) { }

            @Override
            public void onChartDoubleTapped(MotionEvent me) { }

            @Override
            public void onChartSingleTapped(MotionEvent me) {
                int index = barChart.getHighlightByTouchPoint(me.getX(), me.getY()).getXIndex();
                int yInd = barChart.getHighlightByTouchPoint(me.getX(), me.getY()).getStackIndex();

                SharedPreferences statsPref = getSharedPreferences("weekly_data", MODE_PRIVATE);
                float speed = statsPref.getFloat(String.valueOf(index + 1), 0.0f);
                float distance = statsPref.getFloat(String.valueOf(index + 8), 0.0f);
                float activeDist = statsPref.getFloat(String.valueOf(index + 15), 0.0f);
                float inciDist = distance - activeDist;

                Log.d(TAG, String.format("speed: %.1f, distance: %.1f -> active: %.1f + incidental: %.1f", speed, distance, activeDist, inciDist));

                if(yInd == 1) {
                    Toast.makeText(WeeklyStats.this, String.format("Incidental walk distance: %.1f miles \nfor a total of %.1f miles", inciDist, distance), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(WeeklyStats.this,
                            String.format("Active walk distance: %.1f miles\nAverage speed: %.1f MPH", activeDist, speed),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) { }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) { }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) { }
        });

        barEntries = new ArrayList<BarEntry>();

        int max = 0;

        for(int i = 1; i <= 7; i++) {
            int activeSteps = sharedPref.getInt(String.valueOf(i + 7), 0);
            int inactiveSteps = sharedPref.getInt(String.valueOf(i), 0) - activeSteps;

            if (inactiveSteps + activeSteps > max) {
                max = inactiveSteps + activeSteps;
            }
            Log.d(TAG, String.format("day: %d: incidental steps(%d), active steps(%d)", i, inactiveSteps, activeSteps));

            barEntries.add(new BarEntry(new float[]{activeSteps, inactiveSteps}, i-1));
        }

        if(max < goal) {
            barChart.getAxisLeft().setAxisMaxValue(goal + 200);
            barChart.getAxisRight().setAxisMaxValue(goal + 200);
        } else {
            barChart.getAxisLeft().setAxisMaxValue(max + 200);
            barChart.getAxisRight().setAxisMaxValue(max + 200);
        }

        Log.d(TAG, String.format("graph maximum set success: %.1f", barChart.getAxisLeft().getAxisMaximum()));

        BarDataSet barDataSet = new BarDataSet(barEntries, "");
        barDataSet.setStackLabels(new String[]{"Active steps", "Incidental steps"});
        barDataSet.setColors(new int[]{Color.rgb(204, 229, 255), Color.rgb(255, 204, 204)});

        ArrayList<String> days = new ArrayList<>();
        days.add("Sun");
        days.add("Mon");
        days.add("Tues");
        days.add("Wed");
        days.add("Thurs");
        days.add("Fri");
        days.add("Sat");

        barData = new BarData(days, barDataSet);

        barChart.setData(barData);

        barChart.animateY(2000);

        goalLine = new LimitLine(goal);
        barChart.getAxisLeft().addLimitLine(goalLine);
        Log.d(TAG, String.format("goal line set success: %d", goal));

        Button button = findViewById(R.id.backToHome);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
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
}
