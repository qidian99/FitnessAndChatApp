package edu.ucsd.cse110.googlefitapp;

import android.support.v7.app.AppCompatActivity;

public abstract class Activity extends AppCompatActivity implements Subject{
    public abstract void updateAll(int num);
}
