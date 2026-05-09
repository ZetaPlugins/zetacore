package com.zetaplugins.zetacore.config.testconfigs;


import com.zetaplugins.zetacore.config.annotation.ConfigKey;
import com.zetaplugins.zetacore.config.annotation.ConfigFile;

import java.util.List;
import java.util.Map;

@ConfigFile("config.yml")
public class MyConfig {
    public String lang = "de-DE";
    public SomeStatus someStatus = SomeStatus.ACTIVE;
    @ConfigKey(name = "attr-with-dash")
    public String attrWithDash = "default-test-value";
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
