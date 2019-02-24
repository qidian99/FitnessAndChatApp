package edu.ucsd.cse110.googlefitapp;

public interface Observer {
    public void update(int currentStep, int lastStep, int goal, int day, int yesterday, int today, boolean notCleared);
}
