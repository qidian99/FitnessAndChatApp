package com.example.joshuakang.team4_personalbest;

import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
        float strideLength = sharedPreferences.getFloat("stride", 0);

        if(magnitude.equals("") || metric.equals("") || strideLength == 0){
            showHeightPrompt();
        } else {
            TextView textHeight = findViewById(R.id.textHeight);
            textHeight.setText(String.format("Your stride length is estimated to be %.2f feet.", strideLength));
        }

        // In development, we allow users to re-enter their heights
        Button clearBtn = findViewById(R.id.clearBtn);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHeightPrompt();
            }
        });
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

        // Estimate stride length, in inches
        float strideLength = 0;
        // Case 1: use centimeter as metric
        if(inputText[1].equals("cm")){
            strideLength = (float) (Integer.parseInt(inputText[0]) / 2.54 * 0.413);
        }
        // Case 2: use feet as metric
        else {
            strideLength = (float) (Integer.parseInt(inputText[0]) * 0.413);
        }
        editor.putFloat("stride", strideLength);

        textHeight.setText(String.format("Your stride length is estimated to be %.2f feet.", strideLength));

        Toast.makeText(this, "Height saved", Toast.LENGTH_SHORT);
    }
}
