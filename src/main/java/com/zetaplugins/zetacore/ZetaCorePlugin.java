package com.zetaplugins.zetacore;

import com.zetaplugins.zetacore.annotation.ZetaPlugin;
import com.zetaplugins.zetacore.command.registration.AutoCommandRegistrar;
import com.zetaplugins.zetacore.command.registration.CommandRegistrationCustomizer;
import com.zetaplugins.zetacore.config.ConfigService;
import com.zetaplugins.zetacore.di.ServiceRegistry;
import com.zetaplugins.zetacore.di.annotation.Service;
import com.zetaplugins.zetacore.event.registration.AutoEventListenerRegistrar;
import com.zetaplugins.zetacore.integration.bstats.Metrics;
import com.zetaplugins.zetacore.integration.bstats.MetricsCustomizer;
import com.zetaplugins.zetacore.integration.papi.PapiExpansionService;
import com.zetaplugins.zetacore.integration.papi.PlaceholderExpansionCustomizer;
import com.zetaplugins.zetacore.integration.papi.annotation.Papi;
import com.zetaplugins.zetacore.integration.updatechecker.UpdateChecker;
import com.zetaplugins.zetacore.permission.PermissionRegistrar;
import com.zetaplugins.zetacore.scan.ClassScanner;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Base class for plugins built on ZetaCore.
 * <p>
 * On enable, the plugin boots itself. It saves the default config, creates the {@link ConfigService} and
 * {@link ServiceRegistry}, registers the beans of all {@link com.zetaplugins.zetacore.di.annotation.Configuration}
 * classes and starts eagerly loaded services and beans. It then registers everything found in the plugin's package:
 * <ul>
 *     <li>permissions (see {@link com.zetaplugins.zetacore.permission.PermissionHolder})</li>
 *     <li>commands and tab completers, filtered by {@link CommandRegistrationCustomizer}s</li>
 *     <li>event listeners</li>
 *     <li>{@link Papi} placeholders, if PlaceholderAPI is installed, customized by {@link PlaceholderExpansionCustomizer}s</li>
 * </ul>
 * Finally, it starts bStats if {@link ZetaPlugin#bstatsId()} is set (charts come from {@link MetricsCustomizer}s)
 * and runs every {@link UpdateChecker} service or bean asynchronously.
 * <p>
 * Use {@link ZetaPlugin} on the main class to change the defaults, and override
 * {@link #onStartup()} and {@link #onShutdown()} for custom logic.
 * <p>
 * The plugin itself, its {@link java.util.logging.Logger}, the {@link ConfigService}, the {@link ServiceRegistry}, the {@link ClassScanner} and
 * every {@link com.zetaplugins.zetacore.config.annotation.ConfigFile} class can be injected into any service.
 * Injected config objects are updated in place by {@link ConfigService#reload()}.
 */
public abstract class ZetaCorePlugin extends JavaPlugin {
    private ClassScanner classScanner;
    private ConfigService configService;
    private ServiceRegistry serviceRegistry;
    private Metrics metrics;
    private boolean started;

    @Override
    public final void onEnable() {
        try {
            boot();
            started = true;
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to enable " + getName() + ", disabling plugin.", e);
            stopServices();
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
        stopServices();
    }

    private void stopServices() {
        if (metrics != null) {
            metrics.shutdown();
            metrics = null;
        }
        if (serviceRegistry != null) serviceRegistry.shutdown();
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

        int permissions = PermissionRegistrar.registerAll(PermissionRegistrar.discover(classScanner));
        List<String> commands = registerCommands(commandNamespace);
        List<String> listeners = new AutoEventListenerRegistrar(this, classScanner, serviceRegistry)
                .registerAllListeners();
        int placeholders = registerPlaceholders();
        if (settings != null && settings.bstatsId() > 0) startMetrics(settings.bstatsId());
        checkForUpdates();

        getLogger().info(String.format("Enabled with %d services, %d commands, %d listeners, %d permissions and %d placeholders.",
                serviceRegistry.getSingletonCount(), commands.size(), listeners.size(), permissions, placeholders));

        onStartup();
    }

    private List<String> registerCommands(String commandNamespace) {
        List<CommandRegistrationCustomizer> customizers = serviceRegistry.getAll(CommandRegistrationCustomizer.class);
        List<String> commands = new AutoCommandRegistrar(this, classScanner, commandNamespace, serviceRegistry)
                .registerAllCommands(name -> customizers.stream().allMatch(c -> c.shouldRegister(name)));
        List<String> registered = List.copyOf(commands);
        customizers.forEach(c -> c.onCommandsRegistered(registered));
        return registered;
    }

    private int registerPlaceholders() {
        Set<Class<?>> providers = classScanner.getTypesWithMethodsAnnotatedWith(Papi.class);
        if (providers.isEmpty() || !PapiExpansionService.hasPapi()) return 0;

        PapiExpansionService expansion = new PapiExpansionService(this);
        for (Class<?> provider : providers) {
            if (!serviceRegistry.getConditionEvaluator().matches(provider)) continue;
            Object instance = provider.isAnnotationPresent(Service.class)
                    ? serviceRegistry.getOrCreate(provider)
                    : serviceRegistry.create(provider);
            expansion.addAnnotatedPlaceholders(instance);
        }
        serviceRegistry.getAll(PlaceholderExpansionCustomizer.class).forEach(c -> c.customize(expansion));

        if (expansion.getPlaceholderCount() == 0) return 0;
        if (!expansion.register()) {
            getLogger().warning("Failed to register PlaceholderAPI expansion '" + expansion.getIdentifier() + "'.");
            return 0;
        }
        return expansion.getPlaceholderCount();
    }

    private void startMetrics(int bstatsId) {
        metrics = new Metrics(this, bstatsId);
        serviceRegistry.getAll(MetricsCustomizer.class).forEach(c -> c.customize(metrics));
    }

    private void checkForUpdates() {
        List<UpdateChecker> checkers = serviceRegistry.getAll(UpdateChecker.class);
        if (checkers.isEmpty()) return;
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            for (UpdateChecker checker : checkers) {
                try {
                    checker.checkForUpdates(true);
                } catch (Exception e) {
                    getLogger().log(Level.WARNING, "Update check failed", e);
                }
            }
        });
    }

    /**
     * Called at the end of {@link #onEnable()}, after everything is set up.
     */
    protected void onStartup() {
    }

    /**
     * Called when the plugin is disabled, before services and beans are destroyed
     * (see {@link com.zetaplugins.zetacore.di.annotation.PreDestroy}). Not called if enabling the plugin failed.
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
