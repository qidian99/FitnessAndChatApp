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

public class NewGoalSetter extends DialogFragment {
    public NewGoalSetter() {}

    @SuppressLint("ValidFragment")
    public NewGoalSetter(long currentGoal) {
        this.currentGoal = currentGoal;
    }

    private Window window;
    private EditText newGoalTxt;
    private long currentGoal;
    public static final long SUGGESTED_GOAL_INCREMENT = 500L;

    public static NewGoalSetter newInstance(String title, long currentGoal) {
        NewGoalSetter frag = new NewGoalSetter(currentGoal);
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_set_new_goal, container, false);
        getDialog().setTitle(getString(R.string.congratsPrompt));
        return v;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
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

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                btnSuggested.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finishEnterGoal(btnSuggested.getText().toString());
                    }
                });
                btnCustomed.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finishEnterGoal(newGoalTxt.getText().toString());
                    }
                });
            }
        });

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public boolean finishEnterGoal(String goalStr) {
        // Return input text back to activity through the implemented listener
        CustomGoalSetter.GoalPrompterListener listener = (CustomGoalSetter.GoalPrompterListener) getActivity();
        long goal;

        try {
            goal = Integer.parseInt(goalStr);
            // Check for invalid input
            if (goal < 0 || goal > 100000) {
                throw new Exception("Please Try a New Goal.");
            }
        } catch (Exception e) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
            builder1.setMessage(getString(R.string.invalidGoal));
            builder1.setCancelable(false);

            builder1.setPositiveButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            newGoalTxt.setText("");
                            newGoalTxt.clearFocus();
                            newGoalTxt.requestFocus();
                            window.setSoftInputMode(
                                    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                        }
                    });

            AlertDialog alertInvalidInput = builder1.create();
            alertInvalidInput.show();

            return false;
        }

        listener.onFinishEditDialog(goal);
        dismiss();
        return true;
    }
}