package com.zetaplugins.zetacore.services.config;

/**
 * Interface for plugin configuration files. This is supposed to be implmented by enums.
 */
public interface PluginConfig {
    /**
     * Get the file name withut extension
     * @return the file name
     */
    public String getFileName();
}
