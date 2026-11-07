package com.zetaplugins.zetacore.di;

import com.zetaplugins.zetacore.di.annotation.Inject;
import com.zetaplugins.zetacore.di.annotation.Service;
import com.zetaplugins.zetacore.di.annotation.PostConstruct;
import com.zetaplugins.zetacore.scan.ClassScanner;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * A registry for managing and injecting service instances.
 */
public class ServiceRegistry {
    private final JavaPlugin plugin;
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final boolean requireServiceAnnotation;
    private final ClassScanner scanner;
    private final ThreadLocal<Deque<Class<?>>> creationStack = ThreadLocal.withInitial(ArrayDeque::new);

    /**
     * Creates a new ServiceRegistry for the given plugin. Doesn't require the {@link Service} annotation on managed classes.
     * @param plugin The main plugin instance.
     */
    public ServiceRegistry(JavaPlugin plugin) {
        this(plugin, false, plugin.getClass().getPackageName());
    }

    /**
     * Creates a new ServiceRegistry for the given plugin.
     * @param plugin The main plugin instance.
     * @param requireServiceAnnotation Whether to require the {@link Service} annotation on managed classes.
     * @param packagePrefix The package to scan for services.
     */
    public ServiceRegistry(JavaPlugin plugin, boolean requireServiceAnnotation, String packagePrefix) {
        this(plugin, requireServiceAnnotation, new ClassScanner(packagePrefix, plugin.getClass().getClassLoader()));
    }

    /**
     * Creates a new ServiceRegistry for the given plugin that uses an existing scanner.
     * @param plugin The main plugin instance.
     * @param requireServiceAnnotation Whether to require the {@link Service} annotation on managed classes.
     * @param scanner The scanner used to discover services.
     */
    public ServiceRegistry(JavaPlugin plugin, boolean requireServiceAnnotation, ClassScanner scanner) {
        this.plugin = plugin;
        this.requireServiceAnnotation = requireServiceAnnotation;
        this.scanner = scanner;
        instances.put(plugin.getClass(), plugin);
        instances.put(JavaPlugin.class, plugin);
        instances.put(ServiceRegistry.class, this);
        instances.put(ClassScanner.class, scanner);
    }

    /**
     * Gets the scanner this registry uses to discover services.
     * @return The class scanner.
     */
    public ClassScanner getClassScanner() {
        return scanner;
    }

    /**
     * Initializes and registers all eagerly loaded singleton services found in the specified package.
     */
    public void initializeEagerServices() {
        Set<Class<?>> annotatedClasses = scanner.getTypesAnnotatedWith(Service.class);

        for (Class<?> cls : annotatedClasses) {
            ServiceOptions options = getServiceOptions(cls);
            if (options.eagerlyLoad() && options.scope() == ServiceScope.SINGLETON) {
                getOrCreate(cls);
            }
        }
    }

    /**
     * Registers an existing instance in the registry.
     * @param instance The instance to register.
     */
    public void registerInstance(Object instance) {
        requireServiceAnnotation(instance.getClass());
        injectServices(instance);
        instances.put(instance.getClass(), instance);
        registerBindings(instance.getClass(), instance);
    }

    /**
     * Registers an existing instance in the registry under a specific class.
     * @param cls The class to register the instance under.
     * @param instance The instance to register.
     */
    public void registerInstance(Class<?> cls, Object instance) {
        requireServiceAnnotation(cls);
        injectServices(instance);
        instances.put(cls, instance);
    }

