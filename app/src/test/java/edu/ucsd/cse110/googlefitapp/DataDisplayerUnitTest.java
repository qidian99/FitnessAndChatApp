package edu.ucsd.cse110.googlefitapp;
import android.os.Bundle;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class DataDisplayerUnitTest {
    private DataDisplayer dataDisplayer;
    private TextView display;
    private MainActivity mainActivity;
    private float distance = 10;
    private float speed = 10;
    private int steps = 10;
    private int min = 10;
    private int sec = 10;

    @Before
    public void setUp() throws Exception {
        dataDisplayer = DataDisplayer.newInstance("new instance", distance, speed, steps, min, sec);
    }

    @Test
    public void testNullTitle() {
        dataDisplayer = DataDisplayer.newInstance(null, distance, speed, steps, min, sec);
        Bundle bundle = dataDisplayer.getArguments();
        assertNotNull(bundle);
        String title = bundle.getString("title");
        assertNull(title);
    }

    @Test
    public void testEmptyObject() {
        dataDisplayer = new DataDisplayer();
        assertTrue(0 == dataDisplayer.getDistance());
        assertTrue(0 == dataDisplayer.getSpeed());
    }

    @Test
    public void testNewInstance() {
        assertNotNull(dataDisplayer);
        Bundle bundle = dataDisplayer.getArguments();
        assertNotNull(bundle);
        String title = bundle.getString("title");
        assertEquals("new instance", title);
    }

    @Test
    public void testIntData() {
        String data = dataDisplayer.getData();
        assertNotNull(data);
        assertEquals("Steps: 10\n" +
                "Time elapsed: 10' 10\"\n" +
                "Distance: 10.0 miles\n" +
                "Speed: 10.0 miles/hour", data);
    }

    @Test
    public void testFloatData() {
        dataDisplayer = DataDisplayer.newInstance("new instance", 10.15f, 20.15f, steps, min, sec);
        String data = dataDisplayer.getData();
        assertNotNull(data);
        assertEquals("Steps: 10\n" +
                "Time elapsed: 10' 10\"\n" +
                "Distance: 10.1 miles\n" +
                "Speed: 20.1 miles/hour", data);
    }
}
