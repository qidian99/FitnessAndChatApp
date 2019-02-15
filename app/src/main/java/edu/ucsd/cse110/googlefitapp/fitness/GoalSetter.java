package edu.ucsd.cse110.googlefitapp.fitness;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import edu.ucsd.cse110.googlefitapp.R;

public class GoalSetter extends DialogFragment implements TextView.OnEditorActionListener {
    public interface GoalPrompterListener {
        void onFinishEditDialog(long goal);
    }


    private Window window;
    private EditText newGoalTxt;

    public GoalSetter() {}

    public static GoalSetter newInstance(String title) {
        GoalSetter frag = new GoalSetter();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_set_goal, container, false);
        getDialog().setTitle(getString(R.string.setGoalPrompt));
        return v;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        newGoalTxt = (EditText) view.findViewById(R.id.newGoal);
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

//        getDialog().getWindow().setSoftInputMode(
//                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            return finishEnterGoal();
        }
        return false;
    }

    public boolean finishEnterGoal() {
        // Return input text back to activity through the implemented listener
        GoalPrompterListener listener = (GoalPrompterListener) getActivity();
        long goal;

        try {
            goal = Integer.parseInt(newGoalTxt.getText().toString());
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