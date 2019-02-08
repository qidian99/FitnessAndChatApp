package edu.ucsd.cse110.googlefitapp;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
    private FitnessService fitnessService;
    private long steps = 0;
    private double distance = 0;
    private int time = 0;
    private double speed = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_count);

        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView tv = (TextView)findViewById(R.id.timer_text);
                        int min = time / 60;
                        int sec = time - min*60;

                        if(sec < 10) {
                            tv.setText(String.format("%d:0%d", min, sec));
                        } else {
                            tv.setText(String.format("%d:%d", min, sec));
                        }

                        time += 1;
                    }
                });
            }
        }, 0, 1000);

        textSteps = findViewById(R.id.textSteps);

        String fitnessServiceKey = getIntent().getStringExtra(FITNESS_SERVICE_KEY);
        fitnessService = FitnessServiceFactory.create(fitnessServiceKey, this);

        Button btnUpdateSteps = findViewById(R.id.buttonUpdateSteps);
        btnUpdateSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fitnessService.updateStepCount();
            }
        });

        Button btnMockData = findViewById(R.id.btnMockDt);
        btnMockData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((GoogleFitAdapter) fitnessService).mockDataPoint();
            }
        });

        Button btnEndRecord = findViewById(R.id.btnEndRecord);
        btnEndRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fitnessService.stopAsync();
                Intent homescreen = new Intent(getApplicationContext(), MainActivity.class);
                homescreen.putExtra("distance", distance);
                // homescreen.putExtra("time", time);
                if(time == 0) {
                    speed = 0.0;
                } else {
                    speed = distance/(double)time;
                }
                homescreen.putExtra("speed", speed);
                homescreen.putExtra("steps", steps);
                homescreen.putExtra("time", time);
                setResult(RESULT_CODE, homescreen);
                finish();
            }
        });

        fitnessService.setup();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//       If authentication was required during google fit setup, this will be called after the user authenticates
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == fitnessService.getRequestCode()) {
                fitnessService.updateStepCount();
            }
        } else {
            Log.e(TAG, "ERROR, google fit result code: " + resultCode);
        }
    }

    public void setStepCount(long stepCount) {
        textSteps.setText(String.valueOf(stepCount));
        if(stepCount >= 1000){
            showEncouragement(stepCount);
        }
    }

    public void showEncouragement(long stepCount){

        Context context = getApplicationContext();
        CharSequence text = String.format("Good job! You're already at %d percent of the daily recommended number of steps.", (int) stepCount / 100);

        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}
