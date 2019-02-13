package edu.ucsd.cse110.googlefitapp.fitness;

import edu.ucsd.cse110.googlefitapp.StepCountActivity;

public class StepCounterAdapter extends GoogleFitAdapter {

    StepCountActivity stepCountActivity;

    public StepCounterAdapter(StepCountActivity activity, StepCountActivity sca) {
        super(activity);
        stepCountActivity = sca;
    }

    @Override
    public void updateActivity() {
        stepCountActivity.updateAll(totalSteps);

    }

}
