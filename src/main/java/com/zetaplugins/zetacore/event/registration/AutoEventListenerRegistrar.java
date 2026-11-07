package com.zetaplugins.zetacore.event.registration;

import com.zetaplugins.zetacore.event.annotation.EventListener;
import com.zetaplugins.zetacore.di.ServiceRegistry;
import com.zetaplugins.zetacore.di.condition.ConditionEvaluator;
import com.zetaplugins.zetacore.scan.ClassScanner;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Manages the registration of event listeners for a plugin.
 * Use the {@link EventListener} annotation to mark listener classes for automatic registration.
 */
public class AutoEventListenerRegistrar implements EventListenerRegistrar {
    private final JavaPlugin plugin;
    private final ClassScanner scanner;
    private final ServiceRegistry serviceRegistry;
    private final ConditionEvaluator conditions;

    /**
     * @param plugin The JavaPlugin instance.
     * @param packagePrefix The package prefix to scan for annotated classes.
     */
    public AutoEventListenerRegistrar(JavaPlugin plugin, String packagePrefix) {
        this(plugin, packagePrefix, null);
    }

    /**
     * @param plugin The JavaPlugin instance.
     * @param packagePrefix The package prefix to scan for annotated classes.
     * @param serviceRegistry The ServiceRegistry for dependency injection.
     */
    public AutoEventListenerRegistrar(JavaPlugin plugin, String packagePrefix, ServiceRegistry serviceRegistry) {
        this(plugin, new ClassScanner(packagePrefix, plugin.getClass().getClassLoader()), serviceRegistry);
    }

    /**
     * @param plugin The JavaPlugin instance.
     * @param scanner The scanner used to find annotated classes.
     * @param serviceRegistry The ServiceRegistry for dependency injection, or null.
     */
    public AutoEventListenerRegistrar(JavaPlugin plugin, ClassScanner scanner, ServiceRegistry serviceRegistry) {
        this.plugin = plugin;
        this.scanner = scanner;
        this.serviceRegistry = serviceRegistry;
        this.conditions = serviceRegistry != null
                ? serviceRegistry.getConditionEvaluator()
                : new ConditionEvaluator(plugin, null);
    }

    /**
     * Registers all listener classes annotated with {@link EventListener}.
     * @return A list of names of the registered listeners.
     */
    @Override
    public List<String> registerAllListeners() {
        Set<Class<?>> annotatedClasses = scanner.getTypesAnnotatedWith(EventListener.class);
        List<String> registeredListeners = new ArrayList<>();

        for (Class<?> clazz : annotatedClasses) {
            if (Listener.class.isAssignableFrom(clazz) && conditions.matches(clazz)) {
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

            if (serviceRegistry != null) {
                listener = (Listener) serviceRegistry.create(listenerClass);
            } else {
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
            }

            plugin.getServer().getPluginManager().registerEvents(listener, plugin);

            return listenerClass.getSimpleName();
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
            if (serviceRegistry != null) serviceRegistry.injectServices(l);
            plugin.getServer().getPluginManager().registerEvents(l, plugin);
        }
    }

    /**
     * Builder class for AutoEventListenerRegistrar.
     */
    public static class Builder {
        private JavaPlugin plugin;
        private String packagePrefix;
        private ServiceRegistry serviceRegistry;
        private ClassScanner scanner;

        public Builder withPlugin(JavaPlugin plugin) {
            this.plugin = plugin;
            return this;
        }

        public Builder withPackagePrefix(String packagePrefix) {
            this.packagePrefix = packagePrefix;
            return this;
        }

        public Builder withServiceRegistry(ServiceRegistry serviceRegistry) {
            this.serviceRegistry = serviceRegistry;
            return this;
        }

        /**
         * Builds the AutoEventListenerRegistrar instance.
         * @return The constructed AutoEventListenerRegistrar.
         */
        /**
         * Uses an existing scanner instead of creating one from the package prefix.
         * Defaults to the service registry's scanner if a registry is set.
         * @param scanner The scanner to use.
         * @return This builder.
         */
        public Builder withClassScanner(ClassScanner scanner) {
            this.scanner = scanner;
            return this;
        }

        public AutoEventListenerRegistrar build() {
            if (plugin == null) throw new IllegalStateException("Plugin must be set");
            ClassScanner resolvedScanner = scanner;
            if (resolvedScanner == null && serviceRegistry != null
                    && (packagePrefix == null || packagePrefix.equals(serviceRegistry.getClassScanner().getPackagePrefix()))) {
                resolvedScanner = serviceRegistry.getClassScanner();
            }
            if (resolvedScanner == null) {
                if (packagePrefix == null) throw new IllegalStateException("Package prefix, class scanner or service registry must be set");
                resolvedScanner = new ClassScanner(packagePrefix, plugin.getClass().getClassLoader());
            }
            return new AutoEventListenerRegistrar(plugin, resolvedScanner, serviceRegistry);
        }
    }
}
