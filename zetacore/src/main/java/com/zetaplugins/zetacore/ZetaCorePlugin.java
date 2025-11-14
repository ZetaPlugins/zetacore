package com.zetaplugins.zetacore;

import com.zetaplugins.zetacore.services.bStats.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ZetaCorePlugin extends JavaPlugin {
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
