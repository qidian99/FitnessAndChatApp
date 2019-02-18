package edu.ucsd.cse110.googlefitapp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class NewGoalSetterUnitTest {
    @Test
    public void testNullNewInstance() {
        NewGoalSetter newGoalSetter = NewGoalSetter.newInstance(null, 0);
        assertNull(newGoalSetter);
    }

    @Test
    public void testNotNullNewInstance() {
        NewGoalSetter newGoalSetter = NewGoalSetter.newInstance("Test", 5000);
        assertNotNull(newGoalSetter);
        assertNotNull(newGoalSetter.getArguments());
        assertEquals("Test", newGoalSetter.getArguments().getString("title"));
        assertEquals(5000,newGoalSetter.getCurrentGoal());
        assertTrue(newGoalSetter.isCancelable());
    }
}
