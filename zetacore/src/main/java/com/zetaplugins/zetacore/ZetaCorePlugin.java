package com.zetaplugins.zetacore;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ZetaCorePlugin extends JavaPlugin {
    public File getPluginFile() {
        return getFile();
    }
}
