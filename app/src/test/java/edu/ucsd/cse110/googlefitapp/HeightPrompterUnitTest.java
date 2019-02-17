package edu.ucsd.cse110.googlefitapp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
public class HeightPrompterUnitTest {
    private HeightPrompter heightPrompter;

    @Before
    public void setUp() throws Exception {
        heightPrompter = HeightPrompter.newInstance("Please Enter Your Height");
    }

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
    }

    @Test
    public void testNullParamOnCreateView() {
        try {
            heightPrompter.onCreateView(null, null, null);
            fail();
        } catch (Exception ignored) {}
    }
}