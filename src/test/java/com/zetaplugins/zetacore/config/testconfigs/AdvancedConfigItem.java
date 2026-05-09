package com.zetaplugins.zetacore.config.testconfigs;

import com.zetaplugins.zetacore.config.annotation.ConfigSection;

import java.util.List;

@ConfigSection
public class AdvancedConfigItem {
    public int id;
    public String type;
    public List<String> description;
}
