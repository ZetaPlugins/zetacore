package com.zetaplugins.zetacore.services;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

/**
 * Service for handling localization and language files.
 * This service loads the language file based on the configuration and provides methods to retrieve localized strings.
 */
public final class LocalizationService {
    private final JavaPlugin plugin;
    private final List<String> defaultLangs;
    private final String fallbackLang;
    private final String langFolder;
    private final String langConfigOption;

    private FileConfiguration langConfig;

    /**
     * @param plugin The JavaPlugin instance to use for loading resources
     * @param defaultLangs List of default language codes to load (The languages that are provided by the plugin)
     */
    public LocalizationService(JavaPlugin plugin, List<String> defaultLangs) {
        this.plugin = plugin;
        this.defaultLangs = defaultLangs;
        this.fallbackLang = "en-US";
        this.langFolder = "lang/";
        this.langConfigOption = "lang";
        loadLanguageConfig();
    }

    /**
     * @param plugin The JavaPlugin instance to use for loading resources
     * @param defaultLangs List of default language codes to load (The languages that are provided by the plugin)
     * @param fallbackLang The fallback language code to use if the selected language is not found
     * @param langFolder The folder where the language files are stored (e.g., "lang/")
     * @param langConfigOption The configuration option to use for selecting the language (e.g., "lang")
     */
    public LocalizationService(JavaPlugin plugin, List<String> defaultLangs, String fallbackLang, String langFolder, String langConfigOption) {
        this.plugin = plugin;
        this.defaultLangs = defaultLangs;
        this.fallbackLang = fallbackLang;
        this.langFolder = langFolder;
        this.langConfigOption = langConfigOption;
        loadLanguageConfig();
    }

    /**
     * Reload the language configuration from the language files
     */
    public void reload() {
        loadLanguageConfig();
    }

    /**
     * Load the language file from the plugin data folder
     */
    private void loadLanguageConfig() {
        File languageDirectory = new File(plugin.getDataFolder(), langFolder);
        if (!languageDirectory.exists() || !languageDirectory.isDirectory()) languageDirectory.mkdir();

        for (String langString : defaultLangs) {
            File langFile = new File(langFolder, langString + ".yml");
            if (!new File(languageDirectory, langString + ".yml").exists()) {
                plugin.getLogger().info("Saving file " + langFile.getPath());
                plugin.saveResource(langFile.getPath(), false);
            }
        }

        String langOption = plugin.getConfig().getString(langConfigOption, fallbackLang);
        File selectedLangFile = new File(languageDirectory, langOption + ".yml");

        if (!selectedLangFile.exists()) {
            selectedLangFile = new File(languageDirectory, fallbackLang + ".yml");
            plugin.getLogger().warning("Language file " + langOption + ".yml (" + selectedLangFile.getPath() + ") not found! Using fallback " + fallbackLang + ".yml.");
        }

        plugin.getLogger().info("Using language file: " + selectedLangFile.getPath());
        langConfig = YamlConfiguration.loadConfiguration(selectedLangFile);
    }

    /**
     * Get a string from the language file
     * @param key The key to get the string for
     * @return The string from the language file
     */
    public String getString(String key) {
        return langConfig.getString(key);
    }

    /**
     * Get a string from the language file with a fallback
     * @param key The key to get the string for
     * @param fallback The fallback string
     * @return The string from the language file or the fallback
     */
    public String getString(String key, String fallback) {
        return langConfig.getString(key) != null ? langConfig.getString(key) : fallback;
    }

    /**
     * Get a list of strings from the language file
     * @param key The key to get the list of strings for
     * @return The list of strings from the language file
     */
    public List<String> getStringList(String key) {
        return langConfig.getStringList(key);
    }
}
