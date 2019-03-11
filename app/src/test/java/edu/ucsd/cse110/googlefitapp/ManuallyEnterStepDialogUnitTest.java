package edu.ucsd.cse110.googlefitapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import edu.ucsd.cse110.googlefitapp.dialog.ManuallyEnterStepDialog;
import edu.ucsd.cse110.googlefitapp.dialog.PlannedWalkEndingDialog;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessService;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessServiceFactory;
import edu.ucsd.cse110.googlefitapp.fitness.GoogleFitnessServiceFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class ManuallyEnterStepDialogUnitTest {
    private static final String TEST_SERVICE = "TEST_SET_STEP_DIALOG";
    private Button setStepBtn;
    private ManuallyEnterStepDialog manuallyEnterStepDialog;
    private MainActivity activity;
    private EditText stepText;
    private TextView stepMain;
    private Button posBtn;
    private DialogFragment dialogFragment;

    @Before
    public void setUp() throws Exception {
        manuallyEnterStepDialog = ManuallyEnterStepDialog.newInstance("manually enter step dialog");
//        stepText = manuallyEnterStepDialog.getView().findViewById(R.id.num_steps);
//       stepMain = manuallyEnterStepDialog.getView().findViewById(R.id.textStepsMain);

//        FitnessServiceFactory googleFitnessServiceFactory = new GoogleFitnessServiceFactory();
//
//        googleFitnessServiceFactory.put(TEST_SERVICE, new FitnessServiceFactory.BluePrint() {
//            @Override
//            public FitnessService create(Activity mainActivity) {
//                return new TestFitnessService(mainActivity);
//            }
//        });
//
//        setStepBtn = activity.findViewById(R.id.btnSetStep);
//        setStepBtn.performClick();
//        dialogFragment = (DialogFragment) activity.getSupportFragmentManager()
//                .findFragmentByTag("fragment_set_step");
//
//        Intent intent = new Intent(RuntimeEnvironment.application, MainActivity.class);
//        intent.putExtra("TEST", true);
//        intent.putExtra("TEST_SERVICE_MAIN", TEST_SERVICE);
//        intent.putExtra("TEST_SERVICE_STEP_COUNT", TEST_SERVICE);
//        activity = Robolectric.buildActivity(MainActivity.class, intent).create().get();
    }

    @Test
    public void testNullTitle() {
        manuallyEnterStepDialog = ManuallyEnterStepDialog.newInstance(null);
        assertNull(manuallyEnterStepDialog);
    }

    @Test
    public void testTitle() {
        Bundle bundle = manuallyEnterStepDialog.getArguments();
        assertNotNull(bundle);
        String title = bundle.getString("title");
        assertNotNull(title);
        assertEquals("manually enter step dialog", title);
    }

    @Test
    public void testSetStepDialogIsShown() {
        assertNotNull(manuallyEnterStepDialog);
        assertTrue(manuallyEnterStepDialog.getShowsDialog());
    }

    @Test
    public void testSetStepDialogCancelable() {
        assertTrue(manuallyEnterStepDialog.isCancelable());
    }


//    @Test
//    public void testButton() {
//        manuallyEnterStepDialog.getButton().performClick();
//        assertTrue(manuallyEnterStepDialog.finishEnterStep());
//    }

//    @Test
//    public void testSetStep() {
//        assertNotNull(        manuallyEnterStepDialog.getThisView());
//        stepText.setText(1000);
//        posBtn.performClick();
//        String steps = stepMain.getText().toString();
//        assertEquals(1000, steps);
//    }

//    @Test
//    public void testSetStepDialogBindToMain() {
//        assertNotNull(manuallyEnterStepDialog.getActivity());
//        assertEquals(activity, manuallyEnterStepDialog.getContext());
//    }
//
//    @Test
//    public void testSetStepDialogTag() {
//        assertEquals("fragment_set_step", manuallyEnterStepDialog.getTag());
//    }

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
        public void updateStepCount() {}

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
