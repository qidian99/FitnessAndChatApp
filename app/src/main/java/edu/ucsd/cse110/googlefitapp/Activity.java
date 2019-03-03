package edu.ucsd.cse110.googlefitapp;

import android.support.v7.app.AppCompatActivity;

import edu.ucsd.cse110.googlefitapp.observer.Subject;

public abstract class Activity extends AppCompatActivity implements Subject {
    public abstract void updateAll(int num);

    public abstract void setStep(int currentStep);

}
