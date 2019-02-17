package edu.ucsd.cse110.googlefitapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class DataDisplayer extends DialogFragment {
    float distance;
    float speed;
    int steps;
    int sec;
    int min;

    private final String FORMAT_STR = "Steps: %d\n" +
                                      "Time elapsed: %d' %d\"\n" +
                                      "Distance: %.1f miles\n" +
                                      "Speed: %.1f miles/hour";

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
        View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_display_active_data, container, false);
        getDialog().setTitle(getString(R.string.prevSession));
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Button okButton = view.findViewById(R.id.Okbutton);
        final TextView data = view.findViewById(R.id.data);
        this.window = getDialog().getWindow();

        data.setText(String.format(FORMAT_STR, steps, min, sec, distance, speed));

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

}