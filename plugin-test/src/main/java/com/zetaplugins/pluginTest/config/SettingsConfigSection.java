package com.zetaplugins.pluginTest.config;

import com.zetaplugins.zetacore.config.annotation.NestedConfig;

import java.util.List;

@NestedConfig
public class SettingsConfigSection {
    public boolean enableFeature = true;
    public List<String> funnynames;
}
