package edu.ucsd.cse110.googlefitapp;

import android.app.AlertDialog;
import android.app.Dialog;
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

public class HeightPrompter extends DialogFragment implements TextView.OnEditorActionListener {
    public interface HeightPrompterListener {
        void onFinishEditDialog(String[] inputText);
    }


    private Window window;
    private EditText mEditText;
    private Spinner spinner;
    public HeightPrompter() {}

    public static HeightPrompter newInstance(String title) {
        HeightPrompter frag = new HeightPrompter();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return getActivity().getLayoutInflater().inflate(R.layout.fragment_prompt_height, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEditText = (EditText) view.findViewById(R.id.num_height);
        spinner = view.findViewById(R.id.metricSpinner);
        mEditText.setOnEditorActionListener(this);

        setCancelable(false);

        // Show soft keyboard
        this.window = getDialog().getWindow();
        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                               @Override
                                               public void onFocusChange(View v, boolean hasFocus) {
                                                   if (hasFocus) {
                                                       window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                                                   }
                                               }
                                           });
        mEditText.requestFocus();

//        getDialog().getWindow().setSoftInputMode(
//                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            // Return input text back to activity through the implemented listener
            HeightPrompterListener listener = (HeightPrompterListener) getActivity();
            int height;
            try {
                height = Integer.parseInt(mEditText.getText().toString());
                // Check for invalid input
                if( height < 0 || height > 1000 ) {
                    throw new Exception("Invalid input");
                }
            } catch (Exception e){
                AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                builder1.setMessage(getString(R.string.invalidHeight));
                builder1.setCancelable(false);

                builder1.setPositiveButton(
                        "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                mEditText.setText("");
                                mEditText.clearFocus();
                                mEditText.requestFocus();
                                window.setSoftInputMode(
                                        WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                            }
                        });

                AlertDialog alertInvalidInput = builder1.create();
                alertInvalidInput.show();

                return false;
            }

            listener.onFinishEditDialog(new String[] {String.valueOf(height), spinner.getSelectedItem().toString()});
            dismiss();
            return true;
        }
        return false;
    }

}