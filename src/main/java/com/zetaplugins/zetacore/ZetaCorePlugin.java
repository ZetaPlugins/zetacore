package com.zetaplugins.zetacore;

import com.zetaplugins.zetacore.annotation.ZetaPlugin;
import com.zetaplugins.zetacore.command.registration.AutoCommandRegistrar;
import com.zetaplugins.zetacore.config.ConfigService;
import com.zetaplugins.zetacore.di.ServiceRegistry;
import com.zetaplugins.zetacore.event.registration.AutoEventListenerRegistrar;
import com.zetaplugins.zetacore.integration.bstats.Metrics;
import com.zetaplugins.zetacore.scan.ClassScanner;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

/**
 * Base class for plugins built on ZetaCore.
 * <p>
 * On enable, the plugin boots itself: it saves the default config, creates the {@link ConfigService} and
 * {@link ServiceRegistry}, registers the beans of all {@link com.zetaplugins.zetacore.di.annotation.Configuration}
 * classes, starts eagerly loaded services and beans and registers all commands and listeners found
 * in the plugin's package. Use {@link ZetaPlugin} on the main class to change the defaults, and override
 * {@link #onStartup()} and {@link #onShutdown()} for custom logic.
 * <p>
 * The plugin itself, the {@link ConfigService}, the {@link ServiceRegistry}, the {@link ClassScanner} and
 * every {@link com.zetaplugins.zetacore.config.annotation.ConfigFile} class can be injected into any service.
 * Injected config objects are updated in place by {@link ConfigService#reload()}.
 */
public abstract class ZetaCorePlugin extends JavaPlugin {
    private ClassScanner classScanner;
    private ConfigService configService;
    private ServiceRegistry serviceRegistry;
    private boolean started;

    @Override
    public final void onEnable() {
        try {
            boot();
            started = true;
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to enable " + getName() + ", disabling plugin.", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public final void onDisable() {
        if (!started) return;
        started = false;
        try {
            onShutdown();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error while disabling " + getName(), e);
        }
    }

    private void boot() {
        ZetaPlugin settings = getClass().getAnnotation(ZetaPlugin.class);
        String packagePrefix = settings != null && !settings.packagePrefix().isEmpty()
                ? settings.packagePrefix()
                : getClass().getPackageName();
        boolean requireServiceAnnotation = settings == null || settings.requireServiceAnnotation();
        String commandNamespace = settings != null && !settings.commandNamespace().isEmpty()
                ? settings.commandNamespace()
                : getName().toLowerCase();

        if (getResource("config.yml") != null) saveDefaultConfig();

        classScanner = new ClassScanner(packagePrefix, getClassLoader());
        configService = new ConfigService(this);
        serviceRegistry = new ServiceRegistry(this, requireServiceAnnotation, classScanner);
        serviceRegistry.registerInstance(configService);
        serviceRegistry.addResolver(configService.asResolver());

        serviceRegistry.registerConfigurations();
        serviceRegistry.initializeEagerServices();

        List<String> commands = new AutoCommandRegistrar(this, classScanner, commandNamespace, serviceRegistry)
                .registerAllCommands();
        List<String> listeners = new AutoEventListenerRegistrar(this, classScanner, serviceRegistry)
                .registerAllListeners();
        getLogger().info("Registered " + commands.size() + " commands and " + listeners.size() + " listeners.");

        onStartup();
    }

    /**
     * Called at the end of {@link #onEnable()}, after all services, commands and listeners are set up.
     */
    protected void onStartup() {
    }

    /**
     * Called when the plugin is disabled. Not called if enabling the plugin failed.
     */
    protected void onShutdown() {
    }

    /**
     * Gets the service registry of this plugin. Only available once the plugin is enabled.
     * @return The service registry.
     */
    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    /**
     * Gets the config service of this plugin. Only available once the plugin is enabled.
     * @return The config service.
     */
    public ConfigService getConfigService() {
        return configService;
    }

    /**
     * Gets the scanner used to discover this plugin's classes. Only available once the plugin is enabled.
     * @return The class scanner.
     */
    public ClassScanner getClassScanner() {
        return classScanner;
    }

    /**
     * Gets the plugin jar file.
     * @return The plugin file.
     */
    public File getPluginFile() {
        return getFile();
    }

    /**
     * Creates bStats metrics instance for this plugin.
     * @param pluginId The plugin bStats id
     * @return The metrics instance
     */
    public Metrics createBStatsMetrics(int pluginId) {
        return new Metrics(this, pluginId);
    }
}
