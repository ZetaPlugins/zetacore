package com.zetaplugins.zetacore.services.events;

import com.zetaplugins.zetacore.annotations.AutoRegisterListener;
import com.zetaplugins.zetacore.services.di.ManagerRegistry;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Manages the registration of event listeners for a plugin.
 * Use the {@link com.zetaplugins.zetacore.annotations.AutoRegisterListener} annotation to mark listener classes for automatic registration.
 */
public class AutoEventRegistrar implements EventRegistrar {
    private final JavaPlugin plugin;
    private final String packagePrefix;
    private final ManagerRegistry managerRegistry;

    /**
     * @param plugin The JavaPlugin instance.
     * @param packagePrefix The package prefix to scan for annotated classes.
     */
    public AutoEventRegistrar(JavaPlugin plugin, String packagePrefix) {
        this.plugin = plugin;
        this.packagePrefix = packagePrefix;
        this.managerRegistry = null;
    }

    /**
     * @param plugin The JavaPlugin instance.
     * @param packagePrefix The package prefix to scan for annotated classes.
     * @param managerRegistry The ManagerRegistry for dependency injection.
     */
    public AutoEventRegistrar(JavaPlugin plugin, String packagePrefix, ManagerRegistry managerRegistry) {
        this.plugin = plugin;
        this.packagePrefix = packagePrefix;
        this.managerRegistry = managerRegistry;
    }

    /**
     * Registers all listener classes annotated with {@link com.zetaplugins.zetacore.annotations.AutoRegisterListener}.
     * @return A list of names of the registered listeners.
     */
    @Override
    public List<String> registerAllListeners() {
        Reflections reflections = new Reflections(packagePrefix);
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(AutoRegisterListener.class);
        List<String> registeredListeners = new ArrayList<>();

        for (Class<?> clazz : annotatedClasses) {
            if (Listener.class.isAssignableFrom(clazz)) {
                String listenerName = registerListener(clazz);
                if (listenerName != null) registeredListeners.add(listenerName);
            }
        }

        registeredListeners.sort(String::compareTo);
        return registeredListeners;
    }

    /**
     * Registers a single listener class.
     * @param listenerClass The listener class to register.
     * @return The name of the registered listener, or null if registration failed.
     */
    private String registerListener(Class<?> listenerClass) {
        try {
            Listener listener;

            try { // Try constructor with plugin parameter first
                Constructor<?> constructor = listenerClass.getConstructor(plugin.getClass());
                listener = (Listener) constructor.newInstance(plugin);
            } catch (NoSuchMethodException e) {
                try {
                    Constructor<?> constructor = listenerClass.getConstructor();
                    listener = (Listener) constructor.newInstance();
                } catch (NoSuchMethodException ex) {
                    plugin.getLogger().severe("No suitable constructor found for listener: " + listenerClass.getSimpleName());
                    return null;
                }
            }

            if (managerRegistry != null) managerRegistry.injectManagers(listener);

            plugin.getServer().getPluginManager().registerEvents(listener, plugin);

            AutoRegisterListener annotation = listenerClass.getAnnotation(AutoRegisterListener.class);
            return annotation.name().isEmpty() ? listenerClass.getSimpleName() : annotation.name();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to register listener: " + listenerClass.getSimpleName(), e);
            return null;
        }
    }

    /**
     * Registers one or more listener instances. This method can be used for manual registration of listeners.
     * @param listener The listener(s) to register.
     */
    @Override
    public void registerListener(Listener... listener) {
        for (Listener l : listener) {
            if (l == null) continue;
            if (managerRegistry != null) managerRegistry.injectManagers(l);
            plugin.getServer().getPluginManager().registerEvents(l, plugin);
        }
    }

    /**
     * Builder class for AutoEventRegistrar.
     */
    public static class Builder {
        private JavaPlugin plugin;
        private String packagePrefix;
        private ManagerRegistry managerRegistry;

        public Builder setPlugin(JavaPlugin plugin) {
            this.plugin = plugin;
            return this;
        }

        public Builder setPackagePrefix(String packagePrefix) {
            this.packagePrefix = packagePrefix;
            return this;
        }

        public Builder setManagerRegistry(ManagerRegistry managerRegistry) {
            this.managerRegistry = managerRegistry;
            return this;
        }

        public AutoEventRegistrar build() {
            if (plugin == null) throw new IllegalStateException("Plugin must be set");
            if (packagePrefix == null) throw new IllegalStateException("Package prefix must be set");
            return new AutoEventRegistrar(plugin, packagePrefix, managerRegistry);
        }
    }
}
