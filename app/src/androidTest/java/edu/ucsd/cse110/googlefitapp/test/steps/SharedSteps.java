package edu.ucsd.cse110.googlefitapp.test.steps;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.matcher.RootMatchers;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import cucumber.api.PendingException;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import edu.ucsd.cse110.googlefitapp.MainActivity;
import edu.ucsd.cse110.googlefitapp.R;
import edu.ucsd.cse110.googlefitapp.StepCountActivity;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessService;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessServiceFactory;
import gherkin.cli.Main;

import static android.content.Context.MODE_PRIVATE;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.google.android.gms.fitness.request.DataReadRequest;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsAnything.anything;
import static org.hamcrest.core.StringContains.containsString;

public class SharedSteps {
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

    private Map<String, String> nameIdMap = new HashMap<>();

    public SharedSteps() {
    }

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

//        nameIdMap.put("first", "number_1");
//        nameIdMap.put("second", "number_2");
    }

    @After
    public void tearDown() {
        mActivityTestRule.getActivity().finish();
        Intents.release();
    }

    public void restartApp() {
//        InstrumentationRegistry.getTargetContext()
//                .getSharedPreferences(
//                        SHARED_PREFERENCE_NAME,
//                        Context.MODE_PRIVATE)
//                .edit()
//                .remove(MainActivity.KEY_HEIGHT)
//                .remove(MainActivity.KEY_METRIC)
//                .remove(MainActivity.KEY_MAGNITUDE)
//                .remove(MainActivity.KEY_STRIDE)
//                .remove(MainActivity.KEY_BEFORE)
//                .remove(MainActivity.KEY_GOAL)
//                .apply();
    }

    @Given("^Sarah has successfully downloaded the app$")
    @And("^she has accepted all the permissions|she uses feet and inches for her height$")
    public void sarahStartsTheApp() throws Throwable {
        onHomeScreenAssertion();
    }

    @When("^the application asks for her height, she$")
    public void theApplicationAsksForHerHeightShe() throws Throwable {
        // Here, ask for her height means that she clicks START RECORDING button
        // Since Sarah has never entered her height, she is asked to enter her height
        onView(withId(R.id.startBtn))
                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))));
        onView(withId(R.id.startBtn)).perform(click());
    }

    @Then("^chooses the feet and inches option in the drop-down menu$")
    public void choosesTheFeetAndInchesOptionInTheDropDownMenu() throws Throwable {
        // Make sure a dialog show up
        onView(withText(R.string.heightPrompt))
        .inRoot(isDialog())
        .check(matches(isDisplayed()));

        // Choose feet and inches
        onView(withId(R.id.metricSpinner)).perform(click());
        onData(anything()).inRoot(RootMatchers.isPlatformPopup()).atPosition(1).perform(click());

        onView(withId(R.id.metricSpinner)).check(matches(withSpinnerText(containsString("ft"))));
        onView(withId(R.id.cent_height))
                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.GONE))));
        onView(withId(R.id.ft_height))
                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))));
        onView(withId(R.id.inch_height))
                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))));
    }

    @And("^inputs (\\d+) in the first textbox and (\\d+) in the second textbox$")
    public void inputsInTheFirstTextboxAndInTheSecondTextbox(int arg0, int arg1) throws Throwable {
        onView(withId(R.id.ft_height)).perform(typeText(String.valueOf(arg0)));
        onView(withId(R.id.inch_height)).perform(typeText(String.valueOf(arg1)));
    }

    @When("^she presses the “Done” button$")
    public void shePressesTheDoneButton() throws Throwable {
        onView(withId(R.id.posBtn)).perform(click());
    }

    @Then("^she is taken to the home screen.$")
    public void sheIsTakenToTheHomeScreen() throws Throwable {
        //  Make sure the dialog disappear and she is on the Home Screen now
        intended(hasComponent(new ComponentName(getTargetContext(), MainActivity.class)));
    }

    @Given("^Richard has successfully downloaded the app following a google search$")
    @And("^has accepted all permissions by checking the “OK” boxes|he uses centimeter for his height$")

    public void richardStartsTheApp() throws Throwable {
        onHomeScreenAssertion();
    }

    @When("^the application asks for his height, he$")
    public void theApplicationAsksForHisHeightHe() throws Throwable {
        // Here, ask for his height means that he clicks START RECORDING button
        // Since Richard has never entered his height, he is asked to enter her height
        System.out.print(getTargetContext().getSharedPreferences(MainActivity.SHARED_PREFERENCE_NAME, MODE_PRIVATE).getFloat("stride", -1));
        onView(withId(R.id.startBtn))
                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))));
        onView(withId(R.id.startBtn)).perform(click());
    }

    @Then("^chooses the centimeters option in the drop-down menu$")
    public void choosesTheCentimetersOptionInTheDropDownMenu() throws Throwable {
        // Make sure a dialog show up
        onView(withText(R.string.heightPrompt))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        // Choose cm
        onView(withId(R.id.metricSpinner)).perform(click());
        onData(anything()).inRoot(RootMatchers.isPlatformPopup()).atPosition(0).perform(click());

        onView(withId(R.id.metricSpinner)).check(matches(withSpinnerText(containsString("cm"))));
        onView(withId(R.id.cent_height))
                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))));
        onView(withId(R.id.ft_height))
                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.GONE))));
        onView(withId(R.id.inch_height))
                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.GONE))));
    }

    @And("^input a value of (\\d+) in the textbox$")
    public void inputAValueOfInTheTextbox(int arg0) throws Throwable {
        onView(withId(R.id.cent_height)).perform(typeText(String.valueOf(arg0)));
    }

    @When("^he presses the “Done” button$")
    public void hePressesTheDoneButton() throws Throwable {
        onView(withId(R.id.posBtn)).perform(click());
    }

    @Then("^it took him took the home screen$")
    public void itTookHimTookTheHomeScreen() throws Throwable {
        intended(hasComponent(new ComponentName(getTargetContext(), MainActivity.class)));
    }

    @Then("^the application should say height is invalid$")
    public void theApplicationShouldSayHeightIsInvalid() throws Throwable {
        // The alert dialog should show up saying the the height she entered is invalid
        onView(withText(R.string.invalidHeight))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @And("^the application will ask her to type appropriate height$")
    public void theApplicationWillAskHerToTypeAppropriateHeight() throws Throwable {
        // First, she should click the OK button to close the alert
        onView(withText("OK")).perform(click());

        // Then, the dialog for her to enter her height should not disappear
        onView(withText(R.string.heightPrompt))
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
            System.out.println(TAG + "setup");
        }

        @Override
        public void updateStepCount() {
            System.out.println(TAG + "update all texts");
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
            System.out.println(TAG + "setup");
        }

        @Override
        public void updateStepCount() {
            System.out.println(TAG + "updateStepCount");
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

    public void onHomeScreenAssertion(){
        assertThat(mActivityTestRule.getActivity(), notNullValue());

        ViewInteraction startBtn = onView(
                allOf(withId(R.id.startBtn),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        startBtn.check(matches(isDisplayed()));

        ViewInteraction stepsLeft = onView(
                allOf(withId(R.id.stepsLeft),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                5),
                        isDisplayed()));
        stepsLeft.check(matches(isDisplayed()));

        ViewInteraction textStepsMain = onView(
                allOf(withId(R.id.textStepsMain),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                4),
                        isDisplayed()));
        textStepsMain.check(matches(isDisplayed()));

        ViewInteraction textGoal = onView(
                allOf(withId(R.id.textGoal),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        textGoal.check(matches(isDisplayed()));

        ViewInteraction weeklyButton = onView(
                allOf(withId(R.id.weeklyButton),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                6),
                        isDisplayed()));
        weeklyButton.check(matches(isDisplayed()));

        ViewInteraction btnSetGoal = onView(
                allOf(withId(R.id.btnSetGoal),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        btnSetGoal.check(matches(isDisplayed()));

        ViewInteraction clearBtn = onView(
                allOf(withId(R.id.clearBtn),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        clearBtn.check(matches(isDisplayed()));

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
}
