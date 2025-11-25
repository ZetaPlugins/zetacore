package com.zetaplugins.zetacore.services.localization;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

/**
 * Service for handling localization and language files.
 * This service loads the language file based on the configuration and provides methods to retrieve localized strings.
 */
public final class BukkitLocalizationService implements LocalizationService {
    private final JavaPlugin plugin;
    private final List<String> possibleLangs;
    private final String fallbackLang;
    private final String langFolder;
    private final String langConfigOption;

    private FileConfiguration langConfig;

    /**
     * @param plugin The JavaPlugin instance to use for loading resources
     * @param possibleLangs List of language codes to load (The languages that are provided by the plugin)
     */
    public BukkitLocalizationService(JavaPlugin plugin, List<String> possibleLangs) {
        this.plugin = plugin;
        this.possibleLangs = possibleLangs;
        this.fallbackLang = "en-US";
        this.langFolder = "lang/";
        this.langConfigOption = "lang";
        loadLanguageConfig();
    }

    /**
     * @param plugin The JavaPlugin instance to use for loading resources
     * @param possibleLangs List of language codes to load (The languages that are provided by the plugin)
     * @param fallbackLang The fallback language code to use if the selected language is not found
     * @param langFolder The folder where the language files are stored (e.g., "lang/")
     * @param langConfigOption The configuration option to use for selecting the language (e.g., "lang")
     */
    public BukkitLocalizationService(JavaPlugin plugin, List<String> possibleLangs, String fallbackLang, String langFolder, String langConfigOption) {
        this.plugin = plugin;
        this.possibleLangs = possibleLangs;
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

        for (String langString : possibleLangs) {
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

    @Override
    public String getString(String key) {
        return langConfig.getString(key);
    }

    @Override
    public String getString(String key, String fallback) {
        return langConfig.getString(key) != null ? langConfig.getString(key) : fallback;
    }

    @Override
    public List<String> getStringList(String key) {
        return langConfig.getStringList(key);
    }

    /**
     * Builder class for creating instances of BukkitLocalizationService
     */
    public static class Builder {
        private JavaPlugin plugin;
        private List<String> possibleLangs;
        private String fallbackLang = "en-US";
        private String langFolder = "lang/";
        private String langConfigOption = "lang";

        public Builder setPlugin(JavaPlugin plugin) {
            this.plugin = plugin;
            return this;
        }

        public Builder setPossibleLangs(List<String> possibleLangs) {
            this.possibleLangs = possibleLangs;
            return this;
        }

        public Builder setFallbackLang(String fallbackLang) {
            this.fallbackLang = fallbackLang;
            return this;
        }

        public Builder setLangFolder(String langFolder) {
            this.langFolder = langFolder;
            return this;
        }

        public Builder setLangConfigOption(String langConfigOption) {
            this.langConfigOption = langConfigOption;
            return this;
        }

        /**
         * Build the BukkitLocalizationService instance
         * @return The constructed BukkitLocalizationService
         */
        public BukkitLocalizationService build() {
            if (plugin == null) throw new IllegalStateException("Plugin must be set");
            if (possibleLangs == null || possibleLangs.isEmpty())
                throw new IllegalStateException("Possible languages must be set and not empty");
            return new BukkitLocalizationService(plugin, possibleLangs, fallbackLang, langFolder, langConfigOption);
        }
    }
}
