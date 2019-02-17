package edu.ucsd.cse110.googlefitapp;

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

public class ManualStepSetter extends DialogFragment {
    public ManualStepSetter() {}
    public interface ManualStepSetterListener {
        void onFinishEditDialog(int[] inputStep);
    }

    private Window window;
    private EditText stepText;


    public static ManualStepSetter newInstance(String title) {
        ManualStepSetter frag = new ManualStepSetter();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_prompt_step, container, false);
        getDialog().setTitle(getString(R.string.stepPrompt));
        return v;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
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
    }

    public boolean finishEnterStep() {
        ManualStepSetter.ManualStepSetterListener listener = (ManualStepSetter.ManualStepSetterListener) getActivity();
        int step;
        try {
            step = Integer.parseInt(stepText.getText().toString());
            // Check for invalid input
            if (step <= 0) {
                throw new Exception("Invalid input");
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

            return false;
        }

        listener.onFinishEditDialog(new int[]{step});
        dismiss();
        return true;

    }
}