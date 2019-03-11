package edu.ucsd.cse110.googlefitapp;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.style.FadingCircle;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import edu.ucsd.cse110.googlefitapp.adapter.PlannedWalkAdapter;
import edu.ucsd.cse110.googlefitapp.adapter.UnplannedWalkAdapter;
import edu.ucsd.cse110.googlefitapp.dialog.CustomGoalDialog;
import edu.ucsd.cse110.googlefitapp.dialog.HeightDialog;
import edu.ucsd.cse110.googlefitapp.dialog.ManuallyEnterStepDialog;
import edu.ucsd.cse110.googlefitapp.dialog.NewGoalDialog;
import edu.ucsd.cse110.googlefitapp.dialog.PlannedWalkEndingDialog;
import edu.ucsd.cse110.googlefitapp.firebase.ChatMessaging;
import edu.ucsd.cse110.googlefitapp.firebase.FirebaseMessageToChatMessageAdapter;
import edu.ucsd.cse110.googlefitapp.firebase.TestMessaging;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessService;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessServiceFactory;
import edu.ucsd.cse110.googlefitapp.fitness.GoogleFitnessServiceFactory;
import edu.ucsd.cse110.googlefitapp.mock.StepCalendar;
import edu.ucsd.cse110.googlefitapp.observer.EncouragementDisplay;
import edu.ucsd.cse110.googlefitapp.observer.GoalDisplay;
import edu.ucsd.cse110.googlefitapp.observer.GraphDisplay;
import edu.ucsd.cse110.googlefitapp.observer.Observer;
import edu.ucsd.cse110.googlefitapp.chatroom.views.LoginActivity;
import edu.ucsd.cse110.googlefitapp.observer.StepDisplay;

import static edu.ucsd.cse110.googlefitapp.adapter.UnplannedWalkAdapter.RC_SIGN_IN;

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
    private static final int ACTIVE_SESSION_REQUEST_CODE = 1000;
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
    private DrawerLayout drawerLayout;
    public static final String DOCUMENT_KEY = "public_ntfcn";
    private ChatMessaging chatMessaging;


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

    public void setGoal(int goal){
        this.goal = goal;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        super.onResume();
        // fitnessService.setup();
    }


    @SuppressLint({"DefaultLocale", "ClickableViewAccessibility"})
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
            chatMessaging = new TestMessaging();
        } else { // Normal setup
            fitnessServiceFactory.put(MAIN_SERVICE, UnplannedWalkAdapter::new);

            fitnessServiceFactory.put(fitnessServiceKey, PlannedWalkAdapter::new);
//            fitnessServiceFactory.put("WEEKLY_STATS", WeeklyStatsAdapter::new);

            fitnessService = fitnessServiceFactory.create(MAIN_SERVICE, this);
            chatMessaging = new FirebaseMessageToChatMessageAdapter();
            subscribeToNotificationsTopic(chatMessaging);
        }

        ProgressBar progressBarLeft = (ProgressBar)findViewById(R.id.spin_kit_steps_left);
        FadingCircle wave1 = new FadingCircle();
        wave1.setColor(Color.parseColor("#90000000"));
        progressBarLeft.setIndeterminateDrawable(wave1);
        findViewById(R.id.stepsLeft).setVisibility(View.INVISIBLE);

        ProgressBar progressBarTotal = (ProgressBar)findViewById(R.id.spin_kit_steps_taken);
        FadingCircle wave2 = new FadingCircle();
        wave2.setColor(Color.parseColor("#ff00ddff"));
        progressBarTotal.setIndeterminateDrawable(wave2);
        findViewById(R.id.textStepsMain).setVisibility(View.INVISIBLE);


        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.parseColor("#008577"));
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        TextView mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        mTitle.setText(getString(R.string.PersonalBest));

        ImageView friendHint = findViewById(R.id.hintFriend);
        friendHint.setVisibility(View.INVISIBLE);

        ImageView addFriend = findViewById(R.id.friend_18);
        addFriend.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        ImageView view = (ImageView) v;
                        //overlay is black with transparency of 0x77 (119)
                        view.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL: {
                        ImageView view = (ImageView) v;
                        //clear the overlay
                        view.getDrawable().clearColorFilter();
                        view.invalidate();
                        break;
                    }
                }

                return false;
            }
        });
//        addFriend.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_item));
        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchFriendSignUpActivity();
            }
        });

        Button chatBtn = findViewById(R.id.chatroom);
        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lauchChatroomActivity();
            }
        });


        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
//                        menuItem.setChecked(true);
                        Log.e(TAG, "Menu Item selected: " + menuItem.getTitle() + "," + menuItem.getMenuInfo());
//                        // TODO: change logic here. Also, allow dynamically adding items to friend_24 list. Also, HIGHLIGHT unread messages
//                        switch (menuItem.getItemId()) {
//                            case R.id.gary:
//                            case R.id.wgg:
//                            case R.id.rick:
//                            case R.id.politz:
                        Intent intent = new Intent(MainActivity.this, FriendChatActivity.class);
                        startActivity(intent);
//                        }
                        //close navigation drawer
                        // close drawer when item is tapped
                        // TODO: if you want to use a custom dialog for chatting, then may need not close the drawer
                        drawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });

        drawerLayout.addDrawerListener(
                new DrawerLayout.DrawerListener() {
                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        // Respond when the drawer's position changes
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        // Respond when the drawer is opened
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        // Respond when the drawer is closed
                    }

                    @Override
                    public void onDrawerStateChanged(int newState) {
                        // Respond when the drawer motion state changes
                    }
                }
        );

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.friendlist);
//        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

//        setUpFriendlist();

        fitnessService.setup();

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

        btnGoToWeekly.setOnClickListener(v -> launchWeeklyStats());

        checkForDayChange();

        new GoalDisplay(this);
        new StepDisplay(this);
        new EncouragementDisplay(this);
        new GraphDisplay(this);
    }


    private void launchFriendSignUpActivity() {
        Intent intent = new Intent(MainActivity.this, NewFriendSignUpActivity.class);
        intent.putExtra("uid", fitnessService.getUID());
        intent.putExtra("email", fitnessService.getEmail());
        Log.d(TAG, "Async stopped");
        startActivity(intent);
        overridePendingTransition(R.anim.slide_r_to_l_enter, R.anim.slide_r_to_l_exit);
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
            Log.d(TAG, getString(R.string.launchWeeklyStatsSuccess));

        } catch (Exception e) {
            Log.d(TAG, getString(R.string.launchWeeklyStatsFailure) + e.toString());
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
            startActivityForResult(intent, ACTIVE_SESSION_REQUEST_CODE);
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
        if (requestCode == ACTIVE_SESSION_REQUEST_CODE) {
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
        } else if(requestCode == RC_SIGN_IN ){
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
//            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
//            updateUI(null);
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

        fitnessService.addActiveSteps(activeSteps, activeMin, activeSec, strideLength);
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
        setStepDialogFragment.show(fm, "fragment_set_step");
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
//        System.out.println(fitnessService.getLast7DaysSteps(weeklyInactiveSteps, weeklyActiveSteps,
//                Calendar.getInstance()));
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
    private void subscribeToNotificationsTopic(ChatMessaging chatMessaging) {
        chatMessaging.subscribe(this);
    }

    private void lauchChatroomActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_r_to_l_enter, R.anim.slide_r_to_l_exit);
    }
}
