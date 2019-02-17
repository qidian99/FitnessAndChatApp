package edu.ucsd.cse110.googlefitapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.fitness.request.DataReadRequest;

import org.apache.tools.ant.Main;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Calendar;

import androidx.test.InstrumentationRegistry;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessService;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessServiceFactory;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
public class HeightPrompterUnitTest {
    @Test
    public void testNullNewInstance() {
        HeightPrompter newHeightPrompter = HeightPrompter.newInstance(null);
        assertNull(newHeightPrompter);
    }

    @Test
    public void testNotNullNewInstance() {
        HeightPrompter newHeightPrompter = HeightPrompter.newInstance("Test");
        assertNotNull(newHeightPrompter);
        assertNotNull(newHeightPrompter.getArguments());
        assertEquals(newHeightPrompter.getArguments().getString("title"), "Test");
        assertTrue(newHeightPrompter.isCancelable());
    }
}