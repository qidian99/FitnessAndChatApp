package edu.ucsd.cse110.team4personalbest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import edu.ucsd.cse110.team4personalbest.dialog.ManuallyEnterStepDialog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class ManuallyEnterStepDialogUnitTest {
    @Test
    public void testGetInstance() {
        ManuallyEnterStepDialog manuallyEnterStepDialog = ManuallyEnterStepDialog.newInstance(null);
        assertNull(manuallyEnterStepDialog);
    }

    @Test
    public void testNotNullNewInstance() {
        ManuallyEnterStepDialog manuallyEnterStepDialog = ManuallyEnterStepDialog.newInstance("Test");
        assertNotNull(manuallyEnterStepDialog);
        assertNotNull(manuallyEnterStepDialog.getArguments());
        assertEquals("Test", manuallyEnterStepDialog.getArguments().getString("title"));
        assertTrue(manuallyEnterStepDialog.isCancelable());
    }
}
