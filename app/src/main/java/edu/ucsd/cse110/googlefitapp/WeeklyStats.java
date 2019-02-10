package edu.ucsd.cse110.googlefitapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;

public class WeeklyStats extends AppCompatActivity {
    BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weekly_chart);


        Button backButton = findViewById(R.id.backToHome);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        /*
        barChart = findViewById(R.id.barGraph);
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
        days.add("Sunday");
        days.add("Monday");
        days.add("Tuesday");
        days.add("Wednesday");
        days.add("Thursday");
        days.add("Friday");
        days.add("Saturday");

        BarData barData = new BarData((IBarDataSet) days, barDataSet);
        barChart.setData(barData);

        barChart.setTouchEnabled(false);
        barChart.setDragEnabled(false);
        barChart.setScaleEnabled(true);

        */
    }
}
