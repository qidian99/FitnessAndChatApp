package edu.ucsd.cse110.googlefitapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import edu.ucsd.cse110.googlefitapp.fitness.FitnessService;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessServiceFactory;
import edu.ucsd.cse110.googlefitapp.fitness.GoogleFitAdapter;

public class MainActivity extends AppCompatActivity implements HeightPrompter.HeightPrompterListener, CustomGoalSetter.GoalPrompterListener {
    private String fitnessServiceKey = "GOOGLE_FIT";
    private final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = System.identityHashCode(this) & 0xFFFF;
    private static final int REQUEST_CODE = 1000;

    public static final String SHOW_STRIDE = "Your estimated stride length is %.2f\"";
    public static final String SHOW_GOAL = "Your current goal is %d steps.";
    public static final String SHOW_STEP = "Your have taken %d steps.";
    public static final String TMP_RESULT = "distance: %.2f, speed: %.2f, time: %d, steps: %d";

    public static final long DEFAULT_GOAL = 5000L;
    public static boolean firstPromptHeight = true;

    private long goal;

    private double activeDistance;
    private double activeSpeed;
    private int activeTimeElapsed;
    private long activeSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FitnessServiceFactory.put(fitnessServiceKey, new FitnessServiceFactory.BluePrint() {
            @Override
            public FitnessService create(StepCountActivity stepCountActivity) {
                return new GoogleFitAdapter(stepCountActivity);
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE);
        String magnitude = sharedPreferences.getString("magnitude", "");
        String metric = sharedPreferences.getString("metric", "");
        this.goal = sharedPreferences.getLong("goal", DEFAULT_GOAL);
        float strideLength = sharedPreferences.getFloat("stride", 0);

        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .build();

        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this, // your activity
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);
        }

        // Update goal
        long currentGoal = sharedPreferences.getLong("goal", -1);
        if( currentGoal == -1 ){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong("goal", DEFAULT_GOAL);
            editor.apply();
            currentGoal = DEFAULT_GOAL;
        }
        TextView goalText = findViewById(R.id.textGoal);
        goalText.setText(String.format(SHOW_GOAL, currentGoal));

        // Update step
        GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this);
        final TextView stepText = findViewById(R.id.textStepsMain);
        if (GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            Fitness.getHistoryClient(this, lastSignedInAccount)
                    .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                    .addOnSuccessListener(
                            new OnSuccessListener<DataSet>() {
                                @Override
                                public void onSuccess(DataSet dataSet) {
                                    if (dataSet.isEmpty()) {
                                        int stepCountDelta = 950;
                                        Calendar cal = Calendar.getInstance();
                                        Date now = new Date();
                                        cal.setTime(now);
                                        long endTime = cal.getTimeInMillis();
                                        cal.add(Calendar.HOUR_OF_DAY, -1);
                                        long startTime = cal.getTimeInMillis();
                                        DataPoint dataPoint =
                                                dataSet.createDataPoint().setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
                                        dataPoint.getValue(Field.FIELD_STEPS).setInt(stepCountDelta);
                                        dataSet.add(dataPoint);
                                    }

                                    long total =
                                            dataSet.isEmpty()
                                                    ? 0
                                                    : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                                    stepText.setText(String.format(SHOW_STEP, total));
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                }
                            });
        }

        if(strideLength == 0){
            showHeightPrompt();

        }
        firstPromptHeight = false;

        // In development, we allow users to re-enter their heights
        Button setHeightBtn = findViewById(R.id.clearBtn);
        setHeightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHeightPrompt();
            }
        });

        // Users can customize their goals
        Button setGoalBtn = findViewById(R.id.btnSetGoal);
        setGoalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomGoalPrompt();
            }
        });

        // Start an active session
        Button btnGoToSteps = findViewById(R.id.startBtn);
        btnGoToSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchStepCountActivity();
            }
        });

    }

    public void launchStepCountActivity() {
        Intent intent = new Intent(this, StepCountActivity.class);
        intent.putExtra(StepCountActivity.FITNESS_SERVICE_KEY, fitnessServiceKey);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        activeDistance = data.getDoubleExtra("distance", 0.0);
        activeSpeed = data.getDoubleExtra("speed", 0.0);
        activeTimeElapsed = data.getIntExtra("time", 0);
        activeSteps = data.getLongExtra("steps", 0);

        Toast.makeText(getApplicationContext(), String.format(TMP_RESULT,
                activeDistance, activeSpeed, activeTimeElapsed, activeSteps),
                Toast.LENGTH_LONG).show();
        // Toast.makeText(this,String.format("distance: %.2f, speed: %.2f", activeDistance, activeSpeed), Toast.LENGTH_LONG).show();

        if(activeSteps >= this.goal) {
            showNewGoalPrompt();
        }
    }

    public void setFitnessServiceKey(String fitnessServiceKey) {
        this.fitnessServiceKey = fitnessServiceKey;
    }

    private void showHeightPrompt() {
        FragmentManager fm = getSupportFragmentManager();
        HeightPrompter editNameDialogFragment = HeightPrompter.newInstance(getString(R.string.heightPrompt));
        editNameDialogFragment.show(fm, "fragment_prompt_height");
    }

    private void showCustomGoalPrompt() {
        FragmentManager fm = getSupportFragmentManager();
        CustomGoalSetter setGoalDialogFragment = CustomGoalSetter.newInstance(getString(R.string.setGoalPrompt));
        setGoalDialogFragment.show(fm, "fragment_set_goal");
    }
    
    private void showNewGoalPrompt() {
        FragmentManager fm = getSupportFragmentManager();
        NewGoalSetter setGoalDialogFragment = NewGoalSetter.newInstance(getString(R.string.congratsPrompt), goal);
        setGoalDialogFragment.show(fm, "fragment_set_new_goal");
    }

    @Override
    public void onFinishEditDialog(String[] inputText) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("magnitude", inputText[0]);
        editor.putString("metric", inputText[1]);

        // Estimate stride length, in inches
        float strideLength = 0;
        // Case 1: use centimeter as metric
        if(Integer.parseInt(inputText[0]) == 0 ){
            strideLength = (float) (Integer.parseInt(inputText[1]) / 2.54 * 0.413);
        }
        // Case 2: use feet as metric
        else {
            strideLength = (float) ((Integer.parseInt(inputText[1])*12 + Integer.parseInt(inputText[2])) * 0.413);
        }
        editor.putFloat("stride", strideLength);
        editor.apply();

        Toast.makeText(this, "Height saved", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, String.format(SHOW_STRIDE, strideLength), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onFinishEditDialog(long goal) {
        TextView goalText = findViewById(R.id.textGoal);
        goalText.setText(String.format(SHOW_GOAL, goal));

        // Save new goal
        this.goal = goal;
        SharedPreferences sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("goal", goal);
        editor.apply();
    }
}
