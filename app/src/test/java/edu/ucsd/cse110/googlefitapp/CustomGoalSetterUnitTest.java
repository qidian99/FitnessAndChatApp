package edu.ucsd.cse110.googlefitapp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class CustomGoalSetterUnitTest {
    @Test
    public void testNullNewInstance() {
        CustomGoalSetter customGoalSetter = CustomGoalSetter.newInstance(null);
        assertNull(customGoalSetter);
    }

    @Test
    public void testNotNullNewInstance() {
        CustomGoalSetter customGoalSetter = CustomGoalSetter.newInstance("Test");
        assertNotNull(customGoalSetter);
        assertNotNull(customGoalSetter.getArguments());
        assertEquals("Test", customGoalSetter.getArguments().getString("title"));
        assertTrue(customGoalSetter.isCancelable());
    }
}
