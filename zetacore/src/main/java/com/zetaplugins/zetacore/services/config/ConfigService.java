package com.zetaplugins.zetacore.services.config;

import com.zetaplugins.zetacore.annotations.Manager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Service for managing plugin configuration files with caching support.
 */
@Manager
public class ConfigService {
    private final JavaPlugin plugin;
    private final Map<String, FileConfiguration> configCache;

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
     * Get a configuration mapped to the specified configuration class.
     * @param configClass The configuration class annotated with @PluginConfig.
     * @return An instance of the configuration class populated with values from the configuration file.
     * @param <T> The type of the configuration class.
     */
    public <T> T getConfig(Class<T> configClass) {
        return getConfig(configClass, true);
    }

    /**
     * Get a configuration mapped to the specified configuration class.
     * @param configClass The configuration class annotated with @PluginConfig.
     * @param useCache Whether to use the cached version if available.
     * @return An instance of the configuration class populated with values from the configuration file.
     * @param <T> The type of the configuration class.
     */
    public <T> T getConfig(Class<T> configClass, boolean useCache) {
        String fileName = ConfigMapper.toFileName(configClass);
        FileConfiguration fileConfig = getConfig(fileName, useCache);
        if (fileConfig == null) throw new IllegalStateException("Configuration file not found: " + fileName);

        return ConfigMapper.map(fileConfig, configClass);
    }

    private FileConfiguration getFileConfigFromFileName(String fileName, boolean useCache) {
        if (useCache && configCache.containsKey(fileName)) return configCache.get(fileName);

        String fileNameWithExtension = fileName.endsWith(".yml") ? fileName : fileName + ".yml";
        File configFile = new File(plugin.getDataFolder(), fileNameWithExtension);
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            plugin.saveResource(fileNameWithExtension, false);
        }

        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(configFile);
        if (useCache) configCache.put(fileName, fileConfig);
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
        String fileNameWithExtension = fileName.endsWith(".yml") ? fileName : fileName + ".yml";
        File configFile = new File(plugin.getDataFolder(), fileNameWithExtension);
        try {
            fileConfig.save(configFile);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save configuration file: " + fileName + ".yml", e);
        } finally {
            configCache.remove(fileName);
        }
    }

    /**
     * Clear the configuration cache.
     */
    public void clearCache() {
        configCache.clear();
    }
}
