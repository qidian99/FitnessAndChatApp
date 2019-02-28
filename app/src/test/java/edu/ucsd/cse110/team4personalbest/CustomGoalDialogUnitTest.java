package edu.ucsd.cse110.team4personalbest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import edu.ucsd.cse110.team4personalbest.dialog.CustomGoalDialog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class CustomGoalDialogUnitTest {
    @Test
    public void testNullNewInstance() {
        CustomGoalDialog customGoalDialog = CustomGoalDialog.newInstance(null);
        assertNull(customGoalDialog);
    }

    @Test
    public void testNotNullNewInstance() {
        CustomGoalDialog customGoalDialog = CustomGoalDialog.newInstance("Test");
        assertNotNull(customGoalDialog);
        assertNotNull(customGoalDialog.getArguments());
        assertEquals("Test", customGoalDialog.getArguments().getString("title"));
        assertTrue(customGoalDialog.isCancelable());
    }
}
