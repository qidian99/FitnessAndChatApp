package edu.ucsd.cse110.googlefitapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import edu.ucsd.cse110.googlefitapp.fitness.FitnessService;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessServiceFactory;
import edu.ucsd.cse110.googlefitapp.fitness.GoogleFitAdapter;
import edu.ucsd.cse110.googlefitapp.fitness.MainAdapter;
import edu.ucsd.cse110.googlefitapp.fitness.StepCounterAdapter;

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
    public static boolean firstTimeUser = true;

    private boolean switchToActive = false;
    private long goal;

    private boolean isCancelled = false;
    private long currentSteps;
    private boolean goalReached = false;

    private double activeDistance;
    private double activeSpeed;
    private int activeMin;
    private int activeSec;
    private long activeSteps = 0;
    private float strideLength;
    private FitnessOptions fitnessOptions;

    private long currDisplaySteps;
    private Encouragement encourage;

    private FitnessService fitnessService;
    private Calendar calendar = Calendar.getInstance();
    private long[] weeklyData = new long[14];


    //this is only ran when we run the app again (given it is not deleted from the "recent" apps)
    @Override
    protected void onRestart() {
        super.onRestart();

        if(GoogleSignIn.getLastSignedInAccount(this) != null) {
            System.out.println("Get here on restart");
            final TextView stepText = findViewById(R.id.textStepsMain);
            final Long beforeSteps = getLastStepCount();
            //TODO : can add the encourgement here at the beginning of the app
            long total = getCurrentSteps();
            encourage.getEncourgementOnLiveUpdate(total, beforeSteps, goal);
            fitnessService.startAsync();
            fitnessService.setup();
            Toast.makeText(this, "started main ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toast.makeText(this, "started main ", Toast.LENGTH_SHORT).show();
        FitnessServiceFactory.put(fitnessServiceKey, new FitnessServiceFactory.BluePrint() {
            @Override
            public FitnessService create(StepCountActivity stepCountActivity) {
                //return new GoogleFitAdapter(stepCountActivity);
                return new StepCounterAdapter(stepCountActivity, stepCountActivity);
            }
        });
        SharedPreferences sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE);
        String magnitude = sharedPreferences.getString("magnitude", "");
        String metric = sharedPreferences.getString("metric", "");
        strideLength = sharedPreferences.getFloat("stride", 0);
        firstTimeUser = strideLength == 0 || GoogleSignIn.getLastSignedInAccount(this) == null;
        this.goal = sharedPreferences.getLong("goal", DEFAULT_GOAL);
        float strideLength = sharedPreferences.getFloat("stride", 0);
        /* Encouragement
         - set to show every app startup
         - can be made only daily (NEED TO IMPLEMENT)*/
        encourage = new Encouragement(this, false);
        fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .build();

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

        fitnessService = new MainAdapter(this, this);
        fitnessService.updateStepCount();
        fitnessService.setup();

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

        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            Toast.makeText(this, "You must login with Google to use this app", Toast.LENGTH_SHORT).show();
            GoogleSignIn.requestPermissions(
                    this, // your activity
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);
        }
    }

