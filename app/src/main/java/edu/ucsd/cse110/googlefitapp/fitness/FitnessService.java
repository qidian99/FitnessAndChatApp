package edu.ucsd.cse110.googlefitapp.fitness;

public interface FitnessService {
    int getRequestCode();

    void setup();

    void stopAsync();

    void startAsync();

    boolean hasPermission();

    void updateStepCount();

    void addInactiveSteps(int extraStep);

    void addActiveSteps(final int step, final int min, final int sec, final float stride);

    String getUID();

    String getEmail();
}
