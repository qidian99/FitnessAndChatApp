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
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.intent.Intents;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.view.Gravity;
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
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.DrawerMatchers.isClosed;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AddFriendScenarioTest {
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
        Scenario 1: Richard sends Lisa a one-way friend request
        Given Richard and Lisa have downloaded PersonalBest app
        And Richard has authorized Google Fit Services using his Google email, richard@example.com
        And Lisa has authorized Google Fit Services using her Google email, lisa@example.com
        When Richard opens the app and goes to the new friend sign up page
        And he types in Lisa’s email, lisa@example.com
        And he opens his friends list
        Then Richard will not see Lisa’s name on his friend page
        When Lisa opens the app and goes to her friends list
        Then she will see Richard’s name on her friend list
        And the friend request is pending for approval
     */
    @Test
    public void userAddsFriend() {
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
        onView(withText(R.string.PersonalBest)).check(matches(withParent(withId(R.id.toolbar))));
        onView(withId(R.id.friend_18)).check(matches(isDisplayed()));
        onView(withId(R.id.friend_18)).perform(touchDownAndUp(15, 0));
        // onView(withId(R.id.friend_18)).perform(click());
        intended(hasComponent(new ComponentName(getTargetContext(), NewFriendSignUpActivity.class)));
        onView(withId(R.id.enterFriendEmail)).check(matches(withText("")));
        onView(withId(R.id.enterFriendEmail)).perform(clearText(), typeText("daw096@ucsd.edu"));
        onView(withId(R.id.enterFriendEmail)).perform(closeSoftKeyboard());
        onView(withId(R.id.btnSendFriendReq)).check(matches(isDisplayed()));
        onView(withId(R.id.btnSendFriendReq)).perform(click());
        onView(withId(R.id.enterFriendEmail)).check(matches(withText("")));
        onView(withId(R.id.btnEndRecord)).check(matches(isDisplayed()));
        onView(withId(R.id.btnEndRecord)).perform(click());
        intended(hasComponent(new ComponentName(getTargetContext(), MainActivity.class)));
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
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