    /**
     * Gets an existing instance of the specified class, or creates one if it doesn't exist.
     * @param cls The class of the instance to get or create.
     * @return The existing or newly created instance.
     * @param <T> The type of the instance.
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrCreate(Class<T> cls) {
        Object registered = instances.get(cls);
        if (registered != null) return (T) registered;

        requireServiceAnnotation(cls);

        Deque<Class<?>> stack = creationStack.get();
        if (stack.contains(cls)) {
            throw new ServiceException("Circular dependency detected: " + stack + " -> " + cls.getName());
        }
        stack.push(cls);
        try {
            if (cls.isAnnotationPresent(Service.class)) {
                ServiceOptions options = getServiceOptions(cls);
                if (options.scope() == ServiceScope.PROTOTYPE) {
                    T obj = createInstance(cls);
                    injectServices(obj);
                    return obj;
                }
            }

            // If the requested type is an interface or abstract class, try to discover a concrete implementation
            Class<T> resolvedClass = cls;
            if (cls.isInterface() || Modifier.isAbstract(cls.getModifiers())) {
                resolvedClass = discoverImplementation(cls);
                Object impl = instances.get(resolvedClass);
                if (impl != null) {
                    instances.put(cls, impl);
                    return (T) impl;
                }
            }

            T obj = createInstance(resolvedClass);
            instances.put(cls, obj);
            if (resolvedClass != cls) {
                instances.put(resolvedClass, obj);
            }
            registerBindings(resolvedClass, obj);
            injectServices(obj);
            return obj;
        } finally {
            stack.pop();
        }
    }

    /**
     * Discovers a concrete {@link Service}-annotated implementation of the given interface or abstract class.
     * <p>
     * If exactly one implementation is found, it is returned. If none are found, a {@link ServiceException} is thrown.
     * If multiple are found, only one annotated with {@code @Service(binds = ...)} targeting this type is selected.
     * If that still doesn't resolve to a single candidate, a {@link ServiceException} is thrown.
     *
     * @param type The interface or abstract class to find an implementation for.
     * @return The resolved concrete class.
     * @param <T> The type of the interface or abstract class.
     */
    @SuppressWarnings("unchecked")
    private <T> Class<T> discoverImplementation(Class<T> type) {
        List<Class<?>> serviceCandidates = scanner.getSubTypesOf(type).stream()
                .<Class<?>>map(c -> c)
                .filter(c -> c.isAnnotationPresent(Service.class))
                .filter(c -> !c.isInterface() && !Modifier.isAbstract(c.getModifiers()))
                .toList();

        if (serviceCandidates.isEmpty()) {
            throw new ServiceException("No @Service implementation found for " + type.getName());
        }

        if (serviceCandidates.size() == 1) {
            return (Class<T>) serviceCandidates.get(0);
        }

        // Multiple candidates -> try to narrow down using explicit binds
        List<Class<?>> boundCandidates = serviceCandidates.stream()
                .filter(c -> {
                    Class<?>[] binds = c.getAnnotation(Service.class).binds();
                    for (Class<?> bind : binds) {
                        if (bind == type) return true;
                    }
                    return false;
                })
                .toList();

        if (boundCandidates.size() == 1) {
            return (Class<T>) boundCandidates.get(0);
        }

        String candidateNames = serviceCandidates.stream()
                .map(Class::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        throw new ServiceException("Multiple @Service implementations found for " + type.getName()
                + ": [" + candidateNames + "]. Use @Service(binds = " + type.getSimpleName()
                + ".class) on the intended implementation to resolve the ambiguity.");
    }

    /**
     * Creates a new, unregistered instance of the specified class with full dependency injection.
     * <p>
     * The instance is constructed via constructor injection (see {@link #createInstance(Class)}), then its
     * {@link Inject}-annotated fields are injected and its {@link PostConstruct} methods are called.
     * The created object itself is <b>not</b> stored in the registry, which makes this suitable for
     * objects like commands or listeners that depend on services but aren't services themselves.
     *
     * @param cls The class to instantiate.
     * @return The newly created and injected instance.
     * @param <T> The type of the instance.
     */
    public <T> T create(Class<T> cls) {
        Deque<Class<?>> stack = creationStack.get();
        if (stack.contains(cls)) {
            throw new ServiceException("Circular dependency detected: " + stack + " -> " + cls.getName());
        }
        stack.push(cls);
        try {
            T obj = createInstance(cls);
            injectServices(obj);
            return obj;
        } finally {
            stack.pop();
        }
    }

    /**
     * Creates an instance of the specified class using constructor injection.
     * <p>
     * The constructor is selected in the following order:
     * <ol>
     *     <li>The constructor annotated with {@link Inject} (at most one is allowed)</li>
     *     <li>A constructor whose only parameter accepts the plugin instance</li>
     *     <li>The no-argument constructor</li>
     *     <li>The only declared constructor, if the class has exactly one</li>
     * </ol>
     * Each constructor parameter is resolved like an {@link Inject} field: the plugin if the type accepts it,
     * otherwise a service from the registry.
     *
     * @param cls The class to create an instance of.
     * @return The newly created instance.
     * @param <T> The type of the instance.
     */
    private <T> T createInstance(Class<T> cls) {
        Constructor<?> constructor = selectConstructor(cls);
        Object[] args = resolveConstructorArguments(constructor);
        try {
            constructor.setAccessible(true);
            return cls.cast(constructor.newInstance(args));
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new ServiceException("Constructor of " + cls.getName() + " threw an exception", cause);
        } catch (Exception e) {
            throw new ServiceException("Failed to create instance of " + cls.getName(), e);
        }
    }

    /**
     * Selects the constructor to use for creating an instance of the given class.
     * @param cls The class to select a constructor for.
     * @return The selected constructor.
     */
    private Constructor<?> selectConstructor(Class<?> cls) {
        Constructor<?>[] constructors = cls.getDeclaredConstructors();

        List<Constructor<?>> annotated = Arrays.stream(constructors)
                .filter(c -> c.isAnnotationPresent(Inject.class))
                .toList();
        if (annotated.size() > 1) {
            throw new ServiceException("Class " + cls.getName() + " has multiple @Inject constructors. Only one is allowed.");
        }
        if (annotated.size() == 1) return annotated.get(0);

        for (Constructor<?> constructor : constructors) {
            Class<?>[] params = constructor.getParameterTypes();
            if (params.length == 1 && isPluginType(params[0])) return constructor;
        }

        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == 0) return constructor;
        }

