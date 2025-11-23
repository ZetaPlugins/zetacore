package com.zetaplugins.zetacore.services.di;

import com.zetaplugins.zetacore.annotations.InjectManager;
import com.zetaplugins.zetacore.annotations.InjectPlugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ManagerRegistry {
    private final JavaPlugin plugin;
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public ManagerRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
        instances.put(plugin.getClass(), plugin);
        instances.put(JavaPlugin.class, plugin);
    }

    public void registerInstance(Object instance) {
        injectManagers(instance);
        instances.put(instance.getClass(), instance);
    }

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

    public void injectManagers(Object target) {
        Class<?> cls = target.getClass();
        while (cls != null && cls != Object.class) {
            for (Field field : cls.getDeclaredFields()) {
                injectManagerIntoField(field, target);
                injectPluginIntoField(field, target);
            }
            cls = cls.getSuperclass();
        }
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
}
