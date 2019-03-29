package edu.ucsd.cse110.googlefitapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import edu.ucsd.cse110.googlefitapp.adapter.PlannedWalkAdapter;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessService;
import edu.ucsd.cse110.googlefitapp.observer.Observer;

public class PlannedWalkActivity extends Activity {

    public static final String FITNESS_SERVICE_KEY = "FITNESS_SERVICE_KEY";

    private static final String TAG = "PlannedWalkActivity";
    private static final int RESULT_CODE = 1000;
    private static final String MILES_FMT = "%.1f miles";
    private static final String DIST_FMT = "%.1f MPH";

    private TextView textSteps;
    private TextView textDist;
    private TextView textSpeed;
    private TextView tv;
    private FitnessService fitnessService;
    private int steps = 0;
    private int time = 0;
    private float distance = 0.0f;
    private float speed = 0.0f;
    private float strideLen;
    private boolean recordInitialStep = true;
    private int initialSteps;
    private boolean isTimePrinted = false;

    @Override
    protected void onRestart() {
        try {
            super.onRestart();
            fitnessService.startAsync();
            Log.d(TAG, "start async success");
        } catch (Exception e) {
            Log.d(TAG, "start async failed" + e.toString());
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_count);

        boolean test = getIntent().getBooleanExtra("test", false);

        SharedPreferences sharedPref = getSharedPreferences("stepCountData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("recordInitialStep", true);
        editor.apply();

        tv = findViewById(R.id.timer_text);

        if (!test) {
            Timer t = new Timer();
            t.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> {
                        if (!isTimePrinted) {
                            Log.d(TAG, "timer started");
                            isTimePrinted = true;
                        }

                        tv = findViewById(R.id.timer_text);

                        setTime();
                        setDistance();
                        setSpeed();
                        time += 1;
                    });
                }
            }, 0, 1000);
        }

        textSteps = findViewById(R.id.textSteps);
        textDist = findViewById(R.id.textDistance);
        textDist.setText(String.format(MILES_FMT, distance));
        textSpeed = findViewById(R.id.textSpeed);
        textSpeed.setText(String.format(DIST_FMT, speed));

        strideLen = getIntent().getFloatExtra("stride", 0);

        String fitnessServiceKey = getIntent().getStringExtra(FITNESS_SERVICE_KEY);
        fitnessService = MainActivity.fitnessServiceFactory.create(fitnessServiceKey, this);
        fitnessService.updateStepCount();

        Button btnUpdateSteps = findViewById(R.id.buttonUpdateSteps);
        btnUpdateSteps.setOnClickListener(v -> {
            fitnessService.updateStepCount();
            Log.d(TAG, "update step count success");
        });

        Button btnMockData = findViewById(R.id.btnMockDt);
        btnMockData.setOnClickListener(v -> {
            ((PlannedWalkAdapter) fitnessService).mockDataPoint();
            Log.d(TAG, "mock data point success");
        });

        Button btnEndRecord = findViewById(R.id.btnEndRecord);
        btnEndRecord.setOnClickListener(v -> {
            fitnessService.stopAsync();
            Intent homescreen = new Intent(getApplicationContext(), MainActivity.class);
            homescreen.putExtra("speed", speed);
            homescreen.putExtra("steps", steps);
            homescreen.putExtra("min", time / 60);
            homescreen.putExtra("second", time % 60);
            homescreen.putExtra("distance", distance);
            setResult(RESULT_CODE, homescreen);
            finish();
        });
    }

    @Override
    protected void onStop() {
        try {
            super.onStop();
            fitnessService.stopAsync();
            Log.d(TAG, "stop async success");
        } catch (Exception e) {
            Log.d(TAG, "stop async failed " + e.toString());
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //If authentication was required during google fit setup, this will be called after the user authenticates
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == fitnessService.getRequestCode()) {
                fitnessService.updateStepCount();
            }
            Log.e(TAG, "SUCCESS, google fit result code: " + resultCode);
        } else {
            Log.e(TAG, "ERROR, google fit result code: " + resultCode);
        }
    }

    public void setStepCount(int stepCount) {
        SharedPreferences sharedPref = getSharedPreferences("stepCountData", MODE_PRIVATE);
        recordInitialStep = sharedPref.getBoolean("recordInitialStep", true);

        if (recordInitialStep) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("initialSteps", stepCount);
            editor.putBoolean("recordInitialStep", false);
            editor.apply();
        }

        initialSteps = sharedPref.getInt("initialSteps", stepCount);
        steps = stepCount - initialSteps;
        textSteps.setText(String.format("%d", steps));
    }

    public void setDistance() {
        distance = steps * strideLen / 63360.0f;
        textDist.setText(String.format("%.1f miles", distance));
    }

    public void setSpeed() {
        if (time == 0) {
            speed = 0.0f;
        } else {
            speed = distance / (float) time * 3600.0f;
        }
        textSpeed.setText(String.format("%.1f MPH", speed));
    }

    public void setTime() {
        int min = time / 60;
        int sec = time % 60;

        if (sec < 10) {
            tv.setText(String.format("%d:0%d", min, sec));
        } else {
            tv.setText(String.format("%d:%d", min, sec));
        }
    }

    public void updateAll(int stepCount) {
        setStepCount(stepCount);
        setDistance();
        setSpeed();
    }

    @Override
    public void setStep(int currentStep) {
        this.steps = currentStep;
    }

    @Override
    public int getGoal() {
        return 0;
    }

    @Override
    public float getStrideLength() {
        return 0;
    }

    public void setStrideLen(float strideLen) {
        this.strideLen = strideLen;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getSteps() {
        return steps;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float dist) {
        this.distance = dist;
    }

    public float getSpeed() {
        return speed;
    }

    @Override
    public void registerObserver(Observer o) {

    }

    @Override
    public void removeObserver(Observer o) {

    }

    @Override
    public void notifyObservers() {

    }
}
