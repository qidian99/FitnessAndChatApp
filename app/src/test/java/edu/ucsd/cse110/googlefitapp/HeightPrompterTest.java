package edu.ucsd.cse110.googlefitapp;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.DialogFragment;

import org.apache.tools.ant.Main;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowToast;

import edu.ucsd.cse110.googlefitapp.fitness.FitnessService;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessServiceFactory;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import android.support.v4.app.FragmentManager;
import static org.junit.Assert.assertTrue;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

@RunWith(RobolectricTestRunner.class)
public class HeightPrompterTest {
    DialogFragment dialogFragment;

    @Before
    public void setUp() {
        MainActivity activity = Robolectric.setupActivity(MainActivity.class);
        dialogFragment = (DialogFragment) activity.getSupportFragmentManager()
                .findFragmentByTag("fragment_prompt_height");
    }

    @Test
    public void dialogFragmentIsShownToTheUser() {
        assertNotNull(dialogFragment);
    }

    @Test
    public void firstHeightCannotCancel() {
        assertFalse(dialogFragment.isCancelable());
    }
}