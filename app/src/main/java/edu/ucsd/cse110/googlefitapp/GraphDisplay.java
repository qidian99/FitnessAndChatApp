package edu.ucsd.cse110.googlefitapp;

import android.util.Log;

import java.util.Calendar;

public class GraphDisplay implements Observer{
    private final String TAG = "GraphtDisplay Observer";
    private MainActivity activity;
    public GraphDisplay(MainActivity activity){
        this.activity = activity;
        activity.registerObserver(this);
    }

    @Override
    public void update(int currentStep, int lastStep, int goal, int day, int yesterday, int today, boolean notCleared) {
        if(today == Calendar.SUNDAY) {
            if (notCleared) {
                activity.getSharedPref().edit().putBoolean("graphNotCleared", false).apply();
                activity.getStepPref().edit().clear().apply();
                activity.getStatsPref().edit().clear().apply();
                Log.d(TAG, "Sunday: bar graph is cleared");
                activity.setNotCleared(false);
            }
        } else {
            activity.getSharedPref().edit().putBoolean("graphNotCleared", true).apply();
        }
    }
}