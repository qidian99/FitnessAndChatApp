package edu.ucsd.cse110.googlefitapp.fitness;

import com.google.android.gms.fitness.request.DataReadRequest;

import java.util.Calendar;

public interface FitnessService {
    int getRequestCode();

    void setup();

    void stopAsync();

    void startAsync();

    boolean hasPermission();

    void updateStepCount();

    void addInactiveSteps(int extraStep);

    void addActiveSteps(final int step, final int min, final int sec, final float stride);
}
