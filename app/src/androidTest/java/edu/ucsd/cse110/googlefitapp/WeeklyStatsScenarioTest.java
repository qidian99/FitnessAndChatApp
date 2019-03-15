package edu.ucsd.cse110.googlefitapp;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Root;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.MotionEvents;
import android.support.test.espresso.intent.Intents;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.junit.runner.RunWith;

import edu.ucsd.cse110.googlefitapp.fitness.FitnessService;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessServiceFactory;
import edu.ucsd.cse110.googlefitapp.fitness.GoogleFitnessServiceFactory;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class WeeklyStatsScenarioTest {
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

    private static ViewAction touchDownAndUp(final float x, final float y) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "Send touch events.";
            }

            @Override
            public void perform(UiController uiController, final View view) {
                // Get view absolute position
                int[] location = new int[2];
                view.getLocationOnScreen(location);

                // Offset coordinates by view position
                float[] coordinates = new float[]{x + location[0], y + location[1]};
                float[] precision = new float[]{1f, 1f};

                // Send down event, pause, and send up
                MotionEvent down = MotionEvents.sendDown(uiController, coordinates, precision).down;
                uiController.loopMainThreadForAtLeast(200);
                MotionEvents.sendUp(uiController, down, coordinates);
            }
        };
    }

    @Before
    public void setup() {
        FitnessServiceFactory googleFitnessServiceFactory = new GoogleFitnessServiceFactory();
        googleFitnessServiceFactory.put(TEST_SERVICE_MAIN_ACTIVITY, TestMainFitnessService::new);
        googleFitnessServiceFactory.put(TEST_SERVICE_STEP_ACTIVITY, plannedWalkActivity -> new TestStepCountFitnessService());

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
        Scenario 1: User Opens Bar Chart At the End of a Week
        Given that User has successfully logged in to Google account in Personal Best
        And that from last Sunday to Saturday, User has taken 100, 100, 100, 200, 200, 300, 300 unintentional steps
        And that from last Sunday to Saturday, User has walked 200 steps everyday the active session
        And that the current step goal is 500
        When User taps on the show bar chart button
        Then the bar chart of User’s activity record for the past seven days shows up
        And each bar has two colors with red indicating the unintentional steps and blue indicating the intentional steps
        And the red section of each of the seven bars is labeled as 100, 100, 100, 200, 200, 300, 300 respectively
        And the blue section of each bar is labeled as 200, 200, 200, 200, 200, 200, 200 respectively
        And no bar goes over the red horizontal line at 500 steps
     */
    @Test
    public void userSeeWeeklyStats() {
        onView(withId(R.id.weeklyButton)).check(matches(isDisplayed()));
        onView(withId(R.id.weeklyButton)).perform(click());
        onView(withText("WEEKLY"))
                .inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(R.id.congrats)).check(matches(withText("In which time span would you like to view your stats?")));
        onView(withText("WEEKLY"))
                .inRoot(isDialog()).perform(click());
        intended(hasComponent(new ComponentName(getTargetContext(), WeeklyStatsActivity.class)));
        onView(withId(R.id.graphUsage)).check(matches(withText("Tap on blue bar: display stats for active" +
                " sessions of the day.\n\n\t\tTap on red bar: " +
                "display stats from the rest of the day.")));
        onView(withId(R.id.backToHome)).check(matches(isDisplayed()));
        onView(withId(R.id.backToHome)).perform(click());
        intended(hasComponent(new ComponentName(getTargetContext(), MainActivity.class)));
    }

    private class ToastMatcher extends TypeSafeMatcher<Root> {
        @Override
        public void describeTo(Description description) {
            description.appendText("is toast");
        }

        @Override
        public boolean matchesSafely(Root root) {
            int type = root.getWindowLayoutParams().get().type;
            if ((type == WindowManager.LayoutParams.TYPE_TOAST)) {
                IBinder windowToken = root.getDecorView().getWindowToken();
                IBinder appToken = root.getDecorView().getApplicationWindowToken();
                return windowToken == appToken;
            }
            return false;
        }
    }

    private class TestMainFitnessService implements FitnessService {
        private static final String TAG = "[TestMainFitnessService]: ";
        private Activity mainActivity;

        TestMainFitnessService(Activity mainActivity) {
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
            mainActivity.setStep(3000);
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

        TestStepCountFitnessService() {
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
