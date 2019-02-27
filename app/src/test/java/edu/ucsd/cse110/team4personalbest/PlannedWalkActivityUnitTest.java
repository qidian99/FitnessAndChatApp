package edu.ucsd.cse110.team4personalbest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.fitness.request.DataReadRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Calendar;

import edu.ucsd.cse110.team4personalbest.fitness.FitnessService;
import edu.ucsd.cse110.team4personalbest.fitness.FitnessServiceFactory;
import edu.ucsd.cse110.team4personalbest.fitness.GoogleFitnessServiceFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class PlannedWalkActivityUnitTest {
    private static final String TEST_SERVICE = "TEST_SERVICE_STEP_COUNT_ACT";
    Button btnUpdateSteps;
    private PlannedWalkActivity activity;
    private TextView textSteps;
    private TextView textDist;
    private TextView textSpeed;
    private TextView textTime;
    private Button end;
    private int nextStepCount;

    @Before
    public void setUp() throws Exception {
        FitnessServiceFactory googleFitnessServiceFactory = new GoogleFitnessServiceFactory();

        googleFitnessServiceFactory.put(TEST_SERVICE, new FitnessServiceFactory.BluePrint() {
            @Override
            public FitnessService create(Activity plannedWalkActivity) {
                return new TestFitnessService(plannedWalkActivity);
            }
        });

        Intent intent = new Intent(RuntimeEnvironment.application, PlannedWalkActivity.class);
        intent.putExtra(PlannedWalkActivity.FITNESS_SERVICE_KEY, TEST_SERVICE);
        activity = Robolectric.buildActivity(PlannedWalkActivity.class, intent).create().get();

        textSteps = activity.findViewById(R.id.textSteps);
        textDist = activity.findViewById(R.id.textDistance);
        textSpeed = activity.findViewById(R.id.textSpeed);
        textTime = activity.findViewById(R.id.timer_text);
        end = activity.findViewById(R.id.btnEndRecord);
        btnUpdateSteps = activity.findViewById(R.id.buttonUpdateSteps);
        nextStepCount = 2000;
    }

    @Test
    public void testStepUpdate() {
        assertEquals("0", textSteps.getText().toString());
        activity.setStepCount(nextStepCount);
        assertEquals("2000", textSteps.getText().toString());
    }

    @Test
    public void testInitalStep() {
        SharedPreferences sharedPref = activity.getSharedPreferences("stepCountData", activity.MODE_PRIVATE);
        assertTrue(sharedPref.getInt("initialSteps", -1) == 0);
        activity.setStepCount(nextStepCount);
        assertTrue(sharedPref.getInt("initialSteps", -1) == 0);
    }

    @Test
    public void testDistanceUpdate() {
        assertEquals("0.0 miles", textDist.getText().toString());
        activity.setStepCount(nextStepCount);
        activity.setStrideLen(20);
        activity.setDistance();
        assertEquals("0.6 miles", textDist.getText().toString());
    }

    @Test
    public void testSpeedUpdate() {
        assertEquals("0.0 MPH", textSpeed.getText().toString());
        activity.setDistance(1);
        activity.setTime(10000);
        activity.setSpeed();
        assertEquals("0.4 MPH", textSpeed.getText().toString());
    }

    @Test
    public void testTimeUpdate() {
        assertEquals("0:00", textTime.getText().toString());
        activity.setTime(1000);
        activity.setTime();
        assertEquals("16:40", textTime.getText().toString());
    }

    @Test
    public void testWholeMin() {
        assertEquals("0:00", textTime.getText().toString());
        activity.setTime(3600);
        activity.setTime();
        assertEquals("60:00", textTime.getText().toString());
    }

    @Test
    public void testEndButton() {
        end.performClick();
        assertTrue(activity.isFinishing());
    }

    @Test
    public void testStepUpdateButton() {
        activity.setStepCount(nextStepCount);
        btnUpdateSteps.performClick();
        assertEquals("2000", textSteps.getText().toString());
    }

    private class TestFitnessService implements FitnessService {
        private static final String TAG = "[TestStepCountActFitnessService]: ";
        private Activity plannedWalkActivity;

        public TestFitnessService(Activity plannedWalkActivity) {
            this.plannedWalkActivity = plannedWalkActivity;
        }

        @Override
        public int getRequestCode() {
            return 0;
        }

        @Override
        public void setup() {
            Log.d(TAG, "setup");
        }

        @Override
        public void updateStepCount() {
            Log.d(TAG, "updateStepCount");
            ((PlannedWalkActivity) plannedWalkActivity).setStepCount(nextStepCount);
            plannedWalkActivity.updateAll(nextStepCount);
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