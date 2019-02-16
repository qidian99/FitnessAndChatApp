package edu.ucsd.cse110.googlefitapp.fitness;

import com.google.android.gms.fitness.request.DataReadRequest;

import java.util.Calendar;

public interface FitnessService {
    int getRequestCode();
    void setup();
    void updateStepCount();
    void stopAsync();
    void startAsync();
    boolean hasPermission();
    void addInactiveSteps(int extraStep);
    void addActiveSteps(int step);
    DataReadRequest getLast7DaysSteps(double[] weeklyInactiveSteps, double[] weeklyActiveSteps);
    DataReadRequest getLast7DaysSteps(double[] weeklyInactiveSteps, double[] weeklyActiveSteps, Calendar cal);
}
