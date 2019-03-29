package edu.ucsd.cse110.googlefitapp.dialog;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Objects;

import edu.ucsd.cse110.googlefitapp.Activity;
import edu.ucsd.cse110.googlefitapp.MonthlyStatsActivity;
import edu.ucsd.cse110.googlefitapp.R;
import edu.ucsd.cse110.googlefitapp.WeeklyStatsActivity;

public class ChooseStatsDialog extends DialogFragment {
    private static final String TAG = "ChooseStatsDialog";
    private static boolean test;
    private Button showWeeklyBtn;
    private Button showMonthlyBtn;
    private Activity activity;

    public ChooseStatsDialog() {
    }

    @SuppressLint("ValidFragment")
    public ChooseStatsDialog(Activity activity) {
        this.activity = activity;
    }

    public static ChooseStatsDialog newInstance(String title, Activity activity) {
        ChooseStatsDialog frag = new ChooseStatsDialog(activity);
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.setStyle(0, R.style.Dialog);
        test = activity.getIntent().getBooleanExtra("TEST", false);
        return frag;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = null;
        try {
            v = Objects.requireNonNull(getActivity()).getLayoutInflater().inflate(R.layout.fragment_choose_stats, container, false);
            getDialog().setTitle(getString(R.string.choose_stats));
            Log.d(TAG, "onCreateView Success");
        } catch (Exception e) {
            Log.d(TAG, "onCreateView Fail: " + e.toString());
            e.printStackTrace();
        }
        return v;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        try {
            super.onViewCreated(view, savedInstanceState);
            showWeeklyBtn = view.findViewById(R.id.btnWeekly);
            showMonthlyBtn = view.findViewById(R.id.btnMonthly);

            showWeeklyBtn.setOnClickListener(v -> {
                dismiss();
                launchWeeklyStats();
            });

            showMonthlyBtn.setOnClickListener(v -> {
                dismiss();
                launchMonthlyStats();
            });

        } catch (Exception e) {
            Log.e(TAG, "onViewCreated Fail: " + e.toString());
            e.printStackTrace();
        }
    }

    private void launchMonthlyStats() {
        try {
            Intent intent = new Intent(activity, MonthlyStatsActivity.class);
            intent.putExtra("TEST", test);
            startActivity(intent);
            Log.d(TAG, getString(R.string.launchMonthlyStatsSuccess));

        } catch (Exception e) {
            Log.d(TAG, getString(R.string.launchMonthlyStatsFailure) + e.toString());
            e.printStackTrace();
        }
    }

    private void launchWeeklyStats() {
        try {
            Intent intent = new Intent(activity, WeeklyStatsActivity.class);
            intent.putExtra("TEST", test);
            startActivity(intent);
            Log.d(TAG, getString(R.string.launchWeeklyStatsSuccess));

        } catch (Exception e) {
            Log.d(TAG, getString(R.string.launchWeeklyStatsFailure) + e.toString());
            e.printStackTrace();
        }
    }
}
