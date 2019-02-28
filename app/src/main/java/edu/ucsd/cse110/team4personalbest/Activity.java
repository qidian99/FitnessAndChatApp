package edu.ucsd.cse110.team4personalbest;

import android.support.v7.app.AppCompatActivity;

import edu.ucsd.cse110.team4personalbest.observer.Subject;

public abstract class Activity extends AppCompatActivity implements Subject {
    public abstract void updateAll(int num);

    public abstract void setStep(int currentStep);
}
