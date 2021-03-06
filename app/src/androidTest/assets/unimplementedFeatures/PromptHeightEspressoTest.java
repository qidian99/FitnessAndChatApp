//package edu.ucsd.cse110.googlefitapp;
//import static android.app.PendingIntent.getActivity;
//import static android.support.test.espresso.assertion.ViewAssertions.matches;
//import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
//import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
//import static android.support.test.espresso.matcher.ViewMatchers.withText;
//import static org.hamcrest.Matchers.is;
//import static org.hamcrest.Matchers.not;
//
//import android.support.test.espresso.ViewInteraction;
//import android.support.test.espresso.matcher.RootMatchers;
//import android.support.test.espresso.matcher.ViewMatchers;
//import android.support.test.filters.LargeTest;
//import android.support.test.rule.ActivityTestRule;
//import android.support.test.runner.AndroidJUnit4;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.ViewParent;
//import android.widget.Button;
//
//import org.hamcrest.Description;
//import org.hamcrest.Matcher;
//import org.hamcrest.TypeSafeMatcher;
//import org.hamcrest.core.IsInstanceOf;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import static android.support.test.espresso.Espresso.onData;
//import static android.support.test.espresso.Espresso.onView;
//import static android.support.test.espresso.action.ViewActions.click;
//import static android.support.test.espresso.action.ViewActions.typeText;
//import static android.support.test.espresso.assertion.ViewAssertions.matches;
//import static android.support.test.espresso.matcher.RootMatchers.isDialog;
//import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
//import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
//import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
//import static android.support.test.espresso.matcher.ViewMatchers.withId;
//import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
//import static android.support.test.espresso.matcher.ViewMatchers.withText;
//import static org.hamcrest.Matchers.allOf;
//import static org.hamcrest.Matchers.containsString;
//import static org.hamcrest.Matchers.instanceOf;
//import static org.hamcrest.Matchers.is;
//import static org.hamcrest.core.IsAnything.anything;
//
//@LargeTest
//@RunWith(AndroidJUnit4.class)
//public class PromptHeightEspressoTest {
//    @Rule
//    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);
//    private MainActivity activity = mActivityTestRule.getActivity();
//
//    @Test
//    public void promptHeightEspressoTest() {
//        // Dialog should appear first
////        onView(withText(R.string.heightPrompt))
////                .inRoot(isDialog()) // <---
////                .check(matches(isDisplayed()));
//
//        // Click button for entering height
//        onView(withId(R.id.clearBtn)).perform(click());
//        // Check initial visibility of EditText
//        onView(withId(R.id.cent_height))
//                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))));
//        onView(withId(R.id.ft_height))
//                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.GONE))));
//        onView(withId(R.id.inch_height))
//                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.GONE))));
//
//        // Check visibility after selecting spinner
//        // First check spinner is displayed
//        onView(withId(R.id.metricSpinner))
//                .check(matches(isDisplayed()));
//
//        onView(withId(R.id.metricSpinner)).perform(click());
//        onData(anything()).inRoot(RootMatchers.isPlatformPopup()).atPosition(1).perform(click());
//
//        onView(withId(R.id.metricSpinner)).check(matches(withSpinnerText(containsString("ft"))));
//        onView(withId(R.id.cent_height))
//                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.GONE))));
//        onView(withId(R.id.ft_height))
//                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))));
//        onView(withId(R.id.inch_height))
//                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))));
//
//        // Revert back
//        onView(withId(R.id.metricSpinner)).perform(click());
//        onData(anything()).inRoot(RootMatchers.isPlatformPopup()).atPosition(0).perform(click());
//
//        onView(withId(R.id.metricSpinner)).check(matches(withSpinnerText(containsString("cm"))));
//        onView(withId(R.id.cent_height))
//                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))));
//        onView(withId(R.id.ft_height))
//                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.GONE))));
//        onView(withId(R.id.inch_height))
//                .check(matches((withEffectiveVisibility(ViewMatchers.Visibility.GONE))));
//
//        // Test invalid input 1
//        onView(withId(R.id.cent_height)).perform(typeText("10000"));
//        onView(withId(R.id.cent_height)).check(matches(withText("10000")));
//        onView(withId(R.id.posBtn)).perform(click());
//        onView(withText(R.string.invalidHeight)).check(matches(isDisplayed()));
//        onView(withText("OK")).perform(click());
//
//        // Test invalid input 2
//        onView(withId(R.id.metricSpinner)).perform(click());
//        onData(anything()).inRoot(RootMatchers.isPlatformPopup()).atPosition(1).perform(click());
//        onView(withId(R.id.metricSpinner)).check(matches(withSpinnerText(containsString("ft"))));
//        onView(withId(R.id.ft_height)).perform(typeText("10000"));
//        onView(withId(R.id.inch_height)).perform(typeText(""));
//        onView(withId(R.id.posBtn)).perform(click());
//        onView(withText(R.string.invalidHeight)).check(matches(isDisplayed()));
//        onView(withText("OK")).perform(click());
//
//        onView(withId(R.id.ft_height)).perform(typeText(""));
//        onView(withId(R.id.inch_height)).perform(typeText(""));
//        onView(withId(R.id.posBtn)).perform(click());
//        onView(withText(R.string.invalidHeight)).check(matches(isDisplayed()));
//        onView(withText("OK")).perform(click());
//
//        onView(withId(R.id.ft_height)).perform(typeText(""));
//        onView(withId(R.id.inch_height)).perform(typeText("12"));
//        onView(withId(R.id.posBtn)).perform(click());
//        onView(withText(R.string.invalidHeight)).check(matches(isDisplayed()));
//        onView(withText("OK")).perform(click());
//
//        onView(withId(R.id.ft_height)).perform(typeText("10000"));
//        onView(withId(R.id.inch_height)).perform(typeText("10000"));
//        onView(withId(R.id.posBtn)).perform(click());
//        onView(withText(R.string.invalidHeight)).check(matches(isDisplayed()));
//        onView(withText("OK")).perform(click());
//
//        // Test valid input
//        onView(withId(R.id.ft_height)).perform(typeText("6"));
//        onView(withId(R.id.inch_height)).perform(typeText("0"));
//        onView(withId(R.id.posBtn)).perform(click());
//        onView(withText(String.format("Your estimated stride length is %.2f inches.", 6 * 12 *
//                0.413))).inRoot(withDecorView(not(is(activity.getWindow().getDecorView())))).
//                check(matches(isDisplayed()));
//    }
//
//    private static Matcher<View> childAtPosition(
//            final Matcher<View> parentMatcher, final int position) {
//
//        return new TypeSafeMatcher<View>() {
//            @Override
//            public void describeTo(Description description) {
//                description.appendText("Child at position " + position + " in parent ");
//                parentMatcher.describeTo(description);
//            }
//
//            @Override
//            public boolean matchesSafely(View view) {
//                ViewParent parent = view.getParent();
//                return parent instanceof ViewGroup && parentMatcher.matches(parent)
//                        && view.equals(((ViewGroup) parent).getChildAt(position));
//            }
//        };
//    }
//}
