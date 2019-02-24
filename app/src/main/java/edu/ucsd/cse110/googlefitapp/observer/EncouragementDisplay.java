package edu.ucsd.cse110.googlefitapp.observer;

import android.util.Log;

import edu.ucsd.cse110.googlefitapp.MainActivity;

public class EncouragementDisplay implements Observer {
    private final String TAG = "Encouragement Observer";
    private MainActivity activity;

    public EncouragementDisplay(MainActivity activity) {
        this.activity = activity;
        activity.registerObserver(this);
    }

    @Override
    public void update(int currentStep, int lastStep, int goal, int day, int yesterday, int today, boolean notCleared) {
        if (day != today) {
            Log.d(TAG, "new day encountered");
            activity.setCanShowHalfEncour(true);
            activity.setCanShowOverPrevEncour(true);
        }

        if (currentStep > goal / 2 && activity.getCanShowHalfEncour()) {
            Log.d(TAG, "need show achieveHalfEncouragement");
            activity.showAchieveHalfEncouragement();
        }

        if (currentStep > goal && activity.getGoalChangeable()) {
            Log.d(TAG, "need show newGoalPrompt");
            activity.showNewGoalPrompt();
        }

        if (currentStep > lastStep + 1000 && activity.getCanShowOverPrevEncour()) {
            Log.d(TAG, "need show OverPrevEncouragement");
            activity.showOverPrevEncouragement();
        }
    }
}
