package com.zetaplugins.zetacore.services.config.testconfigs;

import com.zetaplugins.zetacore.annotations.NestedConfig;

import java.util.List;

@NestedConfig
public class SettingsConfigSection {
    public boolean enableFeature = true;
    public List<String> funnynames;
}
