package edu.ucsd.cse110.googlefitapp;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.google.android.gms.fitness.request.DataReadRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

import edu.ucsd.cse110.googlefitapp.fitness.FitnessService;
import edu.ucsd.cse110.googlefitapp.fitness.GoogleFitnessServiceFactory;

import static android.support.test.InstrumentationRegistry.getTargetContext;
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
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CustomGoalScenarioTest {
    private static final String TEST_SERVICE_MAIN_ACTIVITY = "TEST_SERVICE_MAIN_ACTIVITY";
    private static final String TEST_SERVICE_STEP_ACTIVITY = "TEST_SERVICE_STEP_ACTIVITY";

    private ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<MainActivity>(MainActivity
            .class){
        @Override
        protected void beforeActivityLaunched() {
            clearSharedPrefs(InstrumentationRegistry.getTargetContext());
            super.beforeActivityLaunched();
        }
    };

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
    }

    @After
    public void tearDown() {
        mActivityTestRule.getActivity().finish();
        Intents.release();
    }


    /*
    Feature: Changing step goal at any time
      Scenario 1: Sarah input new valid goal
        Given Sarah has averaged 4000 steps per day for the week
        And her current goal is 5000 steps per day
        And she press “changing step goal”
        When she set her goal to 4500 steps per day a week
        Then application will show that her new goal of 4500 is set
        And she will be directed to the home screen
     */
    @Test
    public void userEntersValidCustomGoal() {
        onView(withId(R.id.btnSetGoal))
                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))));
        onView(withId(R.id.btnSetGoal)).perform(click());
        onView(withId(R.id.newGoal))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withId(R.id.newGoal)).perform(typeText(String.valueOf(3000)));
        onView(withId(R.id.doneBtn)).perform(click());

        intended(hasComponent(new ComponentName(getTargetContext(), MainActivity.class)));
    }


    /*
      Scenario 2: Sarah input invalid valid goal too low
        Given Sarah has averaged 4000 steps per day for the week
        And her current goal is 5000 steps per day
        And she press “changing step goal”
        When she set her goal to 0 steps per day a week
        Then application will show that her new goal of 0 is invalid
        And she will be asked to input a valid goal
    */
    @Test
    public void userEntersTooLowCustomGoal() {
        onView(withId(R.id.btnSetGoal))
                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))));
        onView(withId(R.id.btnSetGoal)).perform(click());
        onView(withId(R.id.newGoal))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withId(R.id.newGoal)).perform(typeText(String.valueOf(0)));
        onView(withId(R.id.doneBtn)).perform(click());
        onView(withText(R.string.invalidGoal))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        // First, she should click the OK button to close the alert
        onView(withText("OK")).perform(click());

        // Then, the dialog for her to enter her height should not disappear
        onView(withId(R.id.newGoal))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    /*
      Scenario 3: Sarah input invalid valid goal too high
        Given Sarah has averaged 4000 steps per day for the week
        And her current goal is 5000 steps per day
        And she press “changing step goal”
        When she set her goal to 0 steps per day a week
        Then application will show that her new goal of 0 is invalid
        And she will be asked to input a valid goal
     */
    @Test
    public void userEntersTooHighCustomGoal() {
        onView(withId(R.id.btnSetGoal))
                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))));
        onView(withId(R.id.btnSetGoal)).perform(click());
        onView(withId(R.id.newGoal))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withId(R.id.newGoal)).perform(typeText(String.valueOf(100000)));
        onView(withId(R.id.doneBtn)).perform(click());
        onView(withText(R.string.invalidGoal))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        // First, she should click the OK button to close the alert
        onView(withText("OK")).perform(click());

        // Then, the dialog for her to enter her height should not disappear
        onView(withId(R.id.newGoal))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

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
            mainActivity.updateAll(3000);
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
            Log.d(TAG, "updateStepCount");
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
