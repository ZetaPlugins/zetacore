package com.zetaplugins.zetacore.services.config.testconfigs;


import com.zetaplugins.zetacore.annotations.PluginConfig;

import java.util.List;
import java.util.Map;

@PluginConfig("config.yml")
public class MyConfig {
    public String lang = "de-DE";
    public SomeStatus someStatus = SomeStatus.ACTIVE;
    public List<SomeStatus> statusList = List.of(SomeStatus.ACTIVE, SomeStatus.INACTIVE);
    public Map<SomeStatus, Integer> statusToCodeMap = Map.of(
            SomeStatus.ACTIVE, 200,
            SomeStatus.INACTIVE, 300,
            SomeStatus.PENDING, 400
    );
    public SettingsConfigSection settings;
    public List<ItemConfigSection> items = List.of();
    public Map<String, AdvancedConfigItem> advancedItems = Map.of();
}
