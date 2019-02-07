package edu.ucsd.cse110.googlefitapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import edu.ucsd.cse110.googlefitapp.fitness.FitnessService;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessServiceFactory;
import edu.ucsd.cse110.googlefitapp.fitness.GoogleFitAdapter;

public class MainActivity extends AppCompatActivity implements HeightPrompter.HeightPrompterListener {
    private String fitnessServiceKey = "GOOGLE_FIT";

    public static final String showStride = "Your estimated stride length is %.2f inches.";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FitnessServiceFactory.put(fitnessServiceKey, new FitnessServiceFactory.BluePrint() {
            @Override
            public FitnessService create(StepCountActivity stepCountActivity) {
                return new GoogleFitAdapter(stepCountActivity);
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("user_height", MODE_PRIVATE);
        String magnitude = sharedPreferences.getString("magnitude", "");
        String metric = sharedPreferences.getString("metric", "");
        float strideLength = sharedPreferences.getFloat("stride", 0);

        if(magnitude.equals("") || metric.equals("") || strideLength == 0){
            showHeightPrompt();
        } else {
            TextView textHeight = findViewById(R.id.textHeight);
            textHeight.setText(String.format(showStride, strideLength));
        }

        // In development, we allow users to re-enter their heights
        Button clearBtn = findViewById(R.id.clearBtn);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHeightPrompt();
            }
        });

        Button btnGoToSteps = findViewById(R.id.startBtn);
        btnGoToSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchStepCountActivity();
            }
        });

    }

    public void launchStepCountActivity() {
        Intent intent = new Intent(this, StepCountActivity.class);
        intent.putExtra(StepCountActivity.FITNESS_SERVICE_KEY, fitnessServiceKey);
        startActivity(intent);
    }

    public void setFitnessServiceKey(String fitnessServiceKey) {
        this.fitnessServiceKey = fitnessServiceKey;
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
        if(Integer.parseInt(inputText[0]) == 0 ){
            strideLength = (float) (Integer.parseInt(inputText[1]) / 2.54 * 0.413);
        }
        // Case 2: use feet as metric
        else {
            strideLength = (float) ((Integer.parseInt(inputText[1])*12 + Integer.parseInt(inputText[2])) * 0.413);
        }
        editor.putFloat("stride", strideLength);

        textHeight.setText(String.format(showStride, strideLength));

        Toast.makeText(this, "Height saved", Toast.LENGTH_SHORT);
    }
}
