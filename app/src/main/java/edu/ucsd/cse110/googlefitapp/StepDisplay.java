package edu.ucsd.cse110.googlefitapp;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.TextView;

public class StepDisplay implements Observer {
    private final String TAG = "StepDisplay Observer";
    private MainActivity activity;

    public StepDisplay(MainActivity activity) {
        this.activity = activity;
        activity.registerObserver(this);
    }

    @Override
    public void update(int currentStep, int lastStep, int goal, int day, int yesterday, int today, boolean notCleared) {
        SharedPreferences sharedPref = activity.getSharedPref();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(String.valueOf(today), currentStep);
        editor.putInt("goal", goal);
        editor.apply();
        Log.d(TAG, "Current goal: " + goal);
        Log.d(TAG, "Total steps up to now: " + currentStep);

        // store total dist
        SharedPreferences statsPref = activity.getStatsPref();
        SharedPreferences.Editor statsEditor = statsPref.edit();
        statsEditor.putFloat(String.valueOf(day + 7), currentStep * activity.getStrideLength() / 63360.0f);
        statsEditor.apply();
        Log.d(TAG, "Today's total distance (incidental + active): " + currentStep * activity.getStrideLength() / 63360.0f);

        SharedPreferences stepPref = activity.getStepPref();
        SharedPreferences.Editor stepEditor = stepPref.edit();
        stepEditor.putInt(String.valueOf(today), currentStep);
        stepEditor.apply();

        final TextView stepText = activity.findViewById(R.id.textStepsMain);
        final TextView stepsLeft = activity.findViewById(R.id.stepsLeft);
        int stepLeft = goal - currentStep > 0 ? goal - currentStep : 0;
        stepText.setText(String.format(MainActivity.SHOW_STEP, currentStep));
        stepsLeft.setText(String.format(MainActivity.SHOW_STEPS_LEFT, stepLeft));
    }
}
