package edu.ucsd.cse110.googlefitapp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
public class HeightPrompterUnitTest {
    @Test
    public void testNullNewInstance() {
        HeightPrompter newHeightPrompter = HeightPrompter.newInstance(null);
        assertNull(newHeightPrompter);
    }

    @Test
    public void testNotNullNewInstance() {
        HeightPrompter newHeightPrompter = HeightPrompter.newInstance("Test");
        assertNotNull(newHeightPrompter);
        assertNotNull(newHeightPrompter.getArguments());
        assertEquals(newHeightPrompter.getArguments().getString("title"), "Test");
        assertTrue(newHeightPrompter.isCancelable());
    }
}