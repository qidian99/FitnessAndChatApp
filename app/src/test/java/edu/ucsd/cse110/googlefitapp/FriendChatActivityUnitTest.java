package edu.ucsd.cse110.googlefitapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.EditText;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
public class FriendChatActivityUnitTest {
    private FriendChatActivity activity;
    private Button button;
    private EditText nameView;
    private SharedPreferences sharedPreferences;
    private String FROM_KEY = "from";
    private Intent intent;

    @Before
    public void setUp() {
        intent = new Intent(RuntimeEnvironment.application, WeeklyStatsActivity.class);
        intent.putExtra("testkey", true);
        activity = Robolectric.buildActivity(FriendChatActivity.class, intent).create().get();
        button = activity.findViewById(R.id.btn_send);
        nameView = activity.findViewById((R.id.user_name));
        sharedPreferences = activity.getSharedPreferences("FirebaseLabApp", Context.MODE_PRIVATE);
    }

    @Test
    public void testFrom() {
        String from = sharedPreferences.getString(FROM_KEY, null);
        assertNull(from);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(FROM_KEY, "da@ucsd.edu");
        editor.apply();
        activity = Robolectric.buildActivity(FriendChatActivity.class, intent).create().get();
        nameView = activity.findViewById((R.id.user_name));
        assertEquals("da@ucsd.edu", nameView.getText().toString());
    }

    @Test
    public void testNullFrom() {
        nameView = activity.findViewById((R.id.user_name));
        assertEquals("", nameView.getText().toString());
    }

    @Test
    public void testSharedPrefAfter() {
        String from = sharedPreferences.getString(FROM_KEY, null);
        assertNull(from);
        nameView = activity.findViewById((R.id.user_name));
        nameView.setText("test");
        from = sharedPreferences.getString(FROM_KEY, null);
        assertEquals("test", from);
    }
}
