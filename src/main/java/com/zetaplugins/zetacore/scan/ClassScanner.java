package com.zetaplugins.zetacore.scan;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Scans a package for classes using a specific class loader.
 * <p>
 * The classpath is scanned once, on first use, and the result is reused for every query.
 * Share one instance between the {@link com.zetaplugins.zetacore.di.ServiceRegistry} and the registrars
 * instead of letting each of them scan on its own.
 * <p>
 * If ZetaCore is shaded and relocated into the scanned package, its own classes are excluded.
 */
public class ClassScanner {
    private static final String ZETACORE_PACKAGE = ClassScanner.class.getPackageName()
            .substring(0, ClassScanner.class.getPackageName().lastIndexOf('.'));

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

    /**
     * Finds all classes that declare at least one method annotated with the given annotation.
     * @param annotation The method annotation to look for.
     * @return The declaring classes.
     */
    public Set<Class<?>> getTypesWithMethodsAnnotatedWith(Class<? extends Annotation> annotation) {
        Set<Class<?>> types = new LinkedHashSet<>();
        for (Method method : reflections().getMethodsAnnotatedWith(annotation)) {
            types.add(method.getDeclaringClass());
        }
        return types;
    }

    private Reflections reflections() {
        Reflections result = reflections;
        if (result == null) {
            synchronized (this) {
                result = reflections;
                if (result == null) {
                    FilterBuilder filter = new FilterBuilder().includePackage(packagePrefix);
                    if (ZETACORE_PACKAGE.startsWith(packagePrefix + ".")) filter.excludePackage(ZETACORE_PACKAGE);
                    result = new Reflections(new ConfigurationBuilder()
                            .forPackage(packagePrefix, classLoader)
                            .addClassLoaders(classLoader)
                            .filterInputsBy(filter)
                            .setScanners(Scanners.TypesAnnotated, Scanners.SubTypes, Scanners.MethodsAnnotated));
                    reflections = result;
                }
            }
        }
        return result;
    }
}
