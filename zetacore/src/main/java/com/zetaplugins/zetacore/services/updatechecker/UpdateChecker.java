package com.zetaplugins.zetacore.services.updatechecker;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * Abstract class for checking updates for a JavaPlugin.
 * Subclasses should implement the checkForUpdates method to define
 * how updates are checked.
 */
public abstract class UpdateChecker {
    private final JavaPlugin plugin;
    private final Logger logger;
    private boolean newVersionAvailable = false;
    private String latestVersion;

    /**
     * Constructs an UpdateChecker for the given plugin.
     * @param plugin The JavaPlugin to check updates for
     */
    public UpdateChecker(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    /**
     * Checks for updates for the plugin.
     * @param logMessage If true, logs a message if a new version is available
     */
    public abstract void checkForUpdates(boolean logMessage);

    /**
     * Generates a console message indicating that a new version is available.
     * @param latestVersion The latest version available
     * @param currentVersion The current version of the plugin
     * @param newVersionUrl The URL to download the new version
     * @return The formatted console message
     */
    protected String getNewVersionConsoleMessage(String latestVersion, String currentVersion, String newVersionUrl) {
        final String reset = "\u001B[0m";
        final String bold = "\u001B[1m";
        final String darkGray = "\u001B[90m";
        final String lightGray = "\u001B[37m";
        final String green = "\u001B[32m";

        return "\n" +
                darkGray + "==========================================" + reset + "\n" +
                bold + "A new version of " + getPlugin().getName() +  " is available!" + reset + "\n" +
                bold + "New Version: " + reset + bold + green + latestVersion + reset + lightGray + " (Your version: " + currentVersion + ")" + reset + "\n" +
                bold + "Download here: " + reset + lightGray + reset + newVersionUrl + "\n" +
                darkGray + "==========================================" + reset;
    }

    protected void setNewVersionAvailable(boolean available) {
        this.newVersionAvailable = available;
    }

    public boolean isNewVersionAvailable() {
        return newVersionAvailable;
    }

    protected void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    protected JavaPlugin getPlugin() {
        return plugin;
    }

    protected Logger getLogger() {
        return logger;
    }
}
