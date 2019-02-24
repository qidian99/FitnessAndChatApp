package edu.ucsd.cse110.googlefitapp.observer;

import android.util.Log;

import edu.ucsd.cse110.googlefitapp.MainActivity;

public class GoalDisplay implements Observer {
    private final String TAG = "GoalDisplay Observer";
    private MainActivity activity;
    private boolean goalChangeable = false;

    public GoalDisplay(MainActivity activity) {
        this.activity = activity;
        activity.registerObserver(this);
    }

    @Override
    public void update(int currentStep, int lastStep, int goal, int day, int yesterday, int today, boolean notCleared) {
        if (day != today) {
            Log.d(TAG, "onProgressUpdate new day encountered");
            activity.setGoalChangeable(true);
            activity.setCanShowHalfEncour(true);
            activity.setCanShowOverPrevEncour(true);
        }

        if (currentStep >= goal && goalChangeable) {
            goalChangeable = false;
            activity.showNewGoalPrompt();
        }
    }
}
