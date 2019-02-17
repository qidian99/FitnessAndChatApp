package edu.ucsd.cse110.googlefitapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Objects;

public class HeightPrompter extends DialogFragment implements TextView.OnEditorActionListener {
    public interface HeightPrompterListener {
        void onFinishEditDialog(String[] inputText);
    }

    private Window window;
    private EditText centText;
    private EditText ftText;
    private EditText inchText;
    private Spinner spinner;
    public HeightPrompter() {}

    public static HeightPrompter newInstance(String title) {
        if(title == null) {
            return null;
        }
        HeightPrompter frag = new HeightPrompter();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = Objects.requireNonNull(getActivity()).getLayoutInflater().inflate(R.layout.fragment_prompt_height, container, false);
        getDialog().setTitle(getString(R.string.heightPrompt));
        return v;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setCancelable(true);

        centText = view.findViewById(R.id.cent_height);
        ftText = view.findViewById(R.id.ft_height);
        inchText = view.findViewById(R.id.inch_height);
        spinner = view.findViewById(R.id.metricSpinner);
        centText.setOnEditorActionListener(this);
        Button posBtn = view.findViewById(R.id.posBtn);

        posBtn.setOnClickListener(v -> finishEnterHeight());

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int idx = spinner.getSelectedItemPosition();
                if( idx == 0 ){ // Use centimeters as metric
                    inchText.setVisibility(View.GONE);
                    ftText.setVisibility(View.GONE);
                    centText.setVisibility(View.VISIBLE);
                } else { // Use feet/inches as metric
                    inchText.setVisibility(View.VISIBLE);
                    ftText.setVisibility(View.VISIBLE);
                    centText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                inchText.setVisibility(View.GONE);
                ftText.setVisibility(View.GONE);
                centText.setVisibility(View.VISIBLE);
            }
        });

        // Show soft keyboard
        this.window = getDialog().getWindow();
        centText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                }
        });
        centText.requestFocus();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            return finishEnterHeight();
        }
        return false;
    }

    public boolean finishEnterHeight() {
        // Return input text back to activity through the implemented listener
        HeightPrompterListener listener = (HeightPrompterListener) getActivity();
        int height;
        int height2;
        // If centimeter is used as metric
        if (spinner.getSelectedItemPosition() == 0) {
            try {
                height = Integer.parseInt(centText.getText().toString());
                // Check for invalid input
                if (height <= 0 || height > 1000) {
                    throw new Exception("Invalid input");
                }
            } catch (Exception e) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                builder1.setMessage(getString(R.string.invalidHeight));
                builder1.setCancelable(false);

                builder1.setPositiveButton(
                        "OK",
                        (dialog, id) -> {
                            dialog.cancel();
                            centText.setText("");
                            centText.clearFocus();
                            centText.requestFocus();
                            window.setSoftInputMode(
                                    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                        });

                AlertDialog alertInvalidInput = builder1.create();
                alertInvalidInput.show();

                return false;
            }

            listener.onFinishEditDialog(new String[]{String.valueOf(spinner.getSelectedItemPosition()), String.valueOf(height)});
            dismiss();
            return true;
        } else { // Feet and inches are used
            try {
                height = Integer.parseInt(ftText.getText().toString());
                height2 = Integer.parseInt(inchText.getText().toString());
                // Check for invalid input
                if (height <= 0 || height > 50 || height2 <= 0 || height2 >= 12) {
                    throw new Exception("Invalid input");
                }
            } catch (Exception e) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                builder1.setMessage(getString(R.string.invalidHeight));
                builder1.setCancelable(false);

                builder1.setPositiveButton(
                        "OK",
                        (dialog, id) -> {
                            dialog.cancel();
                            ftText.setText("");
                            inchText.setText("");
                            window.setSoftInputMode(
                                    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                        });

                AlertDialog alertInvalidInput = builder1.create();
                alertInvalidInput.show();

                return false;
            }

            listener.onFinishEditDialog(new String[]{String.valueOf(spinner.getSelectedItemPosition()), String.valueOf(height), String.valueOf(height2)});
            dismiss();
            return true;
        }
    }
}