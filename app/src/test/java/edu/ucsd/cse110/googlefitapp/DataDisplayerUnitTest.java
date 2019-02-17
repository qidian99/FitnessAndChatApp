package edu.ucsd.cse110.googlefitapp;
import android.os.Bundle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class DataDisplayerUnitTest {
    DataDisplayer dataDisplayer;
    float distance;
    float speed;
    int steps;
    int min;
    int sec;

    @Before
    public void setUp() throws Exception {
        dataDisplayer = new DataDisplayer(distance, speed, steps, min, sec);
    }

    @Test
    public void testNewInstance() {
        DataDisplayer tmpDisplayer = DataDisplayer.newInstance("new instance", distance, speed, steps, min, sec);
        assertNotNull(tmpDisplayer);
        Bundle bundle = tmpDisplayer.getArguments();
        assertNotNull(bundle);
        String title = bundle.getString("title");
        assertEquals("new instance", title);
    }

    @Test
    public void testOnCreate() {

    }
}
