package edu.ucsd.cse110.googlefitapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.Calendar;

import edu.ucsd.cse110.googlefitapp.fitness.FitnessService;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessServiceFactory;
import edu.ucsd.cse110.googlefitapp.fitness.GoogleFitAdapter;
import edu.ucsd.cse110.googlefitapp.fitness.MainStepCountAdapter;

public class MainActivity extends AppCompatActivity implements HeightPrompter.HeightPrompterListener, CustomGoalSetter.GoalPrompterListener, ManualStepSetter.ManualStepSetterListener {
    private String fitnessServiceKey = "GOOGLE_FIT";
    public static final String MAIN_SERVICE = "MAIN_SERVICE";
    private static final int REQUEST_CODE = 1000;

    public static final String SHOW_STRIDE = "Your estimated stride length is %.2f\"";
//    public static final String SHOW_GOAL = "Your current goal is %d steps.";
//    public static final String SHOW_STEP = "Your have taken %d steps.";
//    public static final String SHOW_STEPS_LEFT = "You have %d steps left.";

    public static final String TMP_RESULT = "distance: %.2f, speed: %.2f, time: %d, steps: %d";

    public static final String SHOW_GOAL = "%d";
    public static final String SHOW_STEP = "%d";
    public static final String SHOW_STEPS_LEFT = "%d";
    public static final String SHARED_PREFERENCE_NAME = "user_data";
    public static final String KEY_MAGNITUDE = "magnitude";
    public static final String KEY_METRIC = "metric";
    public static final String KEY_GOAL = "goal";
    public static final String KEY_HEIGHT = "height";
    public static final String KEY_BEFORE = "before";
    public static final String KEY_STRIDE = "stride";
    public static final int DEFAULT_GOAL = 5000;
    public static boolean firstTimeUser = true;

    private boolean switchToActive = false;
    private int goal;

    private boolean isCancelled = false;
    private boolean goalChangeable = true;
    private boolean canShowHalfEncour = true;
    private boolean canShowOverPrevEncour = true;
    private int currentSteps;

    private float activeDistance;
    private float activeSpeed;
    private int activeMin;
    private int activeSec;
    private int activeSteps = 0;
    private float strideLength;

    private FitnessService fitnessService;
    private Calendar calendar = Calendar.getInstance();
    private double[] weeklyInactiveSteps = new double[7];
    private double[] weeklyActiveSteps = new double[7];

//    //this is only ran when we run the app again (given it is not deleted from the "recent" apps)
//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        if(GoogleSignIn.getLastSignedInAccount(this) != null) {
//            fitnessService.startAsync();
//            fitnessService.setup();
//
//            final TextView stepText = findViewById(R.id.textStepsMain);
//            final int beforeSteps = getLastStepCount();
//
//            int total = getCurrentSteps();
//            encourage.getEncourgementOnLiveUpdate(total, beforeSteps, goal);
//
//            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE);
//            int today = calendar.get(Calendar.DAY_OF_WEEK);
//            int day = sharedPreferences.getInt("day", -1);
//
//            if(day != today) {
//                goalChangable = true;
//                sharedPreferences.edit().putInt("day", today).apply();
//            }
//        }
//    }


    public void setGoalChangeable(boolean goalChangeable) {
        this.goalChangeable = goalChangeable;
    }

    public boolean getGoalChangeable() {
        return this.goalChangeable;
    }

    public void setCanShowHalfEncour(boolean canShowHalfEncour) {
        this.canShowHalfEncour = canShowHalfEncour;
    }

    public boolean getCanShowHalfEncour() {
        return this.canShowHalfEncour;
    }

    public void setCanShowOverPrevEncour(boolean canShowOverPrevEncour) {
        this.canShowOverPrevEncour = canShowOverPrevEncour;
    }

