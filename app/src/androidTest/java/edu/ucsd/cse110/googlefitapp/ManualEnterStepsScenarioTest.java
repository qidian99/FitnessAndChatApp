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
import edu.ucsd.cse110.googlefitapp.fitness.FitnessServiceFactory;
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
public class ManualEnterStepsScenarioTest {
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
      Feature: Manually Record Steps Taken
      Note that since users are normally poor at estimation, this should be based on history data. However, users might use this to cheat on their workouts
      Scenario 1: User enters a valid number to manually record the steps taken
        Given that Richard went out for a walk
        And that he forgot his phone at home
        And after returning from the walk and reviewing his history steps, Richard came up with an estimate of 2000 steps today
        And his previous steps are ranging from 500 - 3500 steps
        When he input the estimate of 2000,
        Then the Home Screen says that he has walked 2000 steps today.
     */
    @Test
    public void userEntersValidStepNumber() {
        onView(withId(R.id.btnSetStep))
                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))));
        onView(withId(R.id.btnSetStep)).perform(click());
        onView(withId(R.id.num_steps))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withId(R.id.num_steps)).perform(typeText(String.valueOf(3000)));
        onView(withId(R.id.stepPosBtn)).perform(click());

        intended(hasComponent(new ComponentName(getTargetContext(), MainActivity.class)));
    }

    /*
     Scenario 2: User enters an invalid number when manually recording the steps taken
        Given that Richard went out for a walk
        And that he forgot his phone at home
        And after returning from the walk and reviewing his history steps, Richard came up with an estimate of 2000 steps today
        And his previous steps are ranging from 500 - 3500 steps
        When he input the estimate of 0,
        Then the Home Screen says that he cannot input 0 as estimated steps
        And Home Screen will ask him to input a valid number
    */
    @Test
    public void userEntersTooSmallNumber() {
        onView(withId(R.id.btnSetStep))
                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))));
        onView(withId(R.id.btnSetStep)).perform(click());
        onView(withId(R.id.num_steps))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withId(R.id.num_steps)).perform(typeText(String.valueOf(0)));
        onView(withId(R.id.stepPosBtn)).perform(click());
        onView(withText(R.string.invalidStep))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        // First, she should click the OK button to close the alert
        onView(withText("OK")).perform(click());

        // Then, the dialog for her to enter her height should not disappear
        onView(withId(R.id.num_steps))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    /*
      Scenario 3: User enters a too high estimation when manually recording the steps taken
        Given that Richard went out for a walk
        And that he forgot his phone at home
        And after returning from the walk and reviewing his history steps, Richard came up with an estimate of 10000 steps today
        And his previous steps are ranging from 500 - 3500 steps
        When he input the estimate of 10000,
        Then the Home Screen says that he cannot input 10000 as estimated steps because it is too high compared to his highest step record for the week, which is 3500 steps
        And Home Screen will ask him to input a reasonable number
     */
    @Test
    public void userEntersTooHighNumber() {
        onView(withId(R.id.btnSetStep))
                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))));
        onView(withId(R.id.btnSetStep)).perform(click());
        onView(withId(R.id.num_steps))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        // We change the too high number to 100000, since that is a more extreme case
        onView(withId(R.id.num_steps)).perform(typeText(String.valueOf(100000)));
        onView(withId(R.id.stepPosBtn)).perform(click());
        onView(withText(R.string.invalidStep))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        // First, she should click the OK button to close the alert
        onView(withText("OK")).perform(click());

        // Then, the dialog for her to enter her height should not disappear
        onView(withId(R.id.num_steps))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

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
        public void addActiveSteps(int step, int min, int sec, float stride) {

        }

        @Override
        public String getUID() {
            return null;
        }

        @Override
        public String getEmail() {
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
        public void addActiveSteps(int step, int min, int sec, float stride) {

        }

        @Override
        public String getUID() {
            return null;
        }

        @Override
        public String getEmail() {
            return null;
        }
    }
}
