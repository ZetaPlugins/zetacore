package com.zetaplugins.zetacore.scan;

import com.zetaplugins.zetacore.scan.inheritance.Inheritance;
import com.zetaplugins.zetacore.scan.fixtures.Marker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ClassScannerJarTest {
    private static final String FIXTURES = "com/zetaplugins/zetacore/scan/fixtures/";

    @TempDir
    Path tempDir;

    private Path jarWith(List<String> entries) throws IOException {
        Path classes = Path.of(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        Path jar = tempDir.resolve("plugin.jar");
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar))) {
            for (String entry : entries) {
                out.putNextEntry(new JarEntry(entry));
                Files.copy(classes.resolve(entry), (OutputStream) out);
                out.closeEntry();
            }
        }
        return jar;
    }

    @SuppressWarnings("unchecked")
    private Set<String> annotatedNames(Path jar) throws Exception {
        try (URLClassLoader loader = new URLClassLoader(new URL[]{jar.toUri().toURL()}, ClassLoader.getPlatformClassLoader())) {
            ClassScanner scanner = new ClassScanner("com.zetaplugins.zetacore.scan.fixtures", loader);
            Class<? extends Annotation> marker = (Class<? extends Annotation>) Class.forName(Marker.class.getName(), false, loader);
            return scanner.getTypesAnnotatedWith(marker).stream().map(Class::getName).collect(Collectors.toSet());
        }
    }

    @Test
    void scansJarWithoutDirectoryEntries() throws Exception {
        Path jar = jarWith(List.of(FIXTURES + "Marker.class", FIXTURES + "Shape.class",
                FIXTURES + "Circle.class", FIXTURES + "sub/Square.class"));

        assertEquals(Set.of("com.zetaplugins.zetacore.scan.fixtures.Circle",
                "com.zetaplugins.zetacore.scan.fixtures.sub.Square"), annotatedNames(jar));
    }

    @Test
    void skipsClassesThatCannotBeLoaded() throws Exception {
        Path jar = jarWith(List.of(FIXTURES + "Marker.class", FIXTURES + "Circle.class", FIXTURES + "sub/Square.class"));

        assertTrue(annotatedNames(jar).isEmpty());
    }

    @Test
    void includesSubclassesAndMetaAnnotatedTypes() {
        ClassScanner scanner = new ClassScanner("com.zetaplugins.zetacore.scan.inheritance", getClass().getClassLoader());

        assertEquals(Set.of(Inheritance.Base.class, Inheritance.Child.class, Inheritance.GrandChild.class,
                Inheritance.MetaMarked.class, Inheritance.ViaMeta.class), scanner.getTypesAnnotatedWith(Marker.class));
    }

    @Test
    void findsTransitiveSubtypes() {
        ClassScanner scanner = new ClassScanner("com.zetaplugins.zetacore.scan.inheritance", getClass().getClassLoader());

        assertEquals(Set.of(Inheritance.Child.class, Inheritance.GrandChild.class), scanner.getSubTypesOf(Inheritance.Base.class));
    }
}
