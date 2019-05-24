package biz.netcentric.vlt.upgrade.handler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void testJoinNullStrings() {
        assertEquals("null,null", StringUtils.join(',', null, null));
    }

    @Test
    public void testJoinStringsWithSeparatorChar() {
        assertEquals("a,b", StringUtils.join(',', "a", "b"));
    }

    @Test
    public void testJoinStringsWithSeparatorSeq() {
        assertEquals("a..b", StringUtils.join("..", "a", "b"));
    }
}
