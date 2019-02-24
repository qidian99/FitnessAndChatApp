package edu.ucsd.cse110.googlefitapp.dialog;

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

import edu.ucsd.cse110.googlefitapp.R;

public class PlannedWalkEndingDialog extends DialogFragment {
    private static final String TAG = "PlannedWalkEndingDialog";
    private final String FORMAT_STR = "Steps: %d\n" +
            "Time elapsed: %d' %d\"\n" +
            "Distance: %.1f miles\n" +
            "Speed: %.1f miles/hour";
    float distance;
    float speed;
    int steps;
    int sec;
    int min;
    private TextView data;
    private Button okButton;
    private Window window;

    public PlannedWalkEndingDialog() {
    }

    @SuppressLint("ValidFragment")
    public PlannedWalkEndingDialog(float distance, float speed, int steps, int min, int sec) {
        this.distance = distance;
        this.speed = speed;
        this.steps = steps;
        this.sec = sec;
        this.min = min;
    }

    public static PlannedWalkEndingDialog newInstance(String title, float distance, float speed, int steps, int min, int sec) {
        PlannedWalkEndingDialog frag = new PlannedWalkEndingDialog(distance, speed, steps, min, sec);
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
            Log.d(TAG, "onCreateView Success");
        } catch (Exception e) {
            Log.d(TAG, "onCreateView Fail: " + e.toString());
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
            Log.d(TAG, "onViewCreated Success");

            data.setText(String.format(FORMAT_STR, steps, min, sec, distance, speed));
        } catch (Exception e) {
            Log.d(TAG, "onViewCreated Fail: " + e.toString());
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
            Log.d(TAG, "session dismiss fail: " + e.toString());
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