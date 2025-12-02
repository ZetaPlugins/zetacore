package com.zetaplugins.zetacore.services.di;

import com.zetaplugins.zetacore.annotations.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A registry for managing and injecting manager instances.
 */
public class ManagerRegistry {
    private final JavaPlugin plugin;
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final boolean requireManagerAnnotation;
    private final String packagePrefix;

    /**
     * Creates a new ManagerRegistry for the given plugin. Doesn't require the {@link Manager} annotation on managed classes.
     * @param plugin The main plugin instance.
     */
    public ManagerRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
        instances.put(plugin.getClass(), plugin);
        instances.put(JavaPlugin.class, plugin);
        this.requireManagerAnnotation = false;
        this.packagePrefix = plugin.getClass().getPackageName();
    }

    /**
     * Creates a new ManagerRegistry for the given plugin.
     * @param plugin The main plugin instance.
     * @param requireManagerAnnotation Whether to require the {@link Manager} annotation on managed classes.
     */
    public ManagerRegistry(JavaPlugin plugin, boolean requireManagerAnnotation, String packagePrefix) {
        this.plugin = plugin;
        instances.put(plugin.getClass(), plugin);
        instances.put(JavaPlugin.class, plugin);
        this.requireManagerAnnotation = requireManagerAnnotation;
        this.packagePrefix = packagePrefix;
    }

    /**
     * Initializes and registers all eagerly loaded singleton managers found in the specified package.
     */
    public void initializeEagerManagers() {
        Reflections reflections = new Reflections(packagePrefix);
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(Manager.class);

        for (Class<?> cls : annotatedClasses) {
            ManagerOptions options = getManagerOptions(cls);
            if (options.eagerlyLoad() && options.scope() == ManagerScope.SINGLETON) {
                getOrCreate(cls);
            }
        }
    }

    /**
     * Registers an existing instance in the registry.
     * @param instance The instance to register.
     */
    public void registerInstance(Object instance) {
        requireManagerAnnotation(instance.getClass());
        injectManagers(instance);
        instances.put(instance.getClass(), instance);
    }

    /**
     * Registers an existing instance in the registry under a specific class.
     * @param cls The class to register the instance under.
     * @param instance The instance to register.
     */
    public void registerInstance(Class<?> cls, Object instance) {
        requireManagerAnnotation(cls);
        injectManagers(instance);
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
        requireManagerAnnotation(cls);

        if (cls.isAnnotationPresent(Manager.class)) {
            ManagerOptions options = getManagerOptions(cls);
            if (options.scope() == ManagerScope.PROTOTYPE) {
                try {
                    T obj = createInstance(cls);
                    injectManagers(obj);
                    return obj;
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create prototype instance of " + cls.getName(), e);
                }
            }
        }

        Object existing = instances.get(cls);
        if (existing != null) return (T) existing;

        try {
            T obj = createInstance(cls);
            instances.put(cls, obj);
            injectManagers(obj);
            return obj;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + cls.getName(), e);
        }
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
            T obj = noArg.newInstance();
            injectManagers(obj);
            return obj;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + cls.getName() + ". The class must have either a no-argument constructor or a constructor that accepts the plugin instance.", e);
        }
    }

    /**
     * Injects manager instances into the fields of the target object.
     * @param target The target object to inject managers into.
     */
    public void injectManagers(Object target) {
        Class<?> cls = target.getClass();
        while (cls != null && cls != Object.class) {
            for (Field field : cls.getDeclaredFields()) {
                injectManagerIntoField(field, target);
                injectPluginIntoField(field, target);
            }
            cls = cls.getSuperclass();
        }

        callPostConstructMethods(target);
    }

    private void injectManagerIntoField(Field field, Object target) {
        if (!field.isAnnotationPresent(InjectManager.class)) return;

        Object instance = getOrCreate(field.getType());
        try {
            field.setAccessible(true);
            field.set(target, instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void injectPluginIntoField(Field field, Object target) {
        if (field.isAnnotationPresent(InjectPlugin.class)) {
            if (!JavaPlugin.class.isAssignableFrom(field.getType())) {
                throw new RuntimeException("Field " + field.getName() + " is annotated with @InjectPlugin but is not of type JavaPlugin or a subclass.");
            }

            try {
                field.setAccessible(true);
                field.set(target, plugin);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void callPostConstructMethods(Object target) {
        Class<?> cls = target.getClass();
        while (cls != null && cls != Object.class) {
            for (var method : cls.getDeclaredMethods()) {
                if (method.isAnnotationPresent(PostManagerConstruct.class)) {
                    if (method.getParameterCount() != 0) {
                        throw new RuntimeException("@PostManagerConstruct method " + method.getName() + " must have no parameters");
                    }
                    try {
                        method.setAccessible(true);
                        method.invoke(target);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to execute @PostManagerConstruct method " + method.getName(), e);
                    }
                }
            }
            cls = cls.getSuperclass();
        }
    }

    private void requireManagerAnnotation(Class<?> cls) {
        if (requireManagerAnnotation && !cls.isAnnotationPresent(Manager.class)) {
            throw new RuntimeException("Class " + cls.getName() + " is not annotated with @Manager");
        }
    }

    record ManagerOptions(
            boolean eagerlyLoad,
            ManagerScope scope
    ) {}

    /**
     * Get the ManagerOptions for the given class.
     * @param cls The class to get the ManagerOptions for.
     * @return The ManagerOptions for the class.
     */
    private ManagerOptions getManagerOptions(Class<?> cls) {
        if (cls.isAnnotationPresent(Manager.class)) {
            Manager managerAnnotation = cls.getAnnotation(Manager.class);
            return new ManagerOptions(
                    managerAnnotation.eagerlyLoad(),
                    managerAnnotation.scope()
            );
        }
        return new ManagerOptions(false, ManagerScope.SINGLETON);
    }
}
