package edu.ucsd.cse110.googlefitapp.observer;

public interface Observer {
    public void update(int currentStep, int lastStep, int goal, int day, int yesterday, int today, boolean notCleared);
}
