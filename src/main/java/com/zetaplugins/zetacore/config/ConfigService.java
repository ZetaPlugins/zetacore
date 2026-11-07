package com.zetaplugins.zetacore.config;

import com.zetaplugins.zetacore.config.annotation.ConfigFile;
import com.zetaplugins.zetacore.di.InstanceResolver;
import com.zetaplugins.zetacore.di.annotation.Service;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Service for managing plugin configuration files with caching support.
 * <p>
 * Config classes annotated with {@link com.zetaplugins.zetacore.config.annotation.ConfigFile} are managed:
 * {@link #getConfig(Class)} always returns the same instance per class, and {@link #reload()} updates
 * those instances in place. Code holding a reference to a config object (e.g. an injected one) therefore
 * always sees the current values.
 */
@Service
public class ConfigService {
    private final JavaPlugin plugin;
    private final Map<String, FileConfiguration> configCache;
    private final Map<Class<?>, Object> managedConfigs = new LinkedHashMap<>();

    /**
     * Construct a new ConfigService.
     * @param plugin The JavaPlugin instance.
     */
    public ConfigService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configCache = new HashMap<>();
    }

    /**
     * Get a configuration file based on the provided PluginConfig enum.
     * @param config The PluginConfig enum representing the desired configuration file.
     * @return The FileConfiguration object for the specified configuration file.
     */
    public FileConfiguration getConfig(PluginConfigFile config) {
        return getFileConfigFromFileName(config.getFileName(), true);
    }

    /**
     * Get a configuration file based on the provided PluginConfig enum.
     * @param config The PluginConfig enum representing the desired configuration file.
     * @param useCache Whether to use the cached version if available.
     * @return The FileConfiguration object for the specified configuration file.
     */
    public FileConfiguration getConfig(PluginConfigFile config, boolean useCache) {
        return getFileConfigFromFileName(config.getFileName(), useCache);
    }

    /**
     * Get a configuration file based on the provided file name.
     * @param fileName The name of the configuration file (without the .yml extension).
     * @return The FileConfiguration object for the specified configuration file.
     */
    public FileConfiguration getConfig(String fileName) {
        return getFileConfigFromFileName(fileName, true);
    }

    /**
     * Get a configuration file based on the provided file name.
     * @param fileName The name of the configuration file (without the .yml extension).
     * @param useCache Whether to use the cached version if available.
     * @return The FileConfiguration object for the specified configuration file.
     */
    public FileConfiguration getConfig(String fileName, boolean useCache) {
        return getFileConfigFromFileName(fileName, useCache);
    }

    /**
     * Get the managed instance of a configuration class.
     * <p>
     * The file is mapped on the first call; later calls return the same instance.
     * @param configClass The configuration class annotated with {@link com.zetaplugins.zetacore.config.annotation.ConfigFile}.
     * @return The managed instance of the configuration class.
     * @param <T> The type of the configuration class.
     */
    public <T> T getConfig(Class<T> configClass) {
        return getConfig(configClass, true);
    }

    /**
     * Get the managed instance of a configuration class.
     * @param configClass The configuration class annotated with {@link com.zetaplugins.zetacore.config.annotation.ConfigFile}.
     * @param useCache If false, the file is re-read from disk and the managed instance is updated in place before it is returned.
     * @return The managed instance of the configuration class.
     * @param <T> The type of the configuration class.
     */
    public <T> T getConfig(Class<T> configClass, boolean useCache) {
        Object managed = managedConfigs.get(configClass);
        if (managed != null && useCache) return configClass.cast(managed);

        T fresh = mapConfig(configClass, useCache);
        if (managed == null) {
            managedConfigs.put(configClass, fresh);
            return fresh;
        }
        copyFields(fresh, managed);
        return configClass.cast(managed);
    }

    /**
     * Reload all configuration files from disk.
     * <p>
     * Clears the file cache, reloads the plugin's default config and updates every managed config instance in place.
     * Values missing from a file fall back to the field defaults of the config class.
     * If any config fails to map, no managed instance is changed.
     * @throws ConfigMappingException If a configuration file cannot be mapped.
     */
    public void reload() {
        configCache.clear();
        plugin.reloadConfig();

        Map<Object, Object> updates = new IdentityHashMap<>();
        for (Map.Entry<Class<?>, Object> entry : managedConfigs.entrySet()) {
            updates.put(entry.getValue(), mapConfig(entry.getKey(), true));
        }
        updates.forEach((managed, fresh) -> copyFields(fresh, managed));
    }

    /**
     * Creates a resolver that lets a {@link com.zetaplugins.zetacore.di.ServiceRegistry} inject managed config instances.
     * {@link com.zetaplugins.zetacore.ZetaCorePlugin} registers it automatically.
     * <pre>
     * registry.addResolver(configService.asResolver());
     * </pre>
     * @return A resolver for classes annotated with {@link ConfigFile}.
     */
    public InstanceResolver asResolver() {
        return type -> type.isAnnotationPresent(ConfigFile.class) ? getConfig(type) : null;
    }

    private <T> T mapConfig(Class<T> configClass, boolean useCache) {
        String fileName = ConfigMapper.toFileName(configClass);
        FileConfiguration fileConfig = getConfig(fileName, useCache);
        if (fileConfig == null) throw new IllegalStateException("Configuration file not found: " + fileName);

        return ConfigMapper.map(fileConfig, configClass);
    }

    private static void copyFields(Object source, Object target) {
        Class<?> cls = target.getClass();
        while (cls != null && cls != Object.class) {
            for (Field field : cls.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) continue;
                try {
                    field.setAccessible(true);
                    field.set(target, field.get(source));
                } catch (IllegalAccessException e) {
                    throw new ConfigMappingException("Failed to update field '" + field.getName()
                            + "' of " + target.getClass().getName(), e);
                }
            }
            cls = cls.getSuperclass();
        }
    }

    private FileConfiguration getFileConfigFromFileName(String fileName, boolean useCache) {
        String normalizedFileName = normalizeFileName(fileName);

        if (useCache && configCache.containsKey(normalizedFileName)) return configCache.get(normalizedFileName);

        File configFile = new File(plugin.getDataFolder(), normalizedFileName);
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            plugin.saveResource(normalizedFileName, false);
        }

        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(configFile);
        if (useCache) configCache.put(normalizedFileName, fileConfig);
        return fileConfig;
    }

    /**
     * Save a configuration file based on the provided PluginConfig enum.
     * @param config The PluginConfig enum representing the configuration file to save.
     * @param fileConfig The FileConfiguration object to save.
     */
    public void saveConfig(PluginConfigFile config, FileConfiguration fileConfig) {
        saveFileConfigToFileName(config.getFileName(), fileConfig);
    }

    /**
     * Save a configuration file based on the provided file name.
     * @param fileName The name of the configuration file (without the .yml extension).
     * @param fileConfig The FileConfiguration object to save.
     */
    public void saveConfig(String fileName, FileConfiguration fileConfig) {
        saveFileConfigToFileName(fileName, fileConfig);
    }

    private void saveFileConfigToFileName(String fileName, FileConfiguration fileConfig) {
        String normalizedFileName = normalizeFileName(fileName);
        File configFile = new File(plugin.getDataFolder(), normalizedFileName);
        try {
            fileConfig.save(configFile);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save configuration file: " + normalizedFileName, e);
        } finally {
            configCache.remove(normalizedFileName);
        }
    }

    /**
     * Normalize the file name to ensure it has the correct .yml extension.
     * @param fileName The original file name.
     * @return The normalized file name with .yml extension.
     */
    private String normalizeFileName(String fileName) {
        return fileName.endsWith(".yml") ? fileName : fileName + ".yml";
    }

    /**
     * Clear the configuration cache.
     */
    public void clearCache() {
        configCache.clear();
    }
}
