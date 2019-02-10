package edu.ucsd.cse110.googlefitapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
    public static final String SHOW_STEPS_LEFT = "You have %d steps left.";

    public static final long DEFAULT_GOAL = 5000L;
    public static boolean firstPromptHeight = true;

    private boolean switchToActive = false;
    private long goal;

    private boolean isCancelled = false;
    private long currentSteps;
    private boolean goalReached = false;

    private double activeDistance;
    private double activeSpeed;
    private int activeMin;
    private int activeSec;
    private long activeSteps;
    private float strideLength;

    private long currDisplaySteps;
    private Encouragement encourage;


    //this is only ran when we run the app again (given it is not deleted from the "recent" apps)
    @Override
    protected void onRestart() {
        super.onRestart();

        final TextView stepText = findViewById(R.id.textStepsMain);


        final Long beforeSteps = getLastStepCount();


        //TODO : can add the encourgement here at the beginning of the app
        long total = getCurrentSteps();
        encourage.getEncourgementOnLiveUpdate(total, beforeSteps, goal);

    }

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
        strideLength = sharedPreferences.getFloat("stride", 0);
        this.goal = sharedPreferences.getLong("goal", DEFAULT_GOAL);
        /* Encouragement
         - set to show every app startup
         - can be made only daily (NEED TO IMPLEMENT)*/
        encourage = new Encouragement(this, false);

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
        final TextView stepsLeft = findViewById(R.id.stepsLeft);

        //this is called to retrieve the before steps when the app is opened for the first time
        final Long beforeSteps = getLastStepCount();

        if (GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            Fitness.getHistoryClient(this, lastSignedInAccount)
                    .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                    .addOnSuccessListener(
                            new OnSuccessListener<DataSet>() {
                                @Override
                                public void onSuccess(DataSet dataSet) {
                                    if (dataSet.isEmpty()) {
                                        int stepCountDelta = 2500;
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

                                    currentSteps = total;

                                    stepText.setText(String.format(SHOW_STEP, total));
                                    SharedPreferences pref = getSharedPreferences("user_data", MODE_PRIVATE);
                                    long stepLeft = goal - total > 0 ? goal - total : 0;
                                    stepsLeft.setText(String.format(SHOW_STEPS_LEFT, stepLeft));

                                    /* Passive encouragement - for now shows when app opened */
                                    encourage.getEncourgementOnLiveUpdate(total, beforeSteps, goal);

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

        // Go to the bar chart activity.
        Button goToWeekly = findViewById(R.id.weeklyButton);
        goToWeekly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchWeeklyStats();
            }
        });

    }

    public void launchWeeklyStats() {
        Intent intent = new Intent(MainActivity.this, WeeklyStats.class);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();

        System.out.println("HI MOMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");
        SharedPreferences sharedPreferences = getSharedPreferences("lastKnownSteps", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        final TextView stepText = findViewById(R.id.textStepsMain);

        //get the steps that were there on the UI before we got the new steps
        String beforeStepText = String.valueOf(stepText.getText());
        String[] separatedStrings = beforeStepText.split(" ");
        Long before = Long.valueOf(separatedStrings[3]);


        editor.putLong("Before", before);
        editor.apply();




    }
    private Long getLastStepCount() {

        SharedPreferences sharedPreferences = getSharedPreferences("lastKnownSteps", MODE_PRIVATE);

        Long beforeSteps = sharedPreferences.getLong("Before", 0);



        return beforeSteps;

    }
    private void updateStepCount() {

        //the first encouragement will come after the first refresh of the live feedback
        final Long beforeSteps = getLastStepCount();

        final TextView stepText = findViewById(R.id.textStepsMain);
        final TextView stepsLeft = findViewById(R.id.stepsLeft);

        GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this);

            Fitness.getHistoryClient(this, lastSignedInAccount)
                    .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                    .addOnSuccessListener(
                            new OnSuccessListener<DataSet>() {
                                @Override
                                public void onSuccess(DataSet dataSet) {
                                    if (dataSet.isEmpty()) {
                                        int stepCountDelta = 2500;
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

                                    currentSteps = total;

                                    //get the steps that were there on the UI before we got the new steps
//                                    String beforeStepText = String.valueOf(stepText.getText());
//                                    String[] separatedStrings = beforeStepText.split(" ");
//                                    Long before = Long.valueOf(separatedStrings[3]);
                                    long before = getCurrentSteps();


                                    stepText.setText(String.format(SHOW_STEP, total));
                                    SharedPreferences pref = getSharedPreferences("user_data", MODE_PRIVATE);
                                    long stepLeft = goal - total > 0 ? goal - total : 0;
                                    stepsLeft.setText(String.format(SHOW_STEPS_LEFT, stepLeft));

                                    /* Passive encouragement - for now shows when app opened */
                                    encourage.getEncourgementOnLiveUpdate(total, before, goal);

                                    //asks the user only once
                                    if((total >= goal) && !goalReached) {
                                        showNewGoalPrompt();
                                        goalReached = true;

                                    }
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                }
                            });

    }

    private long getCurrentSteps() {
        final TextView stepText = findViewById(R.id.textStepsMain);


        String beforeStepText = String.valueOf(stepText.getText());
        String[] separatedStrings = beforeStepText.split(" ");
        Long before = Long.valueOf(separatedStrings[3]);
        return before;
    }
    private class LiveUpdate extends AsyncTask<String, String, Void> {

        private String resp;
        ProgressDialog progressDialog;

        @Override
        protected Void doInBackground(String... sleepTime) {
            while(!isCancelled) {

                try {

                    Thread.sleep(Integer.valueOf(sleepTime[0]));
                    publishProgress();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(String... text) {

            if (isCancelled) {
                cancel(true);
            } else {
                //call update steps here
                updateStepCount();
            }
        }
    }



    public void launchStepCountActivity() {
        Intent intent = new Intent(this, StepCountActivity.class);
        intent.putExtra(StepCountActivity.FITNESS_SERVICE_KEY, fitnessServiceKey);
        intent.putExtra("stride", strideLength);
        startActivityForResult(intent, REQUEST_CODE);
        switchToActive = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(switchToActive) {
            super.onActivityResult(requestCode, resultCode, data);
            activeDistance = data.getDoubleExtra("distance", 0.0);
            activeSpeed = data.getDoubleExtra("speed", 0.0);
            activeMin = data.getIntExtra("min", 0);
            activeSec = data.getIntExtra("second", 0);
            activeSteps = data.getLongExtra("steps", 0);
        }

        if(activeSteps >= this.goal) {
            showNewGoalPrompt();
        }

        displayActiveData();

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

    public void showNewGoalPrompt() {
        FragmentManager fm = getSupportFragmentManager();
        NewGoalSetter setGoalDialogFragment = NewGoalSetter.newInstance(getString(R.string.congratsPrompt), goal);
        setGoalDialogFragment.show(fm, "fragment_set_new_goal");
    }

    private void displayActiveData() {
        FragmentManager fm = getSupportFragmentManager();
        DataDisplayer dataDisplayer = DataDisplayer.newInstance(getString(R.string.prevSession), activeDistance, activeSpeed, activeSteps, activeMin, activeSec);
        dataDisplayer.show(fm, "fragment_display_active_data");
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

    //when we are done with the new goal
    @Override
    public void onFinishEditDialog(long goal) {
        TextView goalText = findViewById(R.id.textGoal);
        goalText.setText(String.format(SHOW_GOAL, goal));

        // Save new goal
        this.goal = goal;

        encourage.resetAllEncourgements();
        goalReached = false;

        SharedPreferences sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("goal", goal);
        editor.apply();
    }

}
