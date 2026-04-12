package com.zetaplugins.zetacore.di;

import com.zetaplugins.zetacore.di.annotation.Inject;
import com.zetaplugins.zetacore.di.annotation.Service;
import com.zetaplugins.zetacore.di.annotation.PostConstruct;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A registry for managing and injecting service instances.
 */
public class ServiceRegistry {
    private final JavaPlugin plugin;
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final boolean requireServiceAnnotation;
    private final String packagePrefix;
    private final ThreadLocal<Deque<Class<?>>> creationStack = ThreadLocal.withInitial(ArrayDeque::new);

    /**
     * Creates a new ServiceRegistry for the given plugin. Doesn't require the {@link Service} annotation on managed classes.
     * @param plugin The main plugin instance.
     */
    public ServiceRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
        instances.put(plugin.getClass(), plugin);
        instances.put(JavaPlugin.class, plugin);
        this.requireServiceAnnotation = false;
        this.packagePrefix = plugin.getClass().getPackageName();
    }

    /**
     * Creates a new ServiceRegistry for the given plugin.
     * @param plugin The main plugin instance.
     * @param requireServiceAnnotation Whether to require the {@link Service} annotation on managed classes.
     */
    public ServiceRegistry(JavaPlugin plugin, boolean requireServiceAnnotation, String packagePrefix) {
        this.plugin = plugin;
        instances.put(plugin.getClass(), plugin);
        instances.put(JavaPlugin.class, plugin);
        this.requireServiceAnnotation = requireServiceAnnotation;
        this.packagePrefix = packagePrefix;
    }

    /**
     * Initializes and registers all eagerly loaded singleton services found in the specified package.
     */
    public void initializeEagerServices() {
        Reflections reflections = new Reflections(packagePrefix);
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(Service.class);

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

            Object existing = instances.get(cls);
            if (existing != null) return (T) existing;

            // If the requested type is an interface or abstract class, try to discover a concrete implementation
            Class<T> resolvedClass = cls;
            if (cls.isInterface() || Modifier.isAbstract(cls.getModifiers())) {
                resolvedClass = discoverImplementation(cls);
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
        Reflections reflections = new Reflections(packagePrefix);
        List<Class<?>> serviceCandidates = reflections.getSubTypesOf(type).stream()
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
     * Creates an instance of the specified class, using a constructor that accepts the plugin if available.
     * @param cls The class to create an instance of.
     * @return The newly created instance.
     * @param <T> The type of the instance.
     */
    private <T> T createInstance(Class<T> cls) {
        try {
            for (Constructor<?> constructor : cls.getDeclaredConstructors()) {
                Class<?>[] params = constructor.getParameterTypes();
                if (params.length == 1 && (params[0].isAssignableFrom(plugin.getClass()) || params[0].isAssignableFrom(JavaPlugin.class))) {
                    constructor.setAccessible(true);
                    Object obj = constructor.newInstance(plugin);
                    return cls.cast(obj);
                }
            }
            Constructor<T> noArg = cls.getDeclaredConstructor();
            noArg.setAccessible(true);
            return noArg.newInstance();
        } catch (NoSuchMethodException e) {
            throw new ServiceException("Failed to create instance of " + cls.getName()
                    + ". The class must have either a no-argument constructor or a constructor that accepts the plugin instance.", e);
        } catch (Exception e) {
            throw new ServiceException("Failed to create instance of " + cls.getName(), e);
        }
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
            if (JavaPlugin.class.isAssignableFrom(field.getType())) {
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

        public ServiceRegistry build() {
            if (plugin == null) throw new IllegalStateException("Plugin must be set before building ServiceRegistry.");
            if (packagePrefix == null) throw new IllegalStateException("Package prefix must be set before building ServiceRegistry.");
            return new ServiceRegistry(plugin, requireServiceAnnotation, packagePrefix);
        }
    }
}
