package com.zetaplugins.zetacore.services.config.testconfigs;

import com.zetaplugins.zetacore.annotations.NestedConfig;

import java.util.List;

@NestedConfig
public class AdvancedConfigItem {
    public int id;
    public String type;
    public List<String> description;
}