<<<<<<< HEAD
=======
    public void launchWeeklyStats() {
        Intent intent = new Intent(MainActivity.this, WeeklyStats.class);
        intent.putExtra("weeklyData", weeklyData);
        startActivity(intent);
>>>>>>> ca65d51... graph first version
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(GoogleSignIn.getLastSignedInAccount(this) != null) {
            System.out.println("HI MOMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");
            SharedPreferences sharedPreferences = getSharedPreferences("lastKnownSteps", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

<<<<<<< HEAD
        System.out.println("HI MOMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");

        Date date = calendar.getTime();
        String time = new SimpleDateFormat("HH:mm:ss").format(date);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        int sec = calendar.get(Calendar.SECOND);
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        System.out.println(time);

        if(hour != 0 && min != 0 && sec != 0 ) {
            // SharedPreferences sharedPref = getSharedPreferences("weekly_records", MODE_PRIVATE);
            System.out.println("updated ----------- ");
            weeklyData[day - 1] = getCurrentSteps();
        }

        SharedPreferences sharedPreferences = getSharedPreferences("lastKnownSteps", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        final TextView stepText = findViewById(R.id.textStepsMain);

        //get the steps that were there on the UI before we got the new steps
        String beforeStepText = String.valueOf(stepText.getText());
        String[] separatedStrings = beforeStepText.split(" ");
        Long before = Long.valueOf(separatedStrings[3]);

=======
            final TextView stepText = findViewById(R.id.textStepsMain);
>>>>>>> 09d22ec40cc2745c533f49e2a3e6c5d12dfa7929

            //get the steps that were there on the UI before we got the new steps
            String beforeStepText = String.valueOf(stepText.getText());
            String[] separatedStrings = beforeStepText.split(" ");
            if(separatedStrings.length >= 3) {
                Long before = Long.valueOf(separatedStrings[3]);


                editor.putLong("Before", before);
                editor.apply();

                //stops the main async
                isCancelled = true;

<<<<<<< HEAD
=======
                Toast.makeText(this, "stopped main ", Toast.LENGTH_SHORT).show();

                fitnessService.stopAsync();
            }
        }
>>>>>>> 09d22ec40cc2745c533f49e2a3e6c5d12dfa7929
    }

    public void updateAll(int total) {
        final TextView stepText = findViewById(R.id.textStepsMain);
        final TextView stepsLeft = findViewById(R.id.stepsLeft);

        long before = currentSteps;


        stepText.setText(String.format(SHOW_STEP, total));
        SharedPreferences pref = getSharedPreferences("user_data", MODE_PRIVATE);
        long stepLeft = goal - total > 0 ? goal - total : 0;
        stepsLeft.setText(String.format(SHOW_STEPS_LEFT, stepLeft));

        currentSteps = total;

        encourage.getEncourgementOnLiveUpdate(currentSteps, before, goal);

        if(currentSteps >= goal && !goalReached) {
            goalReached = true;
            showNewGoalPrompt();
        }

    }
<<<<<<< HEAD

    private Long getLastStepCount() {
=======
>>>>>>> 09d22ec40cc2745c533f49e2a3e6c5d12dfa7929

    private Long getLastStepCount() {
        SharedPreferences sharedPreferences = getSharedPreferences("lastKnownSteps", MODE_PRIVATE);
        Long beforeSteps = sharedPreferences.getLong("Before", 0);
<<<<<<< HEAD

=======
>>>>>>> 09d22ec40cc2745c533f49e2a3e6c5d12dfa7929
        return beforeSteps;
    }

    private long getCurrentSteps() {
        final TextView stepText = findViewById(R.id.textStepsMain);
<<<<<<< HEAD

=======
>>>>>>> 09d22ec40cc2745c533f49e2a3e6c5d12dfa7929
        String beforeStepText = String.valueOf(stepText.getText());
        String[] separatedStrings = beforeStepText.split(" ");
        Long before = 0L;
        if(separatedStrings.length >= 3) {
            before = Long.valueOf(separatedStrings[3]);
        }
        return before;
    }

    public void launchStepCountActivity() {
        if(strideLength == 0) {
            showHeightPrompt();
        } else if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            Toast.makeText(this, "You must login with Google to use this app", Toast.LENGTH_SHORT).show();
            GoogleSignIn.requestPermissions(
                    this, // your activity
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);
        } else {
            Intent intent = new Intent(this, StepCountActivity.class);
            intent.putExtra(StepCountActivity.FITNESS_SERVICE_KEY, fitnessServiceKey);
            intent.putExtra("stride", strideLength);
            startActivityForResult(intent, REQUEST_CODE);
            switchToActive = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
<<<<<<< HEAD
=======
        super.onActivityResult(requestCode, resultCode, data);
        activeDistance = data.getDoubleExtra("distance", 0.0);
        activeSpeed = data.getDoubleExtra("speed", 0.0);
        activeTimeElapsed = data.getIntExtra("time", 0);
        activeSteps = data.getLongExtra("steps", 0);

        Toast.makeText(getApplicationContext(), String.format(TMP_RESULT,
                activeDistance, activeSpeed, activeTimeElapsed, activeSteps),
                Toast.LENGTH_LONG).show();
        // Toast.makeText(this,String.format("distance: %.2f, speed: %.2f", activeDistance, activeSpeed), Toast.LENGTH_LONG).show();
>>>>>>> 09d22ec40cc2745c533f49e2a3e6c5d12dfa7929
        if(switchToActive) {
            super.onActivityResult(requestCode, resultCode, data);
            activeDistance = data.getDoubleExtra("distance", 0.0);
            activeSpeed = data.getDoubleExtra("speed", 0.0);
            activeMin = data.getIntExtra("min", 0);
            activeSec = data.getIntExtra("second", 0);
            activeSteps = data.getLongExtra("steps", 0);
            displayActiveData();
        }

        if(activeSteps >= this.goal) {
            showNewGoalPrompt();
        }

<<<<<<< HEAD
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        if(day == Calendar.SATURDAY) {
            weeklyData = new long[14];
        }

        weeklyData[day + 6] += activeSteps;

        /*
        SharedPreferences sharedPref = getSharedPreferences("weekly_records", MODE_PRIVATE);
        long totalActiveSteps = sharedPref.getLong(String.valueOf(day), 0);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(String.valueOf(day), totalActiveSteps+activeSteps);
        editor.apply();
        */
=======
>>>>>>> 09d22ec40cc2745c533f49e2a3e6c5d12dfa7929
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

    @Override
    public void onFinishEditDialog(String[] inputText) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("magnitude", inputText[0]);
        editor.putString("metric", inputText[1]);

        // Case 1: use centimeter as metric
        if(Integer.parseInt(inputText[0]) == 0 ){
            strideLength = (float) (Integer.parseInt(inputText[1]) / 2.54 * 0.413);
        }
        // Case 2: use feet as metric
        else {
            strideLength = (float) ((Integer.parseInt(inputText[1])*12 + Integer.parseInt(inputText[2])) * 0.413);
        }
        editor.putFloat("stride", strideLength);
        firstTimeUser = strideLength == 0 || GoogleSignIn.getLastSignedInAccount(this) == null;
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
