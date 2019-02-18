package edu.ucsd.cse110.googlefitapp;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.health.SystemHealthManager;
import android.renderscript.Element;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.result.DailyTotalResult;

import java.security.spec.ECField;
import java.util.Timer;
import java.util.TimerTask;

import edu.ucsd.cse110.googlefitapp.fitness.FitnessService;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessServiceFactory;
import edu.ucsd.cse110.googlefitapp.fitness.GoogleFitAdapter;

public class StepCountActivity extends AppCompatActivity {

    public static final String FITNESS_SERVICE_KEY = "FITNESS_SERVICE_KEY";

    private static final String TAG = "StepCountActivity";
    private static final int RESULT_CODE = 1000;

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
            Log.d(TAG,"start async success");
        } catch (Exception e) {
            Log.d(TAG, "start async failed" + e.toString());
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_count);

        SharedPreferences sharedPref = getSharedPreferences("stepCountData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("recordInitialStep", true);
        editor.apply();

        Timer t = new Timer();

        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isTimePrinted) {
                            Log.d(TAG, "timer started");
                            isTimePrinted = true;
                        }

                        tv = (TextView)findViewById(R.id.timer_text);

                        setTime();
                        setDistance();
                        setSpeed();
                        time += 1;
                    }
                });
            }
        }, 0, 1000);

        textSteps = findViewById(R.id.textSteps);
        textDist = findViewById(R.id.textDistance);
        textDist.setText(String.format("%.1f miles", distance));
        textSpeed = findViewById(R.id.textSpeed);
        textSpeed.setText(String.format("%.1f MPH", speed));

        strideLen = getIntent().getFloatExtra("stride", 0);

        String fitnessServiceKey = getIntent().getStringExtra(FITNESS_SERVICE_KEY);
        fitnessService = FitnessServiceFactory.create(fitnessServiceKey, this);
        fitnessService.updateStepCount();

        Button btnUpdateSteps = findViewById(R.id.buttonUpdateSteps);
        btnUpdateSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fitnessService.updateStepCount();
                Log.d(TAG, "update step count success");
            }
        });

        Button btnMockData = findViewById(R.id.btnMockDt);
        btnMockData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((GoogleFitAdapter) fitnessService).mockDataPoint();
                Log.d(TAG, "mock data point success");
            }
        });

        Button btnEndRecord = findViewById(R.id.btnEndRecord);
        btnEndRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fitnessService.stopAsync();
                Intent homescreen = new Intent(getApplicationContext(), MainActivity.class);
                homescreen.putExtra("speed", speed);
                homescreen.putExtra("steps", steps);
                homescreen.putExtra("min", time / 60);
                homescreen.putExtra("second", time % 60);
                homescreen.putExtra("distance", distance);
                setResult(RESULT_CODE, homescreen);
                finish();
            }
        });

        fitnessService.setup();
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

        if(recordInitialStep) {
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
        if(time == 0) {
            speed = 0.0f;
        } else {
            speed = distance/(float)time * 3600.0f;
        }
        textSpeed.setText(String.format("%.1f MPH", speed));
    }

    public void setTime() {
        int min = time / 60;
        int sec = time % 60;

        if(sec < 10) {
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

    public void setStrideLen(float strideLen) {
        this.strideLen = strideLen;
    }

    public void setDistance(float dist) {
        this.distance = dist;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getTime() {
        return time;
    }

    public int getSteps() {
        return steps;
    }

    public float getDistance() {
        return distance;
    }

    public float getSpeed() {
        return speed;
    }
}
