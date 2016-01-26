package co.phoenixlab.dn.dngearsim.utils.version;


import org.junit.*;

import static co.phoenixlab.dn.dngearsim.utils.version.Versions.*;
import static org.junit.Assert.*;

public class VersionsTest {

    @Test
    public void testParseVersion() throws Exception {
        assertEquals(0, parseVersion(VERSION_ZERO));
        assertEquals(0x00000009, parseVersion("0.0.0.9"));
        assertEquals(0xFF00FF00, parseVersion("255.0.255.0"));
        assertEquals(0xFFFFFFFF, parseVersion("255.255.255.255"));
    }


    @Test(expected = NumberFormatException.class)
    public void testParseVersionFailNull() throws Exception {
        parseVersion(null);
    }

    @Test(expected = NumberFormatException.class)
    public void testParseVersionFailEmpty() throws Exception {
        parseVersion("");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseVersionFailBadFormat() throws Exception {
        parseVersion("potato");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseVersionFailRange() throws Exception {
        parseVersion("256.0.0.0");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseVersionFailDigits() throws Exception {
        parseVersion("2256.0.0.0");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseVersionFailBadDigits() throws Exception {
        parseVersion("1.4A.0.0");
    }

    @Test
    public void testToString() throws Exception {
        assertEquals("1.2.3.4", Versions.toString(0x01020304));
    }

    @Test
    public void testCompare() throws Exception {
        assertTrue(compare(0xFFFFFFFE, 0xFFFFFFFF) < 0);
        assertTrue(compare(0xFFFFFFFF, 0xFFFFFFFF) == 0);
        assertTrue(compare(0xFFFFFFFF, 0xFFFFFFFE) > 0);
        assertTrue(compare(0, 1) < 0);
        assertTrue(compare(1, 1) == 0);
        assertTrue(compare(1, 0) > 0);
    }
}
