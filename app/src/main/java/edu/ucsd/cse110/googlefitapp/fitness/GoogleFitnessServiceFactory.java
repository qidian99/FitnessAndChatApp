package edu.ucsd.cse110.googlefitapp.fitness;

public class GoogleFitnessServiceFactory extends FitnessServiceFactory {
    private static final String TAG = "[FitnessService]";

    @Override
    public void put(String key, FitnessServiceFactory.BluePrint bluePrint) {
        blueprints.put(key, bluePrint);
    }

    @Override
    public String getTag() {
        return TAG;
    }
}
