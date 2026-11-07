package com.zetaplugins.zetacore.scan;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Scans a package for classes using a specific class loader.
 * <p>
 * Class files are read without loading them; only classes matching a query are loaded (without being initialized).
 * The package is scanned once, on first use, and the result is reused for every query.
 * Share one instance between the {@link com.zetaplugins.zetacore.di.ServiceRegistry} and the registrars
 * instead of letting each of them scan on its own.
 * <p>
 * If ZetaCore is shaded and relocated into the scanned package, its own classes are excluded.
 * Classes that cannot be loaded (for example because they depend on a plugin that is not installed) are skipped.
 */
public class ClassScanner {
    private static final Logger LOGGER = Logger.getLogger(ClassScanner.class.getName());
    private static final String ZETACORE_PACKAGE = ClassScanner.class.getPackageName()
            .substring(0, ClassScanner.class.getPackageName().lastIndexOf('.'));
    private static final int PARSING_OPTIONS = ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES;

    private final String packagePrefix;
    private final ClassLoader classLoader;
    private volatile Index index;

    /**
     * Creates a scanner for the given package.
     * @param packagePrefix The package to scan, including all sub-packages.
     * @param classLoader The class loader that can see the classes in the package (usually the plugin's class loader).
     */
    public ClassScanner(String packagePrefix, ClassLoader classLoader) {
        this.packagePrefix = Objects.requireNonNull(packagePrefix, "packagePrefix");
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader");
    }

    /**
     * Gets the scanned package.
     * @return The package prefix.
     */
    public String getPackagePrefix() {
        return packagePrefix;
    }

