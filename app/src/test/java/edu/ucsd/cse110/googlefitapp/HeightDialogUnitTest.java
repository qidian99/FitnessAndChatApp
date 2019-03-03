package edu.ucsd.cse110.googlefitapp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import edu.ucsd.cse110.googlefitapp.dialog.HeightDialog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class HeightDialogUnitTest {
    @Test
    public void testNullNewInstance() {
        HeightDialog newHeightDialog = HeightDialog.newInstance(null);
        assertNull(newHeightDialog);
    }

    @Test
    public void testNotNullNewInstance() {
        HeightDialog newHeightDialog = HeightDialog.newInstance("Test");
        assertNotNull(newHeightDialog);
        assertNotNull(newHeightDialog.getArguments());
        assertEquals(newHeightDialog.getArguments().getString("title"), "Test");
        assertTrue(newHeightDialog.isCancelable());
    }
}