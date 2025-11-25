package com.zetaplugins.zetacore.services.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing plugin configuration files with caching support.
 */
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
    public FileConfiguration getConfig(PluginConfig config) {
        return getFileConfigFromFileName(config.getFileName(), true);
    }

    /**
     * Get a configuration file based on the provided PluginConfig enum.
     * @param config The PluginConfig enum representing the desired configuration file.
     * @param useCache Whether to use the cached version if available.
     * @return The FileConfiguration object for the specified configuration file.
     */
    public FileConfiguration getConfig(PluginConfig config, boolean useCache) {
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

    private FileConfiguration getFileConfigFromFileName(String fileName, boolean useCache) {
        if (useCache && configCache.containsKey(fileName)) return configCache.get(fileName);

        File configFile = new File(plugin.getDataFolder(), fileName + ".yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            plugin.saveResource(fileName + ".yml", false);
        }

        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(configFile);
        if (useCache) configCache.put(fileName, fileConfig);
        return fileConfig;
    }

    /**
     * Clear the configuration cache.
     */
    public void clearCache() {
        configCache.clear();
    }
}
