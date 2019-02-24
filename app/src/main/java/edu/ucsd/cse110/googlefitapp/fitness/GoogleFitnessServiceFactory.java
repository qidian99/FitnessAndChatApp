package edu.ucsd.cse110.googlefitapp.fitness;

import java.util.HashMap;
import java.util.Map;

public class GoogleFitnessServiceFactory extends FitnessServiceFactory {
    private static final String TAG = "[FitnessService]";
    private static Map<String, BluePrint> blueprints = new HashMap<>();

    @Override
    public void put(String key, FitnessServiceFactory.BluePrint bluePrint) {
        blueprints.put(key, bluePrint);
    }

    @Override
    public String getTag() {
        return TAG;
    }
}
