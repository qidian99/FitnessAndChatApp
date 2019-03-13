package edu.ucsd.cse110.googlefitapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import edu.ucsd.cse110.googlefitapp.dialog.NewGoalDialog;
import edu.ucsd.cse110.googlefitapp.dialog.PlannedWalkEndingDialog;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessService;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessServiceFactory;
import edu.ucsd.cse110.googlefitapp.fitness.GoogleFitnessServiceFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class PlannedWalkEndingDialogUnitTest {
    private static final String TEST_SERVICE = "TEST_ACTIVE_STATS_DIALOG";

    private PlannedWalkEndingDialog plannedWalkEndingDialog;
    private MainActivity activity;
    private DialogFragment dialogFragment;
    private float distance = 10;
    private float speed = 10;
    private int steps = 10;
    private int min = 10;
    private int sec = 10;

    @Before
    public void setUp() throws Exception {
        FitnessServiceFactory googleFitnessServiceFactory = new GoogleFitnessServiceFactory();

        googleFitnessServiceFactory.put(TEST_SERVICE, new FitnessServiceFactory.BluePrint() {
            @Override
            public FitnessService create(Activity mainActivity) {
                return new PlannedWalkEndingDialogUnitTest.TestFitnessService(mainActivity);
            }
        });

        Intent intent = new Intent(RuntimeEnvironment.application, MainActivity.class);
        intent.putExtra("TEST", true);
        intent.putExtra("TEST_SERVICE_MAIN", TEST_SERVICE);
        intent.putExtra("TEST_SERVICE_STEP_COUNT", TEST_SERVICE);
        activity = Robolectric.buildActivity(MainActivity.class, intent).create().get();
        FragmentManager fm = activity.getSupportFragmentManager();
        dialogFragment = NewGoalDialog.newInstance(activity.getString(R.string.congratsPrompt), 0);
        dialogFragment.show(fm, "fragment_display_active_data");
    }

    @Test
    public void testIsShown() {
        assertNotNull(dialogFragment);
        assertTrue(dialogFragment.getShowsDialog());
    }

    @Test
    public void testDialogCancelable() {
        assertTrue(dialogFragment.isCancelable());
    }

    @Test
    public void testHeightDialogBindToMain() {
        assertNotNull(dialogFragment.getActivity());
        assertEquals(activity, dialogFragment.getContext());
    }

    @Test
    public void testHeightDialogTag() {
        assertEquals("fragment_display_active_data", dialogFragment.getTag());
    }

    @Test
    public void testNullTitle() {
        plannedWalkEndingDialog = PlannedWalkEndingDialog.newInstance(null, distance, speed, steps, min, sec);
        Bundle bundle = plannedWalkEndingDialog.getArguments();
        assertNotNull(bundle);
        String title = bundle.getString("title");
        assertNull(title);
    }

    @Test
    public void testTitle() {
        plannedWalkEndingDialog = PlannedWalkEndingDialog.newInstance("title", distance, speed, steps, min, sec);
        Bundle bundle = plannedWalkEndingDialog.getArguments();
        Assert.assertNotNull(bundle);
        String title = bundle.getString("title");
        assertEquals("title", title);
        assertEquals(distance, plannedWalkEndingDialog.getDistance(), 1e-5);
        assertEquals(min, plannedWalkEndingDialog.getMin());
        assertEquals(sec, plannedWalkEndingDialog.getSec());
        assertEquals(speed, plannedWalkEndingDialog.getSec(), 1e-5);
        assertEquals(steps, plannedWalkEndingDialog.getSteps());
    }

    @Test
    public void testEmptyObject() {
        plannedWalkEndingDialog = new PlannedWalkEndingDialog();
        assertTrue(0 == plannedWalkEndingDialog.getDistance());
        assertTrue(0 == plannedWalkEndingDialog.getSpeed());
        String data = plannedWalkEndingDialog.getData();
        assertNotNull(data);
        assertEquals("Steps: 0\n" +
                "Time elapsed: 0' 0\"\n" +
                "Distance: 0.0 miles\n" +
                "Speed: 0.0 miles/hour", data);
    }

    @Test
    public void testNewInstance() {
        plannedWalkEndingDialog = PlannedWalkEndingDialog.newInstance("new instance", distance, speed, steps, min, sec);
        assertNotNull(plannedWalkEndingDialog);
        Bundle bundle = plannedWalkEndingDialog.getArguments();
        assertNotNull(bundle);
        String title = bundle.getString("title");
        assertEquals("new instance", title);
    }

    @Test
    public void testIntData() {
        plannedWalkEndingDialog = PlannedWalkEndingDialog.newInstance("new instance", distance, speed, steps, min, sec);
        String data = plannedWalkEndingDialog.getData();
        assertNotNull(data);
        assertEquals("Steps: 10\n" +
                "Time elapsed: 10' 10\"\n" +
                "Distance: 10.0 miles\n" +
                "Speed: 10.0 miles/hour", data);
    }

    @Test
    public void testZeroData() {
        plannedWalkEndingDialog = PlannedWalkEndingDialog.newInstance("new instance", 0, 0, 0, 0, 0);
        String data = plannedWalkEndingDialog.getData();
        assertNotNull(data);
        assertEquals("Steps: 0\n" +
                "Time elapsed: 0' 0\"\n" +
                "Distance: 0.0 miles\n" +
                "Speed: 0.0 miles/hour", data);
    }

    @Test
    public void testFloatData() {
        plannedWalkEndingDialog = PlannedWalkEndingDialog.newInstance("new instance", 10.15f, 20.15f, steps, min, sec);
        String data = plannedWalkEndingDialog.getData();
        assertNotNull(data);
        assertEquals("Steps: 10\n" +
                "Time elapsed: 10' 10\"\n" +
                "Distance: 10.1 miles\n" +
                "Speed: 20.1 miles/hour", data);
    }

    private class TestFitnessService implements FitnessService {
        private static final String TAG = "[TestStepCountActFitnessService]: ";
        private Activity mainActivity;

        public TestFitnessService(Activity mainActivity) {
            this.mainActivity = mainActivity;
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
        public void addActiveSteps(int step, int min, int sec, float stride) {

        }

        @Override
        public String getUID() {
            return null;
        }

        @Override
        public String getEmail() {
            return null;
        }
    }

}
