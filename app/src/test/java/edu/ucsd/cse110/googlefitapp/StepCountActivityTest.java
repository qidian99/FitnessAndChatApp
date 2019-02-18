package edu.ucsd.cse110.googlefitapp;

import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.fitness.request.DataReadRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowToast;

import java.util.Calendar;

import edu.ucsd.cse110.googlefitapp.fitness.FitnessService;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessServiceFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class StepCountActivityTest {
    private static final String TEST_SERVICE = "TEST_SERVICE_STEP_COUNT_ACT";

    private StepCountActivity activity;
    private TextView textSteps;
    private TextView textDist;
    private TextView textSpeed;
    private Button btnUpdateSteps;
    private int nextStepCount;

    @Before
    public void setUp() throws Exception {
        FitnessServiceFactory.put(TEST_SERVICE, new FitnessServiceFactory.BluePrint() {
            @Override
            public FitnessService create(StepCountActivity stepCountActivity) {
                return new TestFitnessService(stepCountActivity);
            }

            @Override
            public FitnessService create(MainActivity mainActivity) {
                return null;
            }
        });

        Intent intent = new Intent(RuntimeEnvironment.application, StepCountActivity.class);
        intent.putExtra(StepCountActivity.FITNESS_SERVICE_KEY, TEST_SERVICE);
        activity = Robolectric.buildActivity(StepCountActivity.class, intent).create().get();

        textSteps = activity.findViewById(R.id.textSteps);
        textDist = activity.findViewById(R.id.textDistance);
        textSpeed = activity.findViewById(R.id.textSpeed);
        nextStepCount = 2000;
    }

    @Test
    public void testStepUpdate(){
        assertEquals("Steps will be shown here", textSteps.getText().toString());
        activity.setStepCount(nextStepCount);
        assertEquals("2000", textSteps.getText().toString());
    }

    /*
    private class TestFitnessService implements FitnessService {
        private static final String TAG = "[TestFitnessService]: ";
        private StepCountActivity stepCountActivity;

        public TestFitnessService(StepCountActivity stepCountActivity) {
            this.stepCountActivity = stepCountActivity;
        }

        @Override
        public int getRequestCode() {
            return 0;
        }

        @Override
        public void setup() {
            System.out.println(TAG + "setup");
        }

        @Override
        public void updateStepCount() {
            System.out.println(TAG + "updateStepCount");
            stepCountActivity.setStepCount(nextStepCount);
        }

        @Override
        public void stopAsync() {
            //TODO : this needs to be done for the test for the refresh every 7 seconds
        }

        @Override
        public void updateActivity() {
            //TODO : this needs to be done for the test for the refresh every 7 seconds

        }

        @Override
        public void startAsync() {
            //TODO : this needs to be done for the test for the refresh every 7 seconds

        }
    }*/

    private class TestFitnessService implements FitnessService {
        private static final String TAG = "[TestStepCountActFitnessService]: ";
        private StepCountActivity stepCountActivity;

        public TestFitnessService(StepCountActivity stepCountActivity) {
            this.stepCountActivity = stepCountActivity;
        }

        @Override
        public int getRequestCode() {
            return 0;
        }

        @Override
        public void setup() {
            System.out.println(TAG + "setup");
        }

        @Override
        public void updateStepCount() {
            Log.d(TAG, "updateStepCount");
            stepCountActivity.setStepCount(nextStepCount);
            stepCountActivity.updateAll(nextStepCount);
        }

        @Override
        public void stopAsync() {
        }

        @Override
        public void startAsync() {
        }

        @Override
        public boolean hasPermission() {
            return false;
        }

        @Override
        public void addInactiveSteps(int extraStep) {
        }

        @Override
        public void addActiveSteps(int step) {
        }

        @Override
        public DataReadRequest getLast7DaysSteps(double[] weeklyInactiveSteps, double[] weeklyActiveSteps) {
            return null;
        }

        @Override
        public DataReadRequest getLast7DaysSteps(double[] weeklyInactiveSteps, double[] weeklyActiveSteps, Calendar cal) {
            return null;
        }
    }
}