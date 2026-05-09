package com.zetaplugins.pluginTest.config;

import com.zetaplugins.zetacore.config.annotation.ConfigFile;

import java.util.List;

@ConfigFile("config.yml")
public class MyConfig {
    private String lang = "de-DE";
    private SettingsConfigSection settings;
    private List<ItemConfigSection> items = List.of();

    public String getLang() {
        return lang;
    }

    public SettingsConfigSection getSettings() {
        return settings;
    }

    public List<ItemConfigSection> getItems() {
        return items;
    }
}
