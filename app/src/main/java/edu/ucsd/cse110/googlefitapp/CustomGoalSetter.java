package edu.ucsd.cse110.googlefitapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
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

public class CustomGoalSetter extends DialogFragment implements TextView.OnEditorActionListener {
    private final String TAG = "CustomGoalSetter";

    public interface GoalPrompterListener {
        void onFinishEditDialog(int goal);
    }

    private Window window;
    private EditText newGoalTxt;

    public CustomGoalSetter() {}

    public static CustomGoalSetter newInstance(String title) {
        if(title == null) {
            return null;
        }
        CustomGoalSetter frag = new CustomGoalSetter();
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
            v = getActivity().getLayoutInflater().inflate(R.layout.fragment_set_goal, container, false);
            getDialog().setTitle(getString(R.string.setGoalPrompt));
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

            newGoalTxt = view.findViewById(R.id.newGoal);
            this.newGoalTxt.setOnEditorActionListener(this);
            Button doneBtn = view.findViewById(R.id.doneBtn);

            doneBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finishEnterGoal();
                }
            });

            setCancelable(true);

            // Show soft keyboard
            this.window = getDialog().getWindow();
            this.newGoalTxt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                                   @Override
                                                   public void onFocusChange(View v, boolean hasFocus) {
                                                       if (hasFocus) {
                                                           window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                                                       }
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
            Log.d(TAG, "finishEnterGoal Fail, " + e.toString());
            return false;
        }
        Log.d(TAG, "finishEnterGoal Success");
        listener.onFinishEditDialog(goal);
        dismiss();
        return true;
    }
}