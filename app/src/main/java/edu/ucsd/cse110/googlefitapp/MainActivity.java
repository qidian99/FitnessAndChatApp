package edu.ucsd.cse110.googlefitapp;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import edu.ucsd.cse110.googlefitapp.adapter.PlannedWalkAdapter;
import edu.ucsd.cse110.googlefitapp.adapter.UnplannedWalkAdapter;
import edu.ucsd.cse110.googlefitapp.dialog.CustomGoalDialog;
import edu.ucsd.cse110.googlefitapp.dialog.HeightDialog;
import edu.ucsd.cse110.googlefitapp.dialog.ManuallyEnterStepDialog;
import edu.ucsd.cse110.googlefitapp.dialog.NewGoalDialog;
import edu.ucsd.cse110.googlefitapp.dialog.PlannedWalkEndingDialog;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessService;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessServiceFactory;
import edu.ucsd.cse110.googlefitapp.fitness.GoogleFitnessServiceFactory;
import edu.ucsd.cse110.googlefitapp.mock.StepCalendar;
import edu.ucsd.cse110.googlefitapp.observer.EncouragementDisplay;
import edu.ucsd.cse110.googlefitapp.observer.GoalDisplay;
import edu.ucsd.cse110.googlefitapp.observer.GraphDisplay;
import edu.ucsd.cse110.googlefitapp.observer.Observer;
import edu.ucsd.cse110.googlefitapp.observer.StepDisplay;

public class MainActivity extends Activity implements HeightDialog.HeightPrompterListener,
        CustomGoalDialog.GoalPrompterListener, ManuallyEnterStepDialog.ManualStepSetterListener,
        DatePickerDialog.OnDateSetListener {
    public static final String MAIN_SERVICE = "MAIN_SERVICE";
    public static final String SHOW_STRIDE = "Your estimated stride length is %.2f\"";
    public static final String SHOW_GOAL = "%d";
    public static final String SHOW_STEP = "%d";
    public static final String SHOW_STEPS_LEFT = "%d";
    public static final String SHARED_PREFERENCE_NAME = "user_data";
    public static final String KEY_MAGNITUDE = "magnitude";
    public static final String KEY_METRIC = "metric";
    public static final String KEY_GOAL = "goal";
    public static final String KEY_STRIDE = "stride";
    public static final int DEFAULT_GOAL = 5000;
    public static final String TAG = "MAIN";
    private static final int REQUEST_CODE = 1000;
    public static boolean firstTimeUser = true;
    public static FitnessServiceFactory fitnessServiceFactory = new GoogleFitnessServiceFactory();
    public static Calendar calendar = StepCalendar.getInstance();
    SharedPreferences stepPref;
    SharedPreferences statsPref;
    SharedPreferences sharedPref;
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private String fitnessServiceKey = "GOOGLE_FIT";
    private boolean switchToActive = false;
    private boolean goalChangeable = false;
    private boolean canShowHalfEncouragement = false;
    private boolean canShowOverPrevEncouragement = false;
    private int goal;
    private float activeDistance;
    private float activeSpeed;
    private int activeMin;
    private int activeSec;
    private int activeSteps = 0;
    private float strideLength;
    private FitnessService fitnessService;
    private double[] weeklyInactiveSteps = new double[7];
    private double[] weeklyActiveSteps = new double[7];
    private ArrayList<Observer> observers = new ArrayList<>();
    private int currentStep;
    private int day;
    private int today;
    private boolean notCleared;

    public SharedPreferences getStepPref() {
        return stepPref;
    }

    public SharedPreferences getStatsPref() {
        return statsPref;
    }

    public SharedPreferences getSharedPref() {
        return sharedPref;
    }

    public float getStrideLength() {
        return strideLength;
    }

    public boolean getGoalChangeable() {
        return this.goalChangeable;
    }

    public void setGoalChangeable(boolean goalChangeable) {
        this.goalChangeable = goalChangeable;
    }

    public boolean getCanShowHalfEncouragement() {
        return this.canShowHalfEncouragement;
    }

    public void setCanShowHalfEncouragement(boolean canShowHalfEncouragement) {
        this.canShowHalfEncouragement = canShowHalfEncouragement;
    }

    public boolean getCanShowOverPrevEncouragement() {
        return this.canShowOverPrevEncouragement;
    }

    public void setCanShowOverPrevEncouragement(boolean canShowOverPrevEncouragement) {
        this.canShowOverPrevEncouragement = canShowOverPrevEncouragement;
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // For testing
        if (getIntent().getBooleanExtra("TEST", false)) {
            String mainServiceKey = getIntent().getStringExtra("TEST_SERVICE_MAIN");
            String stepCountServiceKey = getIntent().getStringExtra("TEST_SERVICE_STEP_COUNT");
            fitnessService = fitnessServiceFactory.create(mainServiceKey, this);
            setFitnessServiceKey(stepCountServiceKey);
        } else { // Normal setup
            fitnessServiceFactory.put(MAIN_SERVICE, UnplannedWalkAdapter::new);

            fitnessServiceFactory.put(fitnessServiceKey, PlannedWalkAdapter::new);

            fitnessService = fitnessServiceFactory.create(MAIN_SERVICE, this);
        }

        fitnessService.setup();
        fitnessService.startAsync();

        stepPref = getSharedPreferences("weekly_steps", MODE_PRIVATE);
        statsPref = getSharedPreferences("weekly_data", MODE_PRIVATE);
        sharedPref = getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        strideLength = sharedPref.getFloat(KEY_STRIDE, 0);

        firstTimeUser = strideLength == 0 || GoogleSignIn.getLastSignedInAccount(this) == null;
        this.goal = sharedPref.getInt(KEY_GOAL, DEFAULT_GOAL);

        // Update goal
        int currentGoal = sharedPref.getInt(KEY_GOAL, -1);
        if (currentGoal == -1) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(KEY_GOAL, DEFAULT_GOAL);
            editor.apply();
            currentGoal = DEFAULT_GOAL;
        }
        TextView goalText = findViewById(R.id.textGoal);
        goalText.setText(String.format(SHOW_GOAL, currentGoal));

        // In development, we allow users to re-enter their heights
        Button setHeightBtn = findViewById(R.id.setHeightBtn);
        setHeightBtn.setOnClickListener(v -> showHeightPrompt());

        // Users can customize their goals
        Button setGoalBtn = findViewById(R.id.btnSetGoal);
        setGoalBtn.setOnClickListener(v -> showCustomGoalPrompt());

        // Users can manually enter their steps
        Button setStepBtn = findViewById(R.id.btnSetStep);
        setStepBtn.setOnClickListener(v -> showCustomStepPrompt());

        // Users can roll back the date
        Button setDateBtn = findViewById(R.id.mockCalBtn);
        Calendar tempCal = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this, MainActivity.this, tempCal.get(Calendar.YEAR),
                tempCal.get(Calendar.MONTH), tempCal.get(Calendar.DATE));

        setDateBtn.setOnClickListener(v -> {
            datePickerDialog.show();
            Log.d(TAG, "datePickDialog start success");
        });

        // Start an active session
        Button btnGoToSteps = findViewById(R.id.startBtn);
        btnGoToSteps.setOnClickListener(v -> launchStepCountActivity());

        // Go to the bar chart activity.
        Button btnGoToWeekly = findViewById(R.id.weeklyButton);
        System.out.println("started bar");

        btnGoToWeekly.setOnClickListener(v -> launchWeeklyStats());

        checkForDayChange();

        new GoalDisplay(this);
        new StepDisplay(this);
        new EncouragementDisplay(this);
        new GraphDisplay(this);
    }

    private void checkForDayChange() {
        int today = calendar.get(Calendar.DAY_OF_WEEK);
        int day = sharedPref.getInt("day", -1);

        if (day != today) {
            goalChangeable = true;
            canShowHalfEncouragement = true;
            canShowOverPrevEncouragement = true;
            sharedPref.edit().putInt("day", today).apply();
        }
    }

    public void launchWeeklyStats() {
        try {
            Intent intent = new Intent(MainActivity.this, WeeklyStatsActivity.class);
            startActivity(intent);
            Log.d(TAG, getString(R.string.lauchWeeklyStatsSuccess));

        } catch (Exception e) {
            Log.d(TAG, getString(R.string.lauchWeeklyStatsFailure) + e.toString());
            e.printStackTrace();
        }
    }

    public void updateAll(int total) {
    }

    @Override
    public void setStep(int currentStep) {
        this.currentStep = currentStep;
    }

    public void launchStepCountActivity() {
        if (!fitnessService.hasPermission()) {
            fitnessService.setup();
        } else if (strideLength == 0) {
            showHeightPrompt();
        } else {
            Intent intent = new Intent(this, PlannedWalkActivity.class);
            intent.putExtra(PlannedWalkActivity.FITNESS_SERVICE_KEY, fitnessServiceKey);
            intent.putExtra("stride", strideLength);
            startActivityForResult(intent, REQUEST_CODE);
            switchToActive = true;
            fitnessService.stopAsync();
            Log.d(TAG, "Async stopped");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User pressed the return button on their device -- no extras on intent
        if (data == null)
            return;

        Log.d(TAG, "switch back to main activity success");

        // Firstly it fetches active data from PlannedWalkActivity
        if (switchToActive) {
            super.onActivityResult(requestCode, resultCode, data);
            stepPref = getSharedPreferences("weekly_steps", MODE_PRIVATE);
            statsPref = getSharedPreferences("weekly_data", MODE_PRIVATE);
            sharedPref = getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE);

            activeDistance = data.getFloatExtra("distance", 0.0f);
            activeSpeed = data.getFloatExtra("speed", 0.0f);
            activeMin = data.getIntExtra("min", 0);
            activeSec = data.getIntExtra("second", 0);
            activeSteps = data.getIntExtra("steps", 0);
            displayActiveData();

            // Then, store the active data into local storage
            // Note that if the date is Saturday, a new cycle will start, so also weekly data are cleared
            today = calendar.get(Calendar.DAY_OF_WEEK);
            UpdateActiveSteps();

            // Finally, update total steps, and display it on UI
            fitnessService.updateStepCount();
            fitnessService.startAsync();
        }
    }

    private void UpdateActiveSteps() {
        // update active steps
        // Store active steps
        int totalActiveSteps = stepPref.getInt(String.valueOf(day + 7), 0) + activeSteps;
        SharedPreferences.Editor editor = stepPref.edit();
        editor.putInt(String.valueOf(day + 7), totalActiveSteps);
        editor.apply();

        // update avg speed and total distance
        float currActiveSpeed = statsPref.getFloat(String.valueOf(day), 0.0f);
        float totalActiveDist = totalActiveSteps * strideLength / 63360.0f;
        Log.d(TAG, "Today's total active distance: " + totalActiveDist);

        SharedPreferences.Editor statsEditor = statsPref.edit();
        statsEditor.putFloat(String.valueOf(day), (currActiveSpeed + activeSpeed) / 2.0f);
        Log.d(TAG, "Today's average active speed: " + (currActiveSpeed + activeSpeed) / 2.0f);

        statsEditor.putFloat(String.valueOf(day + 14), totalActiveDist);
        statsEditor.apply();

        currentStep += activeSteps;
        if (currentStep >= this.goal && goalChangeable) { // this.goal is steps remaining
            goalChangeable = false; // Goal is only allowed to be set once in a week
            showNewGoalPrompt();
        }
    }

    public void setFitnessServiceKey(String fitnessServiceKey) {
        this.fitnessServiceKey = fitnessServiceKey;
    }

    private void showHeightPrompt() {
        FragmentManager fm = getSupportFragmentManager();
        HeightDialog editNameDialogFragment = HeightDialog.newInstance(getString(R.string.heightPrompt));
        editNameDialogFragment.show(fm, "fragment_prompt_height");
    }

    private void showCustomGoalPrompt() {
        FragmentManager fm = getSupportFragmentManager();
        CustomGoalDialog setGoalDialogFragment = CustomGoalDialog.newInstance(getString(R.string.setGoalPrompt));
        setGoalDialogFragment.show(fm, "fragment_set_goal");
    }

    private void showCustomStepPrompt() {
        FragmentManager fm = getSupportFragmentManager();
        ManuallyEnterStepDialog setStepDialogFragment = ManuallyEnterStepDialog.newInstance(getString(R.string.stepPrompt));
        setStepDialogFragment.show(fm, "fragment_set_goal");
    }

    public void showNewGoalPrompt() {
        setGoalChangeable(false);
        FragmentManager fm = getSupportFragmentManager();
        NewGoalDialog setGoalDialogFragment = NewGoalDialog.newInstance(getString(R.string.congratsPrompt), goal);
        setGoalDialogFragment.show(fm, "fragment_set_new_goal");
    }

    public void showAchieveHalfEncouragement() {
        setCanShowHalfEncouragement(false);
        Toast.makeText(this, "Good Job! You have finished half of your goal!",
                Toast.LENGTH_LONG).show();
    }

    public void showOverPrevEncouragement() {
        setCanShowOverPrevEncouragement(false);
        Toast.makeText(this, "Congratulation! You have 1000 steps more than yesterday!",
                Toast.LENGTH_LONG).show();
    }

    private void displayActiveData() {
        FragmentManager fm = getSupportFragmentManager();
        PlannedWalkEndingDialog plannedWalkEndingDialog = PlannedWalkEndingDialog.newInstance(getString(R.string.prevSession), activeDistance,
                activeSpeed, activeSteps, activeMin, activeSec);
        plannedWalkEndingDialog.show(fm, "fragment_display_active_data");
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onFinishEditDialog(String[] inputText) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_MAGNITUDE, inputText[0]);
        editor.putString(KEY_METRIC, inputText[1]);

        // Case 1: use centimeter as metric
        if (Integer.parseInt(inputText[0]) == 0) {
            strideLength = (float) (Integer.parseInt(inputText[1]) / 2.54 * 0.413);
        }
        // Case 2: use feet as metric
        else {
            strideLength = (float) ((Integer.parseInt(inputText[1]) * 12 +
                    Integer.parseInt(inputText[2])) * 0.413);
        }
        editor.putFloat(KEY_STRIDE, strideLength);
        editor.apply();

        Toast.makeText(this, "Height saved", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, String.format(SHOW_STRIDE, strideLength),
                Toast.LENGTH_LONG).show();
    }

    //when we are done with the new goal
    @SuppressLint("DefaultLocale")
    @Override
    public void onFinishEditDialog(int goal) {
        TextView goalText = findViewById(R.id.textGoal);
        goalText.setText(String.format(SHOW_GOAL, goal));

        // Save new goal
        this.goal = goal;

        fitnessService.updateStepCount();

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_NAME,
                MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_GOAL, goal);
        editor.apply();
    }

    @Override
    public void onFinishEditDialog(int[] inputStep) {
        fitnessService.addInactiveSteps(inputStep[0]);
    }

    public void setCalendar(Calendar calendar) {
        MainActivity.calendar = calendar;
    }

    public void mockCalendar(View view) {
        System.out.println(fitnessService.getLast7DaysSteps(weeklyInactiveSteps, weeklyActiveSteps,
                Calendar.getInstance()));
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        StepCalendar.set(year, month, dayOfMonth);
        calendar = StepCalendar.getInstance();
        dateFormat.setCalendar(calendar);
        Log.d(TAG, "New year: " + year + ", New month: " + (month + 1) + ", New date: "
                + dayOfMonth
                + ", New day of Week: " + calendar.get(Calendar.DAY_OF_WEEK));

        ((TextView) findViewById(R.id.textCal)).setText(dateFormat.format(calendar.getTime()));
        fitnessService.updateStepCount();
    }

    @Override
    public void registerObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    public void setDay(int day) {
        this.day = day;
        getSharedPreferences(MainActivity.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE).edit().putInt("day", today).apply();
    }

    @SuppressLint("CommitPrefEdits")
    public void setNotCleared(boolean notCleared) {
        this.notCleared = notCleared;
        sharedPref.edit().putBoolean("graphNotCleared", notCleared);
    }

    @Override
    public void notifyObservers() {
        today = calendar.get(Calendar.DAY_OF_WEEK);
        day = getSharedPreferences(MainActivity.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE).getInt("day", -1);
        goal = getSharedPreferences(MainActivity.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE).getInt(MainActivity.KEY_GOAL, 0);
        int yesterday = today - 1 >= 0 ? today - 1 : 6;
        int lastStep = getSharedPreferences("weekly_steps", Context.MODE_PRIVATE).getInt(String.valueOf(yesterday), 0);
        notCleared = sharedPref.getBoolean("graphNotCleared", true);

        for (int i = 0; i < observers.size(); i++) {
            Observer observer = observers.get(i);
            observer.update(currentStep, lastStep, goal, day, yesterday, today, notCleared);
        }
    }
}
