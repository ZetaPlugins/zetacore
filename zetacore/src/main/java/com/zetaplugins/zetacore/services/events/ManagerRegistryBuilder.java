package com.zetaplugins.zetacore.services.events;

import com.zetaplugins.zetacore.services.di.ManagerRegistry;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A builder for creating instances of {@link ManagerRegistry} with customizable options.
 */
public class ManagerRegistryBuilder {
    private JavaPlugin plugin;
    private boolean requireManagerAnnotation = false;
    private String packagePrefix;

    public ManagerRegistryBuilder setPlugin(JavaPlugin plugin) {
        this.plugin = plugin;
        if (this.packagePrefix == null) this.packagePrefix = plugin.getClass().getPackageName();
        return this;
    }

    public ManagerRegistryBuilder setRequireManagerAnnotation(boolean requireManagerAnnotation) {
        this.requireManagerAnnotation = requireManagerAnnotation;
        return this;
    }

    public ManagerRegistryBuilder setPackagePrefix(String packagePrefix) {
        this.packagePrefix = packagePrefix;
        return this;
    }

    public ManagerRegistry build() {
        if (plugin == null) throw new IllegalStateException("Plugin must be set before building ManagerRegistry.");
        if (packagePrefix == null) throw new IllegalStateException("Package prefix must be set before building ManagerRegistry.");
        return new ManagerRegistry(plugin, requireManagerAnnotation, packagePrefix);
    }
}