        if (constructors.length == 1) return constructors[0];

        throw new ServiceException("Failed to create instance of " + cls.getName()
                + ". No suitable constructor found: annotate the constructor to use with @Inject, or provide a "
                + "no-argument constructor or a constructor that accepts the plugin instance.");
    }

    /**
     * Resolves the arguments for the given constructor from the registry.
     * @param constructor The constructor to resolve arguments for.
     * @return The resolved arguments.
     */
    private Object[] resolveConstructorArguments(Constructor<?> constructor) {
        Class<?>[] paramTypes = constructor.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> type = paramTypes[i];
            if (isPluginType(type)) {
                args[i] = plugin;
            } else if (type.isPrimitive()) {
                throw new ServiceException("Cannot inject primitive parameter of type " + type.getName()
                        + " into constructor of " + constructor.getDeclaringClass().getName());
            } else {
                args[i] = getOrCreate(type);
            }
        }
        return args;
    }

    /**
     * Checks whether the plugin instance can be supplied for the given type.
     * @param type The type to check.
     * @return True if the type is a plugin type that accepts the plugin instance.
     */
    private boolean isPluginType(Class<?> type) {
        return type != Object.class && type.isInstance(plugin);
    }

    /**
     * Injects service instances into the fields of the target object.
     * @param target The target object to inject services into.
     */
    public void injectServices(Object target) {
        Class<?> cls = target.getClass();
        while (cls != null && cls != Object.class) {
            for (Field field : cls.getDeclaredFields()) {
                injectField(field, target);
            }
            cls = cls.getSuperclass();
        }

        callPostConstructMethods(target);
    }

    /**
     * Injects a service instance into a single field if it is annotated with @Inject. Supports injection of the plugin instance and other registered services.
     * @param field The field to inject into.
     * @param target The target object containing the field.
     */
    private void injectField(Field field, Object target) {
        if (!field.isAnnotationPresent(Inject.class)) return;

        try {
            field.setAccessible(true);
            if (isPluginType(field.getType())) {
                field.set(target, plugin);
            } else {
                field.set(target, getOrCreate(field.getType()));
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to inject field '" + field.getName()
                    + "' of type " + field.getType().getName()
                    + " on " + target.getClass().getName(), e);
        }
    }

    private void callPostConstructMethods(Object target) {
        Class<?> cls = target.getClass();
        while (cls != null && cls != Object.class) {
            for (var method : cls.getDeclaredMethods()) {
                if (method.isAnnotationPresent(PostConstruct.class)) {
                    if (method.getParameterCount() != 0) {
                        throw new ServiceException("@PostConstruct method " + cls.getName()
                                + "#" + method.getName() + " must have no parameters");
                    }
                    try {
                        method.setAccessible(true);
                        method.invoke(target);
                    } catch (Exception e) {
                        throw new ServiceException("Failed to execute @PostConstruct method "
                                + cls.getName() + "#" + method.getName(), e);
                    }
                }
            }
            cls = cls.getSuperclass();
        }
    }

    private void registerBindings(Class<?> cls, Object instance) {
        if (!cls.isAnnotationPresent(Service.class)) return;

        Class<?>[] binds = cls.getAnnotation(Service.class).binds();
        for (Class<?> bindType : binds) {
            if (!bindType.isAssignableFrom(cls)) {
                throw new ServiceException("Service " + cls.getName()
                        + " declares binding to " + bindType.getName()
                        + " but does not implement or extend it.");
            }
            instances.put(bindType, instance);
        }
    }

    private void requireServiceAnnotation(Class<?> cls) {
        if (!requireServiceAnnotation) return;
        if (cls.isAnnotationPresent(Service.class)) return;
        if (cls.isInterface() || Modifier.isAbstract(cls.getModifiers())) return;

        throw new ServiceException("Class " + cls.getName() + " is not annotated with @Service");
    }

    record ServiceOptions(
            boolean eagerlyLoad,
            ServiceScope scope
    ) {}

    /**
     * Get the ServiceOptions for the given class.
     * @param cls The class to get the ServiceOptions for.
     * @return The ServiceOptions for the class.
     */
    private ServiceOptions getServiceOptions(Class<?> cls) {
        if (cls.isAnnotationPresent(Service.class)) {
            Service serviceAnnotation = cls.getAnnotation(Service.class);
            return new ServiceOptions(
                    serviceAnnotation.eagerlyLoad(),
                    serviceAnnotation.scope()
            );
        }
        return new ServiceOptions(false, ServiceScope.SINGLETON);
    }

    public static class Builder {
        private JavaPlugin plugin;
        private boolean requireServiceAnnotation = false;
        private String packagePrefix;
        private ClassScanner scanner;

        public Builder withPlugin(JavaPlugin plugin) {
            this.plugin = plugin;
            if (this.packagePrefix == null) this.packagePrefix = plugin.getClass().getPackageName();
            return this;
        }

        public Builder withRequireServiceAnnotation(boolean requireServiceAnnotation) {
            this.requireServiceAnnotation = requireServiceAnnotation;
            return this;
        }

        public Builder withPackagePrefix(String packagePrefix) {
            this.packagePrefix = packagePrefix;
            return this;
        }

        /**
         * Uses an existing scanner instead of creating one from the package prefix.
         * @param scanner The scanner to use.
         * @return This builder.
         */
        public Builder withClassScanner(ClassScanner scanner) {
            this.scanner = scanner;
            return this;
        }

        public ServiceRegistry build() {
            if (plugin == null) throw new IllegalStateException("Plugin must be set before building ServiceRegistry.");
            if (scanner != null) return new ServiceRegistry(plugin, requireServiceAnnotation, scanner);
            if (packagePrefix == null) throw new IllegalStateException("Package prefix must be set before building ServiceRegistry.");
            return new ServiceRegistry(plugin, requireServiceAnnotation, packagePrefix);
        }
    }
}
