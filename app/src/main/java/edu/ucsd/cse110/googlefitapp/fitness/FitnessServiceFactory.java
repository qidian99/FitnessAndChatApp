package edu.ucsd.cse110.googlefitapp.fitness;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import edu.ucsd.cse110.googlefitapp.Activity;

public abstract class FitnessServiceFactory {
    public static Map<String, BluePrint> blueprints = new HashMap<>();

    public abstract void put(String key, BluePrint bluePrint);

    public FitnessService create(String key, Activity activity) {
        Log.i(getTag(), String.format("creating Service with key %s", key));
        return blueprints.get(key).create(activity);
    }

    public abstract String getTag();

    public interface BluePrint {
        FitnessService create(Activity activity);
    }
}
