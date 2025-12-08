package com.zetaplugins.zetacore.services.config.testconfigs;


import com.zetaplugins.zetacore.annotations.PluginConfig;

import java.util.List;
import java.util.Map;

@PluginConfig("config.yml")
public class MyConfig {
    public String lang = "de-DE";
    public SettingsConfigSection settings;
    public List<ItemConfigSection> items = List.of();
    public Map<String, AdvancedConfigItem> advancedItems = Map.of();
}
