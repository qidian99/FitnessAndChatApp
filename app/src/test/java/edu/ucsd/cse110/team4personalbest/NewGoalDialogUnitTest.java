package edu.ucsd.cse110.team4personalbest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import edu.ucsd.cse110.team4personalbest.dialog.NewGoalDialog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class NewGoalDialogUnitTest {
    @Test
    public void testNullNewInstance() {
        NewGoalDialog newGoalDialog = NewGoalDialog.newInstance(null, 0);
        assertNull(newGoalDialog);
    }

    @Test
    public void testNotNullNewInstance() {
        NewGoalDialog newGoalDialog = NewGoalDialog.newInstance("Test", 5000);
        assertNotNull(newGoalDialog);
        assertNotNull(newGoalDialog.getArguments());
        assertEquals("Test", newGoalDialog.getArguments().getString("title"));
        assertEquals(5000, newGoalDialog.getCurrentGoal());
        assertTrue(newGoalDialog.isCancelable());
    }
}
