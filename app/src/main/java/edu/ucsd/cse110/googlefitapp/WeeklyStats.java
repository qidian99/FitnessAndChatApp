package edu.ucsd.cse110.googlefitapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getDateInstance;

public class WeeklyStats extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_stats);

        BarChart barChart;

        barChart = (BarChart) findViewById(R.id.barGraph);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelsToSkip(0);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);

        barChart.setDescription("");
        barChart.setAutoScaleMinMaxEnabled(true);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setScaleEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setTouchEnabled(true);

        /*
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        java.text.DateFormat dateFormat = getDateInstance();
        Log.i(TAG, "Range Start: " + dateFormat.format(startTime));
        Log.i(TAG, "Range End: " + dateFormat.format(endTime));

        DataReadRequest readRequest =
                new DataReadRequest.Builder()
                    .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                    .bucketByTime(1, TimeUnit.DAYS)
                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                    .build();

        Task<DataReadResponse> response = Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this)).readData(readRequest);
        List<DataSet> dataSets = response.getResult().getDataSets();

        private static void dumpDataSet(DataSet dataSet) {
            Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());

            for (DataPoint dp : dataSet.getDataPoints()) {
                Log.i(TAG, "Data point:");
                Log.i(TAG, "\tType: " + dp.getDataType().getName());
                Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
                Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
                for (Field field : dp.getDataType().getFields()) {
                    Log.i(TAG, "\tField: " + field.getName() + " Value: " + dp.getValue(field));
                }
            }
        }
        */

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(11f, 0));
        barEntries.add(new BarEntry(14f, 1));
        barEntries.add(new BarEntry(15f, 2));
        barEntries.add(new BarEntry(20f, 3));
        barEntries.add(new BarEntry(19f, 4));
        barEntries.add(new BarEntry(13f, 5));
        barEntries.add(new BarEntry(15f, 6));

        BarDataSet barDataSet = new BarDataSet(barEntries, "Steps");

        ArrayList<String> days = new ArrayList<>();
        days.add("Sun");
        days.add("Mon");
        days.add("Tues");
        days.add("Wed");
        days.add("Thurs");
        days.add("Fri");
        days.add("Sat");

        BarData barData = new BarData(days, barDataSet);
        barChart.setData(barData);

        barChart.animateY(5000);



        Button button = findViewById(R.id.backToHome);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
}
