package edu.ucsd.cse110.googlefitapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class DataDisplayer extends DialogFragment {
    float distance;
    float speed;
    int steps;
    int sec;
    int min;
    private static final String TAG = "[TestDataDisplayer]: ";

    private final String FORMAT_STR = "Steps: %d\n" +
                                      "Time elapsed: %d' %d\"\n" +
                                      "Distance: %.1f miles\n" +
                                      "Speed: %.1f miles/hour";

    private TextView data;
    private Button okButton;

    public DataDisplayer() {}

    @SuppressLint("ValidFragment")
    public DataDisplayer(float distance, float speed, int steps, int min, int sec) {
        this.distance = distance;
        this.speed = speed;
        this.steps = steps;
        this.sec = sec;
        this.min = min;
    }

    private Window window;

    public static DataDisplayer newInstance(String title, float distance, float speed, int steps, int min, int sec) {
        DataDisplayer frag = new DataDisplayer(distance, speed, steps, min, sec);
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = null;
        try {
            v = getActivity().getLayoutInflater().inflate(R.layout.fragment_display_active_data, container, false);
            getDialog().setTitle(getString(R.string.prevSession));
            Log.d(TAG, "Create view succeed");
        } catch (Exception e) {
            Log.d(TAG, "Create view failed" + e.toString());
            e.printStackTrace();
        }
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        try {
            super.onViewCreated(view, savedInstanceState);
            okButton = view.findViewById(R.id.Okbutton);
            data = view.findViewById(R.id.data);
            this.window = getDialog().getWindow();
            Log.d(TAG, "onViewCreated success");

            data.setText(String.format(FORMAT_STR, steps, min, sec, distance, speed));
        } catch (Exception e) {
            Log.d(TAG, "onViewCreated failed" + e.toString());
            e.printStackTrace();
        }

        try {
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    Log.d(TAG, "floating window dismissed");
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "session dismission failed" + e.toString());
            e.printStackTrace();
        }
    }

    public String getData() {
        return String.format(FORMAT_STR, steps, min, sec, distance, speed);
    }

    public float getDistance() {
        return distance;
    }

    public float getSpeed() {
        return speed;
    }
}