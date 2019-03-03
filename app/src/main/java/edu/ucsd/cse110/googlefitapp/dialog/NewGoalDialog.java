package edu.ucsd.cse110.googlefitapp.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import edu.ucsd.cse110.googlefitapp.R;

public class NewGoalDialog extends DialogFragment {
    public static final int SUGGESTED_GOAL_INCREMENT = 500;
    private final String TAG = "NewGoalDialog";
    private Window window;
    private EditText newGoalTxt;
    private int currentGoal;

    public NewGoalDialog() {
    }

    @SuppressLint("ValidFragment")
    public NewGoalDialog(int currentGoal) {
        this.currentGoal = currentGoal;
    }

    public static NewGoalDialog newInstance(String title, int currentGoal) {
        if (title == null) {
            return null;
        }
        NewGoalDialog frag = new NewGoalDialog(currentGoal);
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.setStyle(0, R.style.Dialog);
        return frag;
    }

    public int getCurrentGoal() {
        return this.currentGoal;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = null;
        try {
            v = Objects.requireNonNull(getActivity()).getLayoutInflater().inflate(R.layout.fragment_set_new_goal, container, false);
            getDialog().setTitle(getString(R.string.congratsPrompt));
            Log.d(TAG, "onCreateView Success");
        } catch (Exception e) {
            Log.d(TAG, "onCreateView Fail: " + e.toString());
            e.printStackTrace();
        }
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        try {
            super.onViewCreated(view, savedInstanceState);
            final Button btnYes = view.findViewById(R.id.btnYes);
            final Button btnNo = view.findViewById(R.id.btnNo);
            final TextView congrats = view.findViewById(R.id.congrats);
            final Button btnSuggested = view.findViewById(R.id.btnSuggested);
            final Button btnCustomed = view.findViewById(R.id.btnCustomed);
            final TextView suggestedGoal = view.findViewById(R.id.suggestedGoal);
            final TextView customGoal = view.findViewById(R.id.customGoal);
            newGoalTxt = view.findViewById(R.id.newGoal);
            this.window = getDialog().getWindow();

            btnYes.setOnClickListener(v -> {
                Log.d(TAG, "onViewCreated click yes");
                btnYes.setVisibility(View.GONE);
                btnNo.setVisibility(View.GONE);
                congrats.setVisibility(View.GONE);
                suggestedGoal.setVisibility(View.VISIBLE);
                btnSuggested.setVisibility(View.VISIBLE);
                btnSuggested.setText(String.valueOf(currentGoal + SUGGESTED_GOAL_INCREMENT));
                btnCustomed.setVisibility(View.VISIBLE);
                customGoal.setVisibility(View.VISIBLE);
                newGoalTxt.setVisibility(View.VISIBLE);
                getDialog().setTitle(getString(R.string.newGoalPrompt));
                btnSuggested.setOnClickListener(v12 -> {
                    Log.d(TAG, "onViewCreated click suggested goal");
                    finishEnterGoal(btnSuggested.getText().toString());
                    Toast.makeText(getContext(), "Goal Updated", Toast.LENGTH_SHORT).show();
                });
                btnCustomed.setOnClickListener(v1 -> {
                    Log.d(TAG, "onViewCreated click custom goal");
                    finishEnterGoal(newGoalTxt.getText().toString());
                });
            });

            btnNo.setOnClickListener(v -> {
                Log.d(TAG, "onViewCreated click no");
                dismiss();
            });
        } catch (Exception e) {
            Log.d(TAG, "onViewCreated Fail: " + e.toString());
            e.printStackTrace();
        }
    }

    public void finishEnterGoal(String goalStr) {
        // Return input text back to activity through the implemented listener
        CustomGoalDialog.GoalPrompterListener listener = (CustomGoalDialog.GoalPrompterListener) getActivity();
        int goal;

        try {
            goal = Integer.parseInt(goalStr);
            // Check for invalid input
            if (goal <= 0 || goal >= 100000) {
                throw new Exception("Please Try a New Goal.");
            }
        } catch (Exception e) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
            builder1.setMessage(getString(R.string.invalidGoal));
            builder1.setCancelable(false);

            builder1.setPositiveButton(
                    "OK",
                    (dialog, id) -> {
                        dialog.cancel();
                        newGoalTxt.setText("");
                        newGoalTxt.clearFocus();
                        newGoalTxt.requestFocus();
                        window.setSoftInputMode(
                                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                    });

            AlertDialog alertInvalidInput = builder1.create();
            alertInvalidInput.show();
            Log.d(TAG, "finishEnterGoal Fail, " + e.toString());
            return;
        }

        Log.d(TAG, "finishEnterGoal Success");
        Objects.requireNonNull(listener).onFinishEditDialog(goal);
        dismiss();
    }
}