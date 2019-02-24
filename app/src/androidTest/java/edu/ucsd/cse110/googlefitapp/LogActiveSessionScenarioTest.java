package edu.ucsd.cse110.googlefitapp;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.matcher.RootMatchers;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.fitness.request.DataReadRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

import edu.ucsd.cse110.googlefitapp.fitness.FitnessService;
import edu.ucsd.cse110.googlefitapp.fitness.GoogleFitnessServiceFactory;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsAnything.anything;
import static org.hamcrest.core.StringContains.containsString;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class LogActiveSessionScenarioTest {
    /*
        Note that since our users have to enter their heights to start an active session,
        we will assume the user enter their heights as 160cm before they start the active
        session.
     */
    private static final String TEST_SERVICE_MAIN_ACTIVITY = "TEST_SERVICE_MAIN_ACTIVITY";
    private static final String TEST_SERVICE_STEP_ACTIVITY = "TEST_SERVICE_STEP_ACTIVITY";

    private ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<MainActivity>(MainActivity.class){
        @Override
        protected void beforeActivityLaunched() {
            clearSharedPrefs(InstrumentationRegistry.getTargetContext());
            super.beforeActivityLaunched();
        }
    };

    private ActivityTestRule<PlannedWalkActivity> mStepCountActivityTestRule = new ActivityTestRule<>(PlannedWalkActivity.class);


    @Before
    public void setup() {
        GoogleFitnessServiceFactory.put(TEST_SERVICE_MAIN_ACTIVITY, new GoogleFitnessServiceFactory.BluePrint() {
            @Override
            public FitnessService create(PlannedWalkActivity plannedWalkActivity) {
                return null;
            }

            @Override
            public FitnessService create(MainActivity mainActivity) {
                return new TestMainFitnessService(mainActivity);
            }
        });
        GoogleFitnessServiceFactory.put(TEST_SERVICE_STEP_ACTIVITY, new GoogleFitnessServiceFactory.BluePrint() {
            @Override
            public FitnessService create(PlannedWalkActivity plannedWalkActivity) {
                return new TestStepCountFitnessService(plannedWalkActivity);
            }

            @Override
            public FitnessService create(MainActivity mainActivity) {
                return null;
            }
        });

        Intents.init();
        Intent intent = new Intent();
        intent.putExtra("TEST", true);
        intent.putExtra("TEST_SERVICE_MAIN", TEST_SERVICE_MAIN_ACTIVITY);
        intent.putExtra("TEST_SERVICE_STEP_COUNT", TEST_SERVICE_STEP_ACTIVITY);
        mActivityTestRule.launchActivity(intent);
        mActivityTestRule.getActivity().setFitnessServiceKey(TEST_SERVICE_STEP_ACTIVITY);

        onView(withId(R.id.startBtn))
                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))));
        onView(withId(R.id.startBtn)).perform(click());
        onView(withText(R.string.heightPrompt))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withId(R.id.metricSpinner)).perform(click());
        onData(anything()).inRoot(RootMatchers.isPlatformPopup()).atPosition(0).perform(click());

        onView(withId(R.id.metricSpinner)).check(matches(withSpinnerText(containsString("cm"))));
        onView(withId(R.id.cent_height))
                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))));
        onView(withId(R.id.ft_height))
                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.GONE))));
        onView(withId(R.id.inch_height))
                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.GONE))));

        onView(withId(R.id.cent_height)).perform(typeText(String.valueOf(160)));

        onView(withId(R.id.posBtn)).perform(click());

        intended(hasComponent(new ComponentName(getTargetContext(), MainActivity.class)));
    }

    @After
    public void tearDown() {
        mActivityTestRule.getActivity().finish();
        Intents.release();
    }


    /*
      Feature: Way to Log intended active session (Start/End Button)
      Scenario 1: User presses start button
        Given that Sarah wants to start her workout
        And she has the app started and running
        When she presses the Start Walk/Run button
        Then a new session will activate
        And will record her steps starting at 0.
     */
    @Test
    public void userPressesStartButton() {
        mActivityTestRule.getActivity().launchStepCountActivity();
        intended(hasComponent(new ComponentName(getTargetContext(), PlannedWalkActivity.class)));
        onView(withId(R.id.textSteps)).check(matches(withText("0")));
        
    }


    /*
      Scenario 2: User presses end button
        Given that Sarah had started her workout
        And had the app running with an active session
        When she presses the End Walk/Run button
        Then the current session will end and display her stats
    */
    @Test
    public void userPressesEndButton() {
        
        mActivityTestRule.getActivity().launchStepCountActivity();
        intended(hasComponent(new ComponentName(getTargetContext(), PlannedWalkActivity.class)));
        onView(withId(R.id.btnEndRecord)).perform(click());
//        onView(withText(R.string.invalidHeight))
//                .inRoot(isDialog())
//                .check(matches(isDisplayed()));

        // First, she should click the OK button to close active data display dialog
        onView(withText("OK")).perform(click());
        intended(hasComponent(new ComponentName(getTargetContext(), MainActivity.class)));
        
    }

    private class TestMainFitnessService implements FitnessService {
        private static final String TAG = "[TestMainFitnessService]: ";
        private MainActivity mainActivity;

        public TestMainFitnessService(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        public int getRequestCode() {
            return 0;
        }

        @Override
        public void setup() {
            Log.d(TAG, "setup");
        }

        @Override
        public void updateStepCount() {
            Log.d(TAG, "update all texts");
            mainActivity.updateAll(1000);
        }

        @Override
        public void stopAsync() {

        }


        @Override
        public void startAsync() {

        }

        @Override
        public boolean hasPermission() {
            return true;
        }

        @Override
        public void addInactiveSteps(int extraStep) {

        }

        @Override
        public void addActiveSteps(int step) {

        }

        @Override
        public DataReadRequest getLast7DaysSteps(double[] weeklyInactiveSteps, double[] weeklyActiveSteps) {
            return null;
        }

        @Override
        public DataReadRequest getLast7DaysSteps(double[] weeklyInactiveSteps, double[] weeklyActiveSteps, Calendar cal) {
            return null;
        }
    }

    private class TestStepCountFitnessService implements FitnessService {
        private static final String TAG = "[TestStepCountFitnessService]: ";
        private PlannedWalkActivity plannedWalkActivity;

        public TestStepCountFitnessService(PlannedWalkActivity plannedWalkActivity) {
            this.plannedWalkActivity = plannedWalkActivity;
        }

        @Override
        public int getRequestCode() {
            return 0;
        }

        @Override
        public void setup() {
            Log.d(TAG, "setup");
        }

        @Override
        public void updateStepCount() {
            ((TextView) plannedWalkActivity.findViewById(R.id.textSteps)).setText("0");
        }

        @Override
        public void stopAsync() {

        }

        @Override
        public void startAsync() {

        }

        @Override
        public boolean hasPermission() {
            return true;
        }

        @Override
        public void addInactiveSteps(int extraStep) {

        }

        @Override
        public void addActiveSteps(int step) {

        }

        @Override
        public DataReadRequest getLast7DaysSteps(double[] weeklyInactiveSteps, double[] weeklyActiveSteps) {
            return null;
        }

        @Override
        public DataReadRequest getLast7DaysSteps(double[] weeklyInactiveSteps, double[] weeklyActiveSteps, Calendar cal) {
            return null;
        }
    }

    public static void clearSharedPrefs(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences(MainActivity.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
    }
}
