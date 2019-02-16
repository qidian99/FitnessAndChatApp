package edu.ucsd.cse110.googlefitapp.fitness;

public interface FitnessService {
    int getRequestCode();
    void setup();
    void updateStepCount();
    void stopAsync();
    void startAsync();
    boolean hasPermission();
    void addInactiveSteps(int extraStep);
    void addActiveSteps(int step);
}
