package com.zetaplugins.zetacore.config.testconfigs;

import com.zetaplugins.zetacore.config.annotation.ConfigSection;

import java.util.List;

@ConfigSection
public class SettingsConfigSection {
    public boolean enableFeature = true;
    public List<String> funnynames;
}
