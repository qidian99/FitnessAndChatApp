package edu.ucsd.cse110.googlefitapp;

import android.app.AlertDialog;
import android.os.Bundle;
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

public class ManualStepSetter extends DialogFragment {
    private final String TAG = "ManualStepSetter";
    private Window window;
    private EditText stepText;

    public ManualStepSetter() {
    }

    public static ManualStepSetter newInstance(String title) {
        if (title == null) {
            return null;
        }
        ManualStepSetter frag = new ManualStepSetter();
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
            v = getActivity().getLayoutInflater().inflate(R.layout.fragment_prompt_step, container, false);
            getDialog().setTitle(getString(R.string.stepPrompt));
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

            setCancelable(true);

            stepText = view.findViewById(R.id.num_steps);
            Button posBtn = view.findViewById(R.id.stepPosBtn);

            posBtn.setOnClickListener(v -> finishEnterStep());

            // Show soft keyboard
            this.window = getDialog().getWindow();
            stepText.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                }
            });
            stepText.requestFocus();
            Log.d(TAG, "onViewCreated Success");
        } catch (Exception e) {
            Log.d(TAG, "onViewCreated Fail: " + e.toString());
            e.printStackTrace();
        }
    }

    public boolean finishEnterStep() {
        ManualStepSetter.ManualStepSetterListener listener = (ManualStepSetter.ManualStepSetterListener) getActivity();
        int step;
        try {
            step = Integer.parseInt(stepText.getText().toString());
            // Check for invalid input
            if (step <= 0 || step > 10000) {
                throw new Exception("Steps must be between 0(exclusive) and 10000(inclusive)");
            }
        } catch (Exception e) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
            builder1.setMessage(getString(R.string.invalidStep));
            builder1.setCancelable(false);

            builder1.setPositiveButton(
                    "OK",
                    (dialog, id) -> {
                        dialog.cancel();
                        stepText.setText("");
                        stepText.clearFocus();
                        stepText.requestFocus();
                        window.setSoftInputMode(
                                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                    });

            AlertDialog alertInvalidInput = builder1.create();
            alertInvalidInput.show();
            Log.d(TAG, "finishEnterStep Fail, " + e.toString());
            return false;
        }
        Log.d(TAG, "finishEnterStep Success");
        listener.onFinishEditDialog(new int[]{step});
        dismiss();
        return true;

    }

    public interface ManualStepSetterListener {
        void onFinishEditDialog(int[] inputStep);
    }
}