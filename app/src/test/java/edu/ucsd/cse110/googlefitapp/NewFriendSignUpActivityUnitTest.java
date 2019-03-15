package edu.ucsd.cse110.googlefitapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.fitness.request.DataReadRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowToast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;

import edu.ucsd.cse110.googlefitapp.fitness.FitnessService;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessServiceFactory;
import edu.ucsd.cse110.googlefitapp.fitness.GoogleFitnessServiceFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class NewFriendSignUpActivityUnitTest {
    NewFriendSignUpActivity activity;
    private Button button;
    private EditText text;

    @Before
    public void setUp() throws Exception {
        Intent intent = new Intent(RuntimeEnvironment.application, WeeklyStatsActivity.class);
        intent.putExtra("testkey", true);
        activity = Robolectric.buildActivity(NewFriendSignUpActivity.class, intent).create().get();
        button = activity.findViewById(R.id.btnSendFriendReq);
        text = activity.findViewById(R.id.enterFriendEmail);
        ArrayList<String> userList = new ArrayList<>();
        userList.add("da@ucsd.edu");
        userList.add("110@ucsd.edu");
        userList.add("enz@ucsd.edu");
        activity.setUserList(userList);
    }

    @Test
    public void testAddFriendFail() {
        ArrayList<String> users = activity.getUserList();
        assertEquals(3, users.size());
        text.setText("daw096@ucsd.edu");
        button.performClick();
        assertEquals("User does not exist!", ShadowToast.getTextOfLatestToast());
        assertEquals(3, users.size());
        ArrayList<String> friends = activity.getFriendList();
        assertEquals(0, friends.size());
    }

    @Test
    public void testAddSuccess() {
        text.setText("da@ucsd.edu");
        button.performClick();
        assertEquals("Request Send!", ShadowToast.getTextOfLatestToast());
        String friend = activity.getFriend(0);
        assertEquals("da@ucsd.edu", friend);
    }

    @Test
    public void testEmptyFriendList() {
        ArrayList<String> users = new ArrayList<>();
        activity.setUserList(users);
        text.setText("da@ucds.edu");
        button.performClick();
        assertEquals("User does not exist!", ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void testAddEmpty() {
        assertEquals("", text.getText().toString());
        ArrayList<String> users = activity.getUserList();
        assertEquals(3, users.size());
        ArrayList<String> friends = activity.getFriendList();
        assertEquals(0, friends.size());
        button.performClick();
        assertEquals("Email is invalid", ShadowToast.getTextOfLatestToast());
        assertEquals(3, users.size());
        assertEquals(0, friends.size());
    }

    @Test
    public void testInvalidEmail() {
        text.setText("dasfdjh");
        ArrayList<String> users = activity.getUserList();
        assertEquals(3, users.size());
        ArrayList<String> friends = activity.getFriendList();
        assertEquals(0, friends.size());
        button.performClick();
        assertEquals("Email is invalid", ShadowToast.getTextOfLatestToast());
        assertEquals(3, users.size());
        assertEquals(0, friends.size());
    }

    @Test
    public void testAddMultiple() {
        text.setText("da@ucsd.edu");
        ArrayList<String> users = activity.getUserList();
        assertEquals(3, users.size());
        ArrayList<String> friends = activity.getFriendList();
        assertEquals(0, friends.size());
        button.performClick();
        assertEquals("Request Send", ShadowToast.getTextOfLatestToast());
        assertEquals(1, friends.size());
        assertEquals("da@ucsd.edu", activity.getFriend(0));

        text.setText("110@ucsd.edu");
        button.performClick();
        assertEquals("Request Send", ShadowToast.getTextOfLatestToast());
        assertEquals(2, friends.size());
        assertEquals("da@ucsd.edu", activity.getFriend(0));
        assertEquals("110@ucsd.edu", activity.getFriend(1));

        text.setText("enz@ucsd.edu");
        button.performClick();
        assertEquals("Request Send", ShadowToast.getTextOfLatestToast());
        assertEquals(3, friends.size());
        assertEquals("da@ucsd.edu", activity.getFriend(0));
        assertEquals("110@ucsd.edu", activity.getFriend(1));
        assertEquals("enz@ucsd.edu", activity.getFriend(2));
    }

    @Test
    public void testBack() {
        activity.onBackPressed();
        assertTrue(activity.isFinishing());
    }
}