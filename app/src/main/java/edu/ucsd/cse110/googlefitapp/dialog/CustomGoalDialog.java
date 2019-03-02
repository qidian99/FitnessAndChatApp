package edu.ucsd.cse110.googlefitapp.dialog;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Objects;

import edu.ucsd.cse110.googlefitapp.R;

public class CustomGoalDialog extends DialogFragment implements TextView.OnEditorActionListener {
    private final String TAG = "CustomGoalDialog";
    private Window window;
    private EditText newGoalTxt;

    public CustomGoalDialog() {
    }

    public static CustomGoalDialog newInstance(String title) {
        if (title == null) {
            return null;
        }
        CustomGoalDialog frag = new CustomGoalDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.setStyle(0, R.style.Dialog);
        return frag;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = null;
        try {
            getDialog().setTitle(getString(R.string.setGoalPrompt));
            v = Objects.requireNonNull(getActivity()).getLayoutInflater().inflate(R.layout.fragment_set_goal, container, false);
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

            newGoalTxt = view.findViewById(R.id.newGoal);
            this.newGoalTxt.setOnEditorActionListener(this);
            Button doneBtn = view.findViewById(R.id.doneBtn);

            doneBtn.setOnClickListener(v -> finishEnterGoal());

            setCancelable(true);

            // Show soft keyboard
            this.window = getDialog().getWindow();
            this.newGoalTxt.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                }
            });
            this.newGoalTxt.requestFocus();
            Log.d(TAG, "onViewCreated Success");
        } catch (Exception e) {
            Log.d(TAG, "onViewCreated Fail: " + e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            Log.d(TAG, "onEditorAction Success");
            return finishEnterGoal();
        }
        return false;
    }

    public boolean finishEnterGoal() {
        // Return input text back to activity through the implemented listener
        GoalPrompterListener listener = (GoalPrompterListener) getActivity();
        int goal;

        try {
            goal = Integer.parseInt(newGoalTxt.getText().toString());
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
            return false;
        }
        Log.d(TAG, "finishEnterGoal Success");
        Objects.requireNonNull(listener).onFinishEditDialog(goal);
        dismiss();
        return true;
    }

    public interface GoalPrompterListener {
        void onFinishEditDialog(int goal);
    }
}