package edu.ucsd.cse110.googlefitapp;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.intent.Intents;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.google.android.gms.fitness.request.DataReadRequest;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

import edu.ucsd.cse110.googlefitapp.fitness.FitnessService;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessServiceFactory;
import edu.ucsd.cse110.googlefitapp.test.steps.SharedSteps;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityUITest {
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
        FitnessServiceFactory.put(TEST_SERVICE_MAIN_ACTIVITY, new FitnessServiceFactory.BluePrint() {
            @Override
            public FitnessService create(StepCountActivity stepCountActivity) {
                return null;
            }

            @Override
            public FitnessService create(MainActivity mainActivity) {
                return new TestMainFitnessService(mainActivity);
            }
        });
        FitnessServiceFactory.put(TEST_SERVICE_STEP_ACTIVITY, new FitnessServiceFactory.BluePrint() {
            @Override
            public FitnessService create(StepCountActivity stepCountActivity) {
                return new TestStepCountFitnessService(stepCountActivity);
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

    @Test
    public void mainActivityTest() {
        
        ViewInteraction textView = onView(
                allOf(withId(R.id.textGoal2), withText("CURRENT GOAL"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.widget.TableLayout.class),
                                        0),
                                0),
                        isDisplayed()));
        textView.check(matches(isDisplayed()));

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.textGoal2), withText("CURRENT GOAL"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.widget.TableLayout.class),
                                        0),
                                0),
                        isDisplayed()));
        textView2.check(matches(withText("CURRENT GOAL")));

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.textGoal), withText("5000"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.widget.TableLayout.class),
                                        1),
                                0),
                        isDisplayed()));
        textView3.check(matches(isDisplayed()));

        ViewInteraction textView4 = onView(
                allOf(withId(R.id.textGoal), withText("5000"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.widget.TableLayout.class),
                                        1),
                                0),
                        isDisplayed()));
        textView4.check(matches(withText("5000")));

        ViewInteraction textView5 = onView(
                allOf(withId(R.id.stepsLeft2), withText("STEPS LEFT"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.widget.TableLayout.class),
                                        0),
                                1),
                        isDisplayed()));
        textView5.check(matches(isDisplayed()));

        ViewInteraction textView6 = onView(
                allOf(withId(R.id.stepsLeft),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.widget.TableLayout.class),
                                        1),
                                1),
                        isDisplayed()));
        textView6.check(matches(withText("Steps Left")));

        ViewInteraction textView7 = onView(
                allOf(withId(R.id.textStepsMain2), withText("STEPS TAKEN")));
        textView7.check(matches(isDisplayed()));

        ViewInteraction textView8 = onView(
                allOf(withId(R.id.textStepsMain)));
        textView8.check(matches(withText("Steps Taken")));

        ViewInteraction button = onView(
                allOf(withId(R.id.btnSetGoal),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.tableLayout),
                                        0),
                                0),
                        isDisplayed()));
        button.check(matches(isDisplayed()));

        ViewInteraction button2 = onView(
                allOf(withId(R.id.btnSetStep),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.tableLayout),
                                        0),
                                1),
                        isDisplayed()));
        button2.check(matches(isDisplayed()));

        ViewInteraction button3 = onView(
                allOf(withId(R.id.setHeightBtn),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.tableLayout),
                                        1),
                                0),
                        isDisplayed()));
        button3.check(matches(isDisplayed()));

        ViewInteraction button4 = onView(
                allOf(withId(R.id.weeklyButton),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.tableLayout),
                                        1),
                                1),
                        isDisplayed()));
        button4.check(matches(isDisplayed()));

        ViewInteraction button5 = onView(
                allOf(withId(R.id.mockCalBtn),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                5),
                        isDisplayed()));
        button5.check(matches(isDisplayed()));
        
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
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
        private StepCountActivity stepCountActivity;

        public TestStepCountFitnessService(StepCountActivity stepCountActivity) {
            this.stepCountActivity = stepCountActivity;
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
