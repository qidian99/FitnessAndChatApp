package edu.ucsd.cse110.googlefitapp.test.steps;

import android.content.Intent;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.matcher.RootMatchers;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;

import java.util.HashMap;
import java.util.Map;

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

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsAnything.anything;
import static org.hamcrest.core.StringContains.containsString;

public class SharedSteps {
    private static final String TEST_SERVICE_MAIN_ACTIVITY = "TEST_SERVICE_MAIN_ACTIVITY";
    private static final String TEST_SERVICE_STEP_ACTIVITY = "TEST_SERVICE_STEP_ACTIVITY";

    private ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity
            .class);

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
                return null;
            }

            @Override
            public FitnessService create(MainActivity mainActivity) {
                return null;
            }
        });

        Intent intent = new Intent();
        intent.putExtra("TEST", true);
        intent.putExtra("TEST_SERVICE_MAIN", TEST_SERVICE_MAIN_ACTIVITY);
        intent.putExtra("TEST_SERVICE_STEP_COUNT", TEST_SERVICE_STEP_ACTIVITY);
        mActivityTestRule.launchActivity(intent);
        mActivityTestRule.getActivity().setFitnessServiceKey(TEST_SERVICE_STEP_ACTIVITY);

        Intents.init();
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
    public void sarahHasSuccessfullyDownloadedTheApp() throws Throwable {
        assertThat(mActivityTestRule.getActivity(), notNullValue());
    }

    @And("^she has accepted all the permissions$")
    public void sheHasAcceptedAllThePermissions() throws Throwable {}

    @And("^she uses feet and inches for her height$")
    public void sheUsesFeetAndInchesForHerHeight() throws Throwable {}

    @When("^the application asks for her height, she$")
    public void theApplicationAsksForHerHeightShe() throws Throwable {
        onView(withId(R.id.clearBtn))
                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))));
        onView(withId(R.id.clearBtn)).perform(click());
    }

    @Then("^chooses the feet and inches option in the drop-down menu$")
    public void choosesTheFeetAndInchesOptionInTheDropDownMenu() throws Throwable {
        // Make sure a dialog show up
        onView(withText(R.string.heightPrompt))
        .inRoot(isDialog()) // <---
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
        // Make sure the dialog disappear
//        onView(withText(R.string.heightPrompt))
//                .inRoot(isDialog()) // <---
//                .check(matches(not(isDisplayed())));
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
            stepCountActivity.setStepCount(1000);
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
    }

}
