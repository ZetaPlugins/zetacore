package com.zetaplugins.zetacore.services.config.testconfigs;


import com.zetaplugins.zetacore.annotations.PluginConfig;

import java.util.List;

@PluginConfig("config.yml")
public class MyConfig {
    public String lang = "de-DE";
    public SettingsConfigSection settings;
    public List<ItemConfigSection> items = List.of();
}
