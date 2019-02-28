package edu.ucsd.cse110.team4personalbest;


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

import edu.ucsd.cse110.team4personalbest.fitness.FitnessService;
import edu.ucsd.cse110.team4personalbest.fitness.FitnessServiceFactory;
import edu.ucsd.cse110.team4personalbest.fitness.GoogleFitnessServiceFactory;

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
public class CreateNewGoalScenarioTest {
    private static final String TEST_SERVICE_MAIN_ACTIVITY = "TEST_SERVICE_MAIN_ACTIVITY";
    private static final String TEST_SERVICE_STEP_ACTIVITY = "TEST_SERVICE_STEP_ACTIVITY";

    private ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<MainActivity>(MainActivity
            .class) {
        @Override
        protected void beforeActivityLaunched() {
            clearSharedPrefs(InstrumentationRegistry.getTargetContext());
            super.beforeActivityLaunched();
        }
    };

    public static void clearSharedPrefs(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences(MainActivity.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
    }

    @Before
    public void setup() {
        FitnessServiceFactory googleFitnessServiceFactory = new GoogleFitnessServiceFactory();
        googleFitnessServiceFactory.put(TEST_SERVICE_MAIN_ACTIVITY, new FitnessServiceFactory.BluePrint() {
            @Override
            public FitnessService create(Activity activity) {
                return new TestMainFitnessService(activity);
            }
        });
        googleFitnessServiceFactory.put(TEST_SERVICE_STEP_ACTIVITY, new FitnessServiceFactory.BluePrint() {
            @Override
            public FitnessService create(Activity activity) {
                return new TestStepCountFitnessService(activity);
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
    Feature: Creating New Step Goals
      Scenario 1: Daily step goal is not met
        Given that user’s initial goal is 5000 steps a day
        When she walks below 5000 steps
        Then she will not be given the option to change her daily goal
     */
    @Test
    public void dailyStepGoalNotMet() {
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
      Scenario 2: Accept the optional new goal
        Given that user’s initial goal is 5000 steps a day
        And he or she walks over 5000 steps
        And he or she will be given the option to change her daily goal
        When he or she accepts the optional new goal which is 5500
        Then the new goal of 5500 steps is set
        And he or she will be directed to the home screen
        */
    @Test
    public void userAcceptsOptionalNewGoal() {
        onView(withId(R.id.btnSetGoal))
                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))));
        onView(withId(R.id.btnSetGoal)).perform(click());
        onView(withId(R.id.newGoal))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withId(R.id.newGoal)).perform(typeText(String.valueOf(5000)));
        onView(withId(R.id.doneBtn)).perform(click());

        onView(withText(R.string.congrats))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        // First, she should click the OK button to close the alert
        onView(withText("Yes")).perform(click());

        onView(withId(R.id.newGoal))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnSuggested)).perform(click());
        intended(hasComponent(new ComponentName(getTargetContext(), MainActivity.class)));
        onView(withId(R.id.textGoal)).check(matches(withText("5500")));
    }

    /*
     Scenario 3: Choose to create custom a new goal and higher than before
        Given that user’s initial goal is 5000 steps a day
        And he or she walks over 5000 steps
        And he or she will be given the option to change her daily goal
        When he or she rejects the optional new goal which is 5500
        Then the application asks the user if he or she wants to set custom goal
        When he or she chooses to set custom goal
        Then the application prompt a text-box for new goal input
        When he or she type in 6000 steps
        Then the new goal of 5500 steps is set
        And he or she will be directed to the home screen
     */
    @Test
    public void userEntersTooHighNewGoal() {
        onView(withId(R.id.btnSetGoal))
                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))));
        onView(withId(R.id.btnSetGoal)).perform(click());
        onView(withId(R.id.newGoal))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withId(R.id.newGoal)).perform(typeText(String.valueOf(5000)));
        onView(withId(R.id.doneBtn)).perform(click());

        onView(withText(R.string.congrats))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        // First, she should click the OK button to close the alert
        onView(withText("Yes")).perform(click());

        // Then, the dialog for her to enter her height should not disappear
        onView(withId(R.id.newGoal))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withId(R.id.newGoal)).perform(typeText(String.valueOf(100000)));
        onView(withId(R.id.btnCustomed)).perform(click());
        onView(withText(R.string.invalidGoal))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    /*
     Scenario 4: Choose to create custom a new goal but lower than before
       Given that user’s initial goal is 5000 steps a day
       And he or she walks over 5000 steps
       And he or she will be given the option to change her daily goal
       When he or she rejects the optional new goal which is 5500
       Then the application asks the user if he or she wants to set custom goal
       When he or she chooses to set custom goal
       Then the application prompt a text-box for new goal input
       When he or she type in 4500 steps
       Then the application should say the new goal must be higher than before
       And the application will ask him or her to type in new goal again
   */
    @Test
    public void userEntersALowerNewGoal() {
        onView(withId(R.id.btnSetGoal))
                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))));
        onView(withId(R.id.btnSetGoal)).perform(click());
        onView(withId(R.id.newGoal))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withId(R.id.newGoal)).perform(typeText(String.valueOf(5000)));
        onView(withId(R.id.doneBtn)).perform(click());

        onView(withText(R.string.congrats))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        // First, she should click the OK button to close the alert
        onView(withText("Yes")).perform(click());

        // Then, the dialog for her to enter her height should not disappear
        onView(withId(R.id.newGoal))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withId(R.id.newGoal)).perform(typeText(String.valueOf(0)));
        onView(withId(R.id.btnCustomed)).perform(click());
        onView(withText(R.string.invalidGoal))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    /*
      Scenario 5: Declining the new optional goal and custom goal
      Choose to create custom a new goal and higher than before
        Given that user’s initial goal is 5000 steps a day
        And he or she walks over 5000 steps

        +
        Then the goal is not changed and still be 5000 steps
        And he or she will be directed to the home screen
        */
    @Test
    public void userDeclinesOptionalNewGoal() {
        onView(withId(R.id.btnSetGoal))
                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))));
        onView(withId(R.id.btnSetGoal)).perform(click());
        onView(withId(R.id.newGoal))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withId(R.id.newGoal)).perform(typeText(String.valueOf(5000)));
        onView(withId(R.id.doneBtn)).perform(click());

        onView(withText(R.string.congrats))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        // First, she should click the OK button to close the alert
        onView(withText("Yes")).perform(click());

        onView(withId(R.id.newGoal))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnCustomed)).perform(click());
    }

    private class TestMainFitnessService implements FitnessService {
        private static final String TAG = "[TestMainFitnessService]: ";
        private Activity mainActivity;

        public TestMainFitnessService(Activity mainActivity) {
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
            mainActivity.setStep(5000);
            mainActivity.notifyObservers();
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
        private Activity plannedWalkActivity;

        public TestStepCountFitnessService(Activity plannedWalkActivity) {
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
}
