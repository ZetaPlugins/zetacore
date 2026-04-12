package com.zetaplugins.pluginTest.config;

import com.zetaplugins.zetacore.config.annotation.PluginConfig;

import java.util.List;

@PluginConfig("config.yml")
public class MyConfig {
    public String lang = "de-DE";
    public SettingsConfigSection settings;
    public List<ItemConfigSection> items = List.of();
}