    public boolean getCanShowOverPrevEncour() {
        return this.canShowOverPrevEncour;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // For testing
        if(getIntent().getBooleanExtra("TEST", false)){
            String mainServiceKey = getIntent().getStringExtra("TEST_SERVICE_MAIN");
            String stepCountServiceKey = getIntent().getStringExtra("TEST_SERVICE_STEP_COUNT");
            fitnessService = FitnessServiceFactory.create(mainServiceKey, this);
            setFitnessServiceKey(stepCountServiceKey);
        } else { // Normal setup

            FitnessServiceFactory.put(MAIN_SERVICE, new FitnessServiceFactory.BluePrint() {
                @Override
                public FitnessService create(StepCountActivity stepCountActivity) {
                    return null;
                }

                @Override
                public FitnessService create(MainActivity mainActivity) {
                    return new MainStepCountAdapter(mainActivity);
                }
            });

            FitnessServiceFactory.put(fitnessServiceKey, new FitnessServiceFactory.BluePrint() {
                @Override
                public FitnessService create(StepCountActivity stepCountActivity) {
                    return new GoogleFitAdapter(stepCountActivity);
                }

                @Override
                public FitnessService create(MainActivity mainActivity) {
                    return null;
                }
            });

            fitnessService = FitnessServiceFactory.create(MAIN_SERVICE, this);
        }

        fitnessService.setup();

        fitnessService.startAsync();

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("graphNotCleared", true).apply();
        String magnitude = sharedPreferences.getString(KEY_MAGNITUDE, "");
        String metric = sharedPreferences.getString(KEY_METRIC, "");
        strideLength = sharedPreferences.getFloat(KEY_STRIDE, 0);
        editor.putBoolean("graphNotCleared", true);

        firstTimeUser = strideLength == 0 || GoogleSignIn.getLastSignedInAccount(this) == null;
        this.goal = sharedPreferences.getInt(KEY_GOAL, DEFAULT_GOAL);

        // Update goal
        int currentGoal = sharedPreferences.getInt(KEY_GOAL, -1);
        if( currentGoal == -1 ){
            editor.putInt(KEY_GOAL, DEFAULT_GOAL);
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
        final int beforeSteps = getLastStepCount();

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

        // Users can customize their goals
        Button setStepbtn = findViewById(R.id.btnSetStep);
        setStepbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomStepPrompt();
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
        Button btnGoToWeekly = findViewById(R.id.weeklyButton);
        System.out.println("started bar");

        btnGoToWeekly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchWeeklyStats();
            }
        });

        int today = calendar.get(Calendar.DAY_OF_WEEK);
        int day = sharedPreferences.getInt("day", -1);

        if(day != today) {
            goalChangeable = true;
            canShowHalfEncour = true;
            canShowOverPrevEncour = true;
            sharedPreferences.edit().putInt("day", today).apply();
        }
    }

    public void launchWeeklyStats() {
        Intent intent = new Intent(MainActivity.this, WeeklyStats.class);
        startActivity(intent);
    }

