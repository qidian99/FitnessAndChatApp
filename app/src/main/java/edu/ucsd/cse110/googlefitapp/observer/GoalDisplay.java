package edu.ucsd.cse110.googlefitapp.observer;

import android.util.Log;

import edu.ucsd.cse110.googlefitapp.MainActivity;

public class GoalDisplay implements Observer {
    private MainActivity activity;
    private boolean goalChangeable;

    public GoalDisplay(MainActivity activity) {
        this.activity = activity;
        goalChangeable = activity.getGoalChangeable();
        activity.registerObserver(this);
    }

    @Override
    public void update(int currentStep, int lastStep, int goal, int day, int yesterday, int today, boolean notCleared) {
        if (day != today) {
            String TAG = "GoalDisplay Observer";
            Log.d(TAG, "onProgressUpdate new day encountered");
            activity.setGoalChangeable(true);
            activity.setCanShowHalfEncouragement(true);
            activity.setCanShowOverPrevEncouragement(true);
            activity.getSharedPref().edit().putInt("day", today).apply();
        }

        if (currentStep >= goal && goalChangeable) {
            Log.e("dddddd", "show new goal prompt");
            goalChangeable = false;
            activity.setGoalChangeable(false);
            activity.showNewGoalPrompt();
        }
    }
}
