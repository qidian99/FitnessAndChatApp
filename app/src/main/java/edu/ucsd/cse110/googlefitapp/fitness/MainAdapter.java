package edu.ucsd.cse110.googlefitapp.fitness;

import android.app.Activity;

import edu.ucsd.cse110.googlefitapp.MainActivity;

public class MainAdapter extends GoogleFitAdapter {

    MainActivity mainActivity;

    public MainAdapter(MainActivity activity, MainActivity ma) {
        super(activity);
        mainActivity = ma;
    }

    @Override
    public void updateActivity() {
        mainActivity.updateAll(totalSteps);
    }
}
