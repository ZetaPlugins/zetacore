package com.zetaplugins.zetacore.scan;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Set;

/**
 * Scans a package for classes using a specific class loader.
 * <p>
 * The classpath is scanned once, on first use, and the result is reused for every query.
 * Share one instance between the {@link com.zetaplugins.zetacore.di.ServiceRegistry} and the registrars
 * instead of letting each of them scan on its own.
 */
public class ClassScanner {
    private final String packagePrefix;
    private final ClassLoader classLoader;
    private volatile Reflections reflections;

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
     * Finds all classes annotated with the given annotation, including subclasses of annotated classes.
     * @param annotation The annotation to look for.
     * @return The annotated classes.
     */
    public Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotation) {
        return reflections().getTypesAnnotatedWith(annotation);
    }

    /**
     * Finds all subtypes of the given type.
     * @param type The supertype.
     * @return The subtypes, not including the type itself.
     * @param <T> The supertype.
     */
    public <T> Set<Class<? extends T>> getSubTypesOf(Class<T> type) {
        return reflections().getSubTypesOf(type);
    }

    private Reflections reflections() {
        Reflections result = reflections;
        if (result == null) {
            synchronized (this) {
                result = reflections;
                if (result == null) {
                    result = new Reflections(new ConfigurationBuilder()
                            .forPackage(packagePrefix, classLoader)
                            .addClassLoaders(classLoader)
                            .filterInputsBy(new FilterBuilder().includePackage(packagePrefix))
                            .setScanners(Scanners.TypesAnnotated, Scanners.SubTypes));
                    reflections = result;
                }
            }
        }
        return result;
    }
}
