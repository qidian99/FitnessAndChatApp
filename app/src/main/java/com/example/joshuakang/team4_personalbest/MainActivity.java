package com.example.joshuakang.team4_personalbest;

import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements HeightPrompter.HeightPrompterListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPreferences = getSharedPreferences("user_height", MODE_PRIVATE);
        String magnitude = sharedPreferences.getString("magnitude", "");
        String metric = sharedPreferences.getString("metric", "");

        if(magnitude == ""){
            showHeightPrompt();
        } else {
            TextView textHeight = findViewById(R.id.textHeight);
            textHeight.setText("Hi, your height is " + magnitude + " " + metric);
        }

    }


    private void showHeightPrompt() {
        FragmentManager fm = getSupportFragmentManager();
        HeightPrompter editNameDialogFragment = HeightPrompter.newInstance(getString(R.string.heightPrompt));
        editNameDialogFragment.show(fm, "fragment_prompt_height");
    }

    @Override
    public void onFinishEditDialog(String[] inputText) {
        TextView textHeight = findViewById(R.id.textHeight);
        SharedPreferences sharedPreferences = getSharedPreferences("user_height", MODE_PRIVATE);
        SharedPreferences.Editor editor =   sharedPreferences.edit();
        editor.putString("magnitude", inputText[0]);
        editor.putString("metric", inputText[1]);
        editor.apply();
        textHeight.setText("Hi, your height is " + inputText[0] + " " + inputText[1]);
        Toast.makeText(this, "Height saved", Toast.LENGTH_SHORT);
    }
}
