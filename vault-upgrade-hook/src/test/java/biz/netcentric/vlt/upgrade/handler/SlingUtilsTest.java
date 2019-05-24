package biz.netcentric.vlt.upgrade.handler;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class SlingUtilsTest {

    private SlingUtils toTest = spy(new SlingUtils());

    @Before
    public void setup() {
        doReturn(ImmutableSet.of("runmode1", "runmode2")).when(toTest).getRunModes();
    }

    @Test
    public void testHasRunModes() {
        Assert.assertTrue(toTest.hasRunModes(Collections.<String>emptySet()));

        Assert.assertTrue(toTest.hasRunModes(ImmutableSet.of("runmode1", "runmode2")));
        Assert.assertTrue(toTest.hasRunModes(ImmutableSet.of("runmode1")));
        Assert.assertTrue(toTest.hasRunModes(ImmutableSet.of("runmode1", "runmode3")));
        Assert.assertTrue(toTest.hasRunModes(ImmutableSet.of("runmode1.runmode2")));

        Assert.assertFalse(toTest.hasRunModes(ImmutableSet.of("runmode3")));
        Assert.assertFalse(toTest.hasRunModes(ImmutableSet.of("runmode1.runmode3")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHasRunModesNull() {
        Set<String> nullSet = new HashSet<>();
        nullSet.add(null);
        toTest.hasRunModes(nullSet);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHasRunModesEmpty() {
        Set<String> nullSet = new HashSet<>();
        nullSet.add("");
        toTest.hasRunModes(nullSet);
    }
}
