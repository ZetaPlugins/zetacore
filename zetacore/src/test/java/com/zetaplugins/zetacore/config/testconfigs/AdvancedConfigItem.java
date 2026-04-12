package com.zetaplugins.zetacore.config.testconfigs;

import com.zetaplugins.zetacore.config.annotation.NestedConfig;

import java.util.List;

@NestedConfig
public class AdvancedConfigItem {
    public int id;
    public String type;
    public List<String> description;
}