//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        if(GoogleSignIn.getLastSignedInAccount(this) != null) {
//            SharedPreferences sharedPreferences = getSharedPreferences("lastKnownSteps", MODE_PRIVATE);
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//
//            final TextView stepText = findViewById(R.id.textStepsMain);
//
//            //get the steps that were there on the UI before we got the new steps
//            String beforeStepText = String.valueOf(stepText.getText());
//            String[] separatedStrings = beforeStepText.split(" ");
//            if(separatedStrings.length >= 3) {
//                int before = Integer.valueOf(separatedStrings[3]);
//
//                editor.putInt(KEY_BEFORE, before);
//                editor.apply();
//
//                //stops the main async
//                isCancelled = true;
//                //Toast.makeText(this, "stopped main ", Toast.LENGTH_SHORT).show();
//                fitnessService.stopAsync();
//            }
//        }
//    }

    public void updateAll(int total) {
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        clearGraph(day);

        // store total steps
        SharedPreferences sharedPref = getSharedPreferences("weekly_steps", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(String.valueOf(day), total);
        editor.putInt("goal", goal);
        editor.apply();
        Log.d("MAIN", "Total steps up to now: " + total);

        // store total dist
        SharedPreferences statsPref = getSharedPreferences("weekly_data", MODE_PRIVATE);
        SharedPreferences.Editor statsEditor = statsPref.edit();
        statsEditor.putFloat(String.valueOf(day + 7), total * strideLength / 63360.0f);
        statsEditor.apply();

        final TextView stepText = findViewById(R.id.textStepsMain);
        final TextView stepsLeft = findViewById(R.id.stepsLeft);

        currentSteps = total;
        int stepLeft = goal - total > 0 ? goal - total : 0;

        stepText.setText(String.format(SHOW_STEP, total));
        stepsLeft.setText(String.format(SHOW_STEPS_LEFT, stepLeft));

        if(currentSteps >= goal && goalChangeable) {
            goalChangeable = false;
            showNewGoalPrompt();
        }
    }

    private int getLastStepCount() {
        SharedPreferences sharedPreferences = getSharedPreferences("lastKnownSteps", MODE_PRIVATE);
        int beforeSteps = sharedPreferences.getInt("Before", 0);
        return beforeSteps;
    }

    public void launchStepCountActivity() {
        if(!fitnessService.hasPermission()){
            fitnessService.setup();
        } else if(strideLength == 0) {
            showHeightPrompt();
        } else {
            Intent intent = new Intent(this, StepCountActivity.class);
            intent.putExtra(StepCountActivity.FITNESS_SERVICE_KEY, fitnessServiceKey);
            intent.putExtra("stride", strideLength);
            startActivityForResult(intent, REQUEST_CODE);
            switchToActive = true;
            fitnessService.stopAsync();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User pressed the return button on their device -- no extras on intent
        if(data == null)
            return;

        // Firstly it fetches active data from StepCountActivity
        if(switchToActive) {
            super.onActivityResult(requestCode, resultCode, data);
            activeDistance = data.getFloatExtra("distance", 0.0f);
            activeSpeed = data.getFloatExtra("speed", 0.0f);
            activeMin = data.getIntExtra("min", 0);
            activeSec = data.getIntExtra("second", 0);
            activeSteps = data.getIntExtra("steps", 0);
            displayActiveData();
        }

        // Then, store the active data into local storage
        // Note that if the date is Saturday, a new cycle will start, so also weekly data are cleared
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        clearGraph(day);

        // update active steps
        SharedPreferences stepPref = getSharedPreferences("weekly_steps", MODE_PRIVATE);
        int totalActiveSteps = stepPref.getInt(String.valueOf(day + 7), 0) + activeSteps; // Store active steps
        SharedPreferences.Editor editor = stepPref.edit();
        editor.putInt(String.valueOf(day + 7), totalActiveSteps);
        editor.apply();

        // update avg speed and total distance
        SharedPreferences statsPref = getSharedPreferences("weekly_data", MODE_PRIVATE);
        float currActiveSpeed = statsPref.getFloat(String.valueOf(day), 0.0f);
        float totalActiveDist = totalActiveSteps * strideLength / 63360.0f;
        SharedPreferences.Editor statsEditor = statsPref.edit();
        statsEditor.putFloat(String.valueOf(day), (currActiveSpeed + activeSpeed)/2.0f);
        statsEditor.putFloat(String.valueOf(day+14), totalActiveDist);
        statsEditor.apply();

        if(activeSteps >= this.goal && goalChangeable) { // this.goal is steps remaining
            goalChangeable = false; // Goal is only allowed to be set once in a week
            showNewGoalPrompt();
        }

        if(currentSteps > getSharedPreferences(MainActivity.SHARED_PREFERENCE_NAME,
                MODE_PRIVATE).getInt(MainActivity.KEY_GOAL, 0) / 2 && canShowHalfEncour){
            setCanShowHalfEncour(false);
            showAchieveHalfEncouragement();
        }

        int yesterday = day - 1 >= 0 ? day - 1 : 6;
        if(currentSteps > getSharedPreferences("weekly_steps",
                MODE_PRIVATE).getInt(String.valueOf(yesterday), 0) + 1000 && canShowOverPrevEncour) {
            showOverPrevEncouragement();
        }

        // Finally, update total steps, and display it on UI
        fitnessService.updateStepCount();
        fitnessService.startAsync();
        fitnessService.addActiveSteps(activeSteps);
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

    private void showCustomStepPrompt() {
        FragmentManager fm = getSupportFragmentManager();
        ManualStepSetter setSetpDialogFragment = ManualStepSetter.newInstance(getString(R.string.stepPrompt));
        setSetpDialogFragment.show(fm, "fragment_set_goal");
    }

    public void showNewGoalPrompt() {
        setGoalChangeable(false);
        FragmentManager fm = getSupportFragmentManager();
        NewGoalSetter setGoalDialogFragment = NewGoalSetter.newInstance(getString(R.string.congratsPrompt), goal);
        setGoalDialogFragment.show(fm, "fragment_set_new_goal");
    }

    public void showAchieveHalfEncouragement() {
        setCanShowHalfEncour(false);
        Toast.makeText(this, "Good Job! You have finished half of your goal!", Toast.LENGTH_LONG).show();
    }

    public void showOverPrevEncouragement() {
        setCanShowOverPrevEncour(false);
        Toast.makeText(this, "Congratulation! You have 1000 steps more than yesterday!", Toast.LENGTH_LONG).show();
    }

    private void displayActiveData() {
        FragmentManager fm = getSupportFragmentManager();
        DataDisplayer dataDisplayer = DataDisplayer.newInstance(getString(R.string.prevSession), activeDistance, activeSpeed, activeSteps, activeMin, activeSec);
        dataDisplayer.show(fm, "fragment_display_active_data");
    }

    @Override
    public void onFinishEditDialog(String[] inputText) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_MAGNITUDE, inputText[0]);
        editor.putString(KEY_METRIC, inputText[1]);

        // Case 1: use centimeter as metric
        if(Integer.parseInt(inputText[0]) == 0 ){
            strideLength = (float) (Integer.parseInt(inputText[1]) / 2.54 * 0.413);
        }
        // Case 2: use feet as metric
        else {
            strideLength = (float) ((Integer.parseInt(inputText[1])*12 + Integer.parseInt(inputText[2])) * 0.413);
        }
        editor.putFloat(KEY_STRIDE, strideLength);
        editor.apply();

        Toast.makeText(this, "Height saved", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, String.format(SHOW_STRIDE, strideLength), Toast.LENGTH_LONG).show();
    }

    //when we are done with the new goal
    @Override
    public void onFinishEditDialog(int goal) {
        TextView goalText = findViewById(R.id.textGoal);
        goalText.setText(String.format(SHOW_GOAL, goal));

        // Save new goal
        this.goal = goal;

        fitnessService.updateStepCount();

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_GOAL, goal);
        editor.apply();
    }

    private void clearGraph(int day) {
        SharedPreferences stepPref = getSharedPreferences("weekly_steps", MODE_PRIVATE);
        SharedPreferences statsPref = getSharedPreferences("weekly_data", MODE_PRIVATE);
        SharedPreferences sharedPref = getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        boolean notCleared = sharedPref.getBoolean("graphNotCleared", true);

        if(day == Calendar.SUNDAY) {
            if (notCleared) {
                sharedPref.edit().putBoolean("graphNotCleared", false).apply();
                stepPref.edit().clear().apply();
                statsPref.edit().clear().apply();
            }
        } else {
            sharedPref.edit().putBoolean("graphNotCleared", true).apply();
        }
    }

    @Override
    public void onFinishEditDialog(int[] inputStep) {
        fitnessService.addInactiveSteps(inputStep[0]);
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public void mockCalendar(View view){
        System.out.println(fitnessService.getLast7DaysSteps(weeklyInactiveSteps, weeklyActiveSteps, Calendar.getInstance()));
    }

}
