package edu.ucsd.cse110.team4personalbest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class NewFriendSignUp extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friend);

    }

    public void goBackToHomeScreen(View view) {
        finish();
    }
}