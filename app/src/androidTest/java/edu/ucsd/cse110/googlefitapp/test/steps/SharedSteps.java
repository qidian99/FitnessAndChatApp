package edu.ucsd.cse110.googlefitapp.test.steps;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;

import java.lang.reflect.Field;
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

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static edu.ucsd.cse110.googlefitapp.MainActivity.SHARED_PREFERENCE_NAME;
import static org.hamcrest.Matchers.notNullValue;

public class SharedSteps {

    private ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity
            .class);

    private Map<String, String> nameIdMap = new HashMap<>();

    public SharedSteps() {
    }

    @Before
    public void setup() {
        Intents.init();
        nameIdMap.put("first", "number_1");
        nameIdMap.put("second", "number_2");
    }

    @After
    public void tearDown() {
        mActivityTestRule.getActivity().finish();
        Intents.release();
    }

    public void restartApp() {
        InstrumentationRegistry.getTargetContext()
                .getSharedPreferences(
                        SHARED_PREFERENCE_NAME,
                        Context.MODE_PRIVATE)
                .edit()
                .remove(MainActivity.KEY_HEIGHT)
                .remove(MainActivity.KEY_METRIC)
                .remove(MainActivity.KEY_MAGNITUDE)
                .remove(MainActivity.KEY_STRIDE)
                .remove(MainActivity.KEY_BEFORE)
                .remove(MainActivity.KEY_GOAL)
                .apply();
    }

    @Given("^Sarah has successfully downloaded the app$")
    public void sarahHasSuccessfullyDownloadedTheApp() throws Throwable {
        // Should clear any shared preference and restart the app if necessary to make it
        // act like newly downloaded app
        restartApp();
        mActivityTestRule.launchActivity(null);
        assertThat(mActivityTestRule.getActivity(), notNullValue());
    }

    @And("^she has accepted all the permissions$")
    public void sheHasAcceptedAllThePermissions() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("^she uses feet and inches for her height$")
    public void sheUsesFeetAndInchesForHerHeight() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("^the application asks for her height, she$")
    public void theApplicationAsksForHerHeightShe() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("^chooses the feet and inches option in the drop-down menu$")
    public void choosesTheFeetAndInchesOptionInTheDropDownMenu() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("^inputs (\\d+) in the first textbox and (\\d+) in the second textbox$")
    public void inputsInTheFirstTextboxAndInTheSecondTextbox(int arg0, int arg1) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("^she presses the “Done” button$")
    public void shePressesTheDoneButton() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("^she is taken to the home screen.$")
    public void sheIsTakenToTheHomeScreen() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }




    /*@Before
    public void setup() {
        Intents.init();
        nameIdMap.put("first", "number_1");
        nameIdMap.put("second", "number_2");
    }

    @After
    public void tearDown() {
        mActivityTestRule.getActivity().finish();
        Intents.release();
    }

    @Given("a main activity")z
    public void aMainActivity() {
        System.out.println("STARTING MAINACTIVITY");
        mActivityTestRule.launchActivity(null);
        assertThat(mActivityTestRule.getActivity(), notNullValue());
    }

    public static int getLayoutIdFromString(String resName) {
        try {
            Field idField = R.id.class.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @When("^the user enters (\\d+) in the (.*) text field$")
    public void theUserEntersANumberInTheEdittextFieldWithId(int number, String id) throws Throwable {
        int layoutId = getLayoutIdFromString(nameIdMap.get(id));
        onView(withId(layoutId))
                .check(matches(isDisplayed()))
                .perform(typeText("" + number));
    }

    @And("^the user clicks the plus button$")
    public void theUserClicksThePlusButton() throws Throwable {
        onView(withId(R.id.btn_plus))
                .check(matches(isDisplayed()))
                .perform(click());
    }

    @And("^the user clicks the minus button$")
    public void theUserClicksTheMinusButton() throws Throwable {
        onView(withId(R.id.btn_minus))
                .check(matches(isDisplayed()))
                .perform(click());
    }

    @Then("^the answer is 579$")
    public void theAnswerIs() throws Throwable {
        onView(withId(R.id.answer))
                .check(matches(isDisplayed()))
                .check(matches(withText("579")));
    }

    @Then("^the answer is 222$")
    public void theMinusAnswerIs() throws Throwable {
        onView(withId(R.id.answer))
                .check(matches(isDisplayed()))
                .check(matches(withText("222")));
    }*/
}
