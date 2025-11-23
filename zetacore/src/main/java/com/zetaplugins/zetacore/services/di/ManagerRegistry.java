package com.zetaplugins.zetacore.services.di;

import com.zetaplugins.zetacore.annotations.InjectManager;
import com.zetaplugins.zetacore.annotations.InjectPlugin;
import com.zetaplugins.zetacore.annotations.PostManagerConstruct;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * A registry for managing and injecting manager instances.
 */
public class ManagerRegistry {
    private final JavaPlugin plugin;
    private final Map<Class<?>, Object> instances = new HashMap<>();

    /**
     * Creates a new ManagerRegistry for the given plugin.
     * @param plugin The main plugin instance.
     */
    public ManagerRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
        instances.put(plugin.getClass(), plugin);
        instances.put(JavaPlugin.class, plugin);
    }

    /**
     * Registers an existing instance in the registry.
     * @param instance The instance to register.
     */
    public void registerInstance(Object instance) {
        injectManagers(instance);
        instances.put(instance.getClass(), instance);
    }

    /**
     * Registers an existing instance in the registry under a specific class.
     * @param cls The class to register the instance under.
     * @param instance The instance to register.
     */
    public void registerInstance(Class<?> cls, Object instance) {
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
        Object existing = instances.get(cls);
        if (existing != null) return (T) existing;

        try {
            for (Constructor<?> constructor : cls.getDeclaredConstructors()) {
                Class<?>[] params = constructor.getParameterTypes();
                if (params.length == 1 && params[0].isAssignableFrom(plugin.getClass())) {
                    constructor.setAccessible(true);
                    T obj = (T) constructor.newInstance(plugin);
                    instances.put(cls, obj);
                    injectManagers(obj);
                    return obj;
                }
                if (params.length == 1 && params[0].isAssignableFrom(JavaPlugin.class)) {
                    constructor.setAccessible(true);
                    T obj = (T) constructor.newInstance(plugin);
                    instances.put(cls, obj);
                    injectManagers(obj);
                    return obj;
                }
            }
            Constructor<T> noArg = cls.getDeclaredConstructor();
            noArg.setAccessible(true);
            T obj = noArg.newInstance();
            instances.put(cls, obj);
            injectManagers(obj);
            return obj;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + cls.getName(), e);
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
        if (field.isAnnotationPresent(InjectManager.class)) {
            Object instance = getOrCreate(field.getType());
            try {
                field.setAccessible(true);
                field.set(target, instance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
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
}
