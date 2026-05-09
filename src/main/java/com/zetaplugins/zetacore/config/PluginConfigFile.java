package com.zetaplugins.zetacore.config;

/**
 * Interface for plugin configuration files. This is supposed to be implmented by enums.
 */
public interface PluginConfigFile {
    /**
     * Get the file name withut extension
     * @return the file name
     */
    public String getFileName();
}