    /**
     * Gets the class loader used for scanning and loading classes.
     * @return The class loader.
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Finds all classes annotated with the given annotation, including subclasses of annotated classes
     * and classes annotated with an annotation that is itself annotated with it.
     * @param annotation The annotation to look for.
     * @return The annotated classes, sorted by name.
     */
    public Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotation) {
        Index idx = index();
        Set<String> annotated = closure(Set.of(annotation.getName()), idx.annotatedTypes);
        annotated.remove(annotation.getName());
        Set<String> result = new TreeSet<>(annotated);
        result.addAll(closure(annotated, idx.subTypes));
        result.retainAll(idx.classNames);
        return load(result, Object.class);
    }

    /**
     * Finds all subtypes of the given type.
     * @param type The supertype.
     * @return The subtypes, not including the type itself, sorted by name.
     * @param <T> The supertype.
     */
    public <T> Set<Class<? extends T>> getSubTypesOf(Class<T> type) {
        Set<String> subTypes = closure(Set.of(type.getName()), index().subTypes);
        subTypes.remove(type.getName());
        return load(new TreeSet<>(subTypes), type);
    }

    /**
     * Finds all classes that declare at least one method annotated with the given annotation.
     * @param annotation The method annotation to look for.
     * @return The declaring classes, sorted by name.
     */
    public Set<Class<?>> getTypesWithMethodsAnnotatedWith(Class<? extends Annotation> annotation) {
        Set<String> types = index().typesWithAnnotatedMethods.getOrDefault(annotation.getName(), Set.of());
        return load(new TreeSet<>(types), Object.class);
    }

    private static Set<String> closure(Set<String> start, Map<String, Set<String>> edges) {
        Set<String> result = new HashSet<>(start);
        Deque<String> queue = new ArrayDeque<>(start);
        while (!queue.isEmpty()) {
            for (String next : edges.getOrDefault(queue.poll(), Set.of())) {
                if (result.add(next)) queue.add(next);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T> Set<Class<? extends T>> load(Set<String> classNames, Class<T> type) {
        Set<Class<? extends T>> classes = new LinkedHashSet<>();
        for (String name : classNames) {
            try {
                Class<?> cls = Class.forName(name, false, classLoader);
                if (type.isAssignableFrom(cls)) classes.add((Class<? extends T>) cls);
            } catch (ClassNotFoundException | LinkageError e) {
                LOGGER.log(Level.FINE, "Skipping class " + name + " because it cannot be loaded", e);
            }
        }
        return classes;
    }

    private Index index() {
        Index result = index;
        if (result == null) {
            synchronized (this) {
                result = index;
                if (result == null) {
                    result = buildIndex();
                    index = result;
                }
            }
        }
        return result;
    }

    private Index buildIndex() {
        Index idx = new Index();
        String packagePath = packagePrefix.replace('.', '/') + "/";
        try {
            for (Root root : findRoots(packagePath)) {
                if (root.isJar()) scanJar(root.path(), packagePath, idx);
                else scanDirectory(root.path(), packagePath, idx);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to scan package " + packagePrefix, e);
        }
        return idx;
    }

    private record Root(Path path, boolean isJar) {}

    private Set<Root> findRoots(String packagePath) throws IOException {
        Set<Root> roots = new LinkedHashSet<>();
        if (classLoader instanceof URLClassLoader urlClassLoader) {
            for (URL url : urlClassLoader.getURLs()) {
                Path path = toPath(url);
                if (path == null) continue;
                if (Files.isDirectory(path)) roots.add(new Root(path, false));
                else if (Files.isRegularFile(path)) roots.add(new Root(path, true));
            }
        }

        Enumeration<URL> resources = classLoader.getResources(packagePath.substring(0, packagePath.length() - 1));
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            if ("jar".equals(url.getProtocol())) {
                URLConnection connection = url.openConnection();
                if (connection instanceof JarURLConnection jarConnection) {
                    Path path = toPath(jarConnection.getJarFileURL());
                    if (path != null) roots.add(new Root(path, true));
                }
            } else {
                Path packageDir = toPath(url);
                if (packageDir == null) continue;
                Path root = packageDir;
                for (int i = 0; i < packagePrefix.split("\\.").length; i++) root = root.getParent();
                roots.add(new Root(root, false));
            }
        }
        return roots;
    }

    private static Path toPath(URL url) {
        if (!"file".equals(url.getProtocol())) return null;
        try {
            return Path.of(url.toURI());
        } catch (URISyntaxException | IllegalArgumentException e) {
            return null;
        }
    }

    private void scanJar(Path jar, String packagePath, Index idx) throws IOException {
        try (JarFile jarFile = new JarFile(jar.toFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory() || !isCandidate(entry.getName(), packagePath)) continue;
                try (InputStream in = jarFile.getInputStream(entry)) {
                    parse(in, idx);
                }
            }
        }
    }

    private void scanDirectory(Path root, String packagePath, Index idx) throws IOException {
        Path packageDir = root.resolve(packagePath);
        if (!Files.isDirectory(packageDir)) return;
        try (Stream<Path> files = Files.walk(packageDir)) {
            for (Path file : (Iterable<Path>) files::iterator) {
                String relative = root.relativize(file).toString().replace('\\', '/');
                if (!Files.isRegularFile(file) || !isCandidate(relative, packagePath)) continue;
                try (InputStream in = Files.newInputStream(file)) {
                    parse(in, idx);
                }
            }
        }
    }

    private boolean isCandidate(String entryName, String packagePath) {
        if (!entryName.startsWith(packagePath) || !entryName.endsWith(".class")) return false;
        if (entryName.endsWith("module-info.class") || entryName.endsWith("package-info.class")) return false;
        String className = entryName.substring(0, entryName.length() - ".class".length()).replace('/', '.');
        return !ZETACORE_PACKAGE.startsWith(packagePrefix + ".") || !className.startsWith(ZETACORE_PACKAGE + ".");
    }

    private static void parse(InputStream in, Index idx) throws IOException {
        ClassReader reader;
        try {
            reader = new ClassReader(in);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Skipping class file with unsupported format", e);
            return;
        }
        reader.accept(new IndexingVisitor(idx), PARSING_OPTIONS);
    }

    private static final class Index {
        final Set<String> classNames = new HashSet<>();
        final Map<String, Set<String>> annotatedTypes = new HashMap<>();
        final Map<String, Set<String>> subTypes = new HashMap<>();
        final Map<String, Set<String>> typesWithAnnotatedMethods = new HashMap<>();

        static void put(Map<String, Set<String>> map, String key, String value) {
            map.computeIfAbsent(key, k -> new HashSet<>()).add(value);
        }
    }

    private static final class IndexingVisitor extends ClassVisitor {
        private final Index idx;
        private String className;

        IndexingVisitor(Index idx) {
            super(Opcodes.ASM9);
            this.idx = idx;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            className = Type.getObjectType(name).getClassName();
            idx.classNames.add(className);
            if (superName != null) Index.put(idx.subTypes, Type.getObjectType(superName).getClassName(), className);
            if (interfaces != null) {
                for (String itf : interfaces) Index.put(idx.subTypes, Type.getObjectType(itf).getClassName(), className);
            }
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            Index.put(idx.annotatedTypes, Type.getType(descriptor).getClassName(), className);
            return null;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            return new MethodVisitor(Opcodes.ASM9) {
                @Override
                public AnnotationVisitor visitAnnotation(String annotationDescriptor, boolean visible) {
                    Index.put(idx.typesWithAnnotatedMethods, Type.getType(annotationDescriptor).getClassName(), className);
                    return null;
                }
            };
        }
    }
}
