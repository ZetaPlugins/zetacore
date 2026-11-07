package com.zetaplugins.zetacore.scan;

import com.zetaplugins.zetacore.scan.fixtures.Circle;
import com.zetaplugins.zetacore.scan.fixtures.Marker;
import com.zetaplugins.zetacore.scan.fixtures.Shape;
import com.zetaplugins.zetacore.scan.fixtures.sub.Square;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ClassScannerTest {
    private final ClassLoader loader = getClass().getClassLoader();

    @Test
    void findsAnnotatedTypesInPackageAndSubPackages() {
        ClassScanner scanner = new ClassScanner("com.zetaplugins.zetacore.scan.fixtures", loader);
        assertEquals(Set.of(Circle.class, Square.class), scanner.getTypesAnnotatedWith(Marker.class));
    }

    @Test
    void findsSubTypes() {
        ClassScanner scanner = new ClassScanner("com.zetaplugins.zetacore.scan.fixtures", loader);
        assertEquals(Set.of(Circle.class, Square.class), scanner.getSubTypesOf(Shape.class));
    }

    @Test
    void ignoresClassesOutsidePackage() {
        ClassScanner scanner = new ClassScanner("com.zetaplugins.zetacore.scan.fixtures.sub", loader);
        assertEquals(Set.of(Square.class), scanner.getTypesAnnotatedWith(Marker.class));
    }

    @Test
    void returnsEmptyForUnknownPackage() {
        ClassScanner scanner = new ClassScanner("com.zetaplugins.doesnotexist", loader);
        assertTrue(scanner.getTypesAnnotatedWith(Marker.class).isEmpty());
    }
}
