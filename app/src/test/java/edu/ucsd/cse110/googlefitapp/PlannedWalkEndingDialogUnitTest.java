package edu.ucsd.cse110.googlefitapp;

import android.os.Bundle;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import edu.ucsd.cse110.googlefitapp.dialog.PlannedWalkEndingDialog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class PlannedWalkEndingDialogUnitTest {
    private PlannedWalkEndingDialog plannedWalkEndingDialog;
    private TextView display;
    private MainActivity mainActivity;
    private float distance = 10;
    private float speed = 10;
    private int steps = 10;
    private int min = 10;
    private int sec = 10;

    @Before
    public void setUp() throws Exception {
        plannedWalkEndingDialog = PlannedWalkEndingDialog.newInstance("new instance", distance, speed, steps, min, sec);
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
    public void testEmptyObject() {
        plannedWalkEndingDialog = new PlannedWalkEndingDialog();
        assertTrue(0 == plannedWalkEndingDialog.getDistance());
        assertTrue(0 == plannedWalkEndingDialog.getSpeed());
    }

    @Test
    public void testNewInstance() {
        assertNotNull(plannedWalkEndingDialog);
        Bundle bundle = plannedWalkEndingDialog.getArguments();
        assertNotNull(bundle);
        String title = bundle.getString("title");
        assertEquals("new instance", title);
    }

    @Test
    public void testIntData() {
        String data = plannedWalkEndingDialog.getData();
        assertNotNull(data);
        assertEquals("Steps: 10\n" +
                "Time elapsed: 10' 10\"\n" +
                "Distance: 10.0 miles\n" +
                "Speed: 10.0 miles/hour", data);
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
}
