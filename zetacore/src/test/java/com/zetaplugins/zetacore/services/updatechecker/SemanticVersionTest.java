package com.zetaplugins.zetacore.services.updatechecker;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SemanticVersionTest {

    @Test
    void parseSimpleVersion() {
        SemanticVersion v = new SemanticVersion("1.2.3");
        assertEquals(1, v.getMajor());
        assertEquals(2, v.getMinor());
        assertEquals(3, v.getPatch());
        assertEquals("", v.getLabel());
        assertEquals("1.2.3", v.toString());
    }

    @Test
    void parseWithLabel() {
        SemanticVersion v = new SemanticVersion("1.2.3-beta.1");
        assertEquals(1, v.getMajor());
        assertEquals(2, v.getMinor());
        assertEquals(3, v.getPatch());
        assertEquals("beta.1", v.getLabel());
        assertEquals("1.2.3-beta.1", v.toString());
    }

    @Test
    void parseWithVPrefixAndWhitespace() {
        SemanticVersion v = new SemanticVersion("  v2.0.10-alpha ");
        assertEquals(2, v.getMajor());
        assertEquals(0, v.getMinor());
        assertEquals(10, v.getPatch());
        assertEquals("alpha", v.getLabel());
        assertEquals("2.0.10-alpha", v.toString());
    }

    @Test
    void compareVersions() {
        SemanticVersion a = new SemanticVersion("1.0.0");
        SemanticVersion b = new SemanticVersion("1.0.1");
        SemanticVersion c = new SemanticVersion("1.1.0");
        SemanticVersion d = new SemanticVersion("2.0.0");
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(c) < 0);
        assertTrue(c.compareTo(d) < 0);
        assertTrue(d.compareTo(a) > 0);
    }

    @Test
    void compareEqualVersions() {
        SemanticVersion a = new SemanticVersion("1.2.3");
        SemanticVersion b = new SemanticVersion("1.2.3");
        assertEquals(0, a.compareTo(b));
        assertEquals(a, b);
    }

    @Test
    void compareUsingGreaterThan() {
        SemanticVersion a = new SemanticVersion("1.2.3");
        SemanticVersion b = new SemanticVersion("1.2.2");
        assertTrue(a.isGreaterThan(b));
    }

    @Test
    void compareUsingLessThan() {
        SemanticVersion a = new SemanticVersion("2.0.0");
        SemanticVersion b = new SemanticVersion("2.1.0");
        assertTrue(a.isLessThan(b));
    }

    @Test
    void invalidNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> new SemanticVersion(null));
    }

    @Test
    void invalidEmptyThrows() {
        assertThrows(IllegalArgumentException.class, () -> new SemanticVersion("   "));
    }

    @Test
    void invalidTooFewPartsThrows() {
        assertThrows(IllegalArgumentException.class, () -> new SemanticVersion("1.2"));
        assertThrows(IllegalArgumentException.class, () -> new SemanticVersion("1"));
    }

    @Test
    void invalidNonNumericThrows() {
        assertThrows(IllegalArgumentException.class, () -> new SemanticVersion("a.b.c"));
        assertThrows(IllegalArgumentException.class, () -> new SemanticVersion("1.two.3"));
    }

    @Test
    void invalidNegativeNumberThrows() {
        assertThrows(IllegalArgumentException.class, () -> new SemanticVersion("-1.0.0"));
        assertThrows(IllegalArgumentException.class, () -> new SemanticVersion("1.-2.0"));
    }

    @Test
    void vOnlyPrefixThrows() {
        assertThrows(IllegalArgumentException.class, () -> new SemanticVersion("v"));
        assertThrows(IllegalArgumentException.class, () -> new SemanticVersion("V   "));
    }
}