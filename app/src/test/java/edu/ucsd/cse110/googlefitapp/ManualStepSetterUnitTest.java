package edu.ucsd.cse110.googlefitapp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class ManualStepSetterUnitTest {
    @Test
    public void testGetInstance() {
        ManualStepSetter manualStepSetter = ManualStepSetter.newInstance(null);
        assertNull(manualStepSetter);
    }

    @Test
    public void testNotNullNewInstance() {
        ManualStepSetter manualStepSetter = ManualStepSetter.newInstance("Test");
        assertNotNull(manualStepSetter);
        assertNotNull(manualStepSetter.getArguments());
        assertEquals("Test", manualStepSetter.getArguments().getString("title"));
        assertTrue(manualStepSetter.isCancelable());
    }
}
