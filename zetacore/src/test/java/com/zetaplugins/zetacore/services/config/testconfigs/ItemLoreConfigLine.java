package com.zetaplugins.zetacore.services.config.testconfigs;

import com.zetaplugins.zetacore.annotations.NestedConfig;

@NestedConfig
public class ItemLoreConfigLine {
    private String loreLine;
    private int lineNumber;

    public String getLoreLine() {
        return loreLine;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String toString() {
        return "ItemLoreConfigSection{" +
                "loreLine='" + loreLine + '\'' +
                ", lineNumber=" + lineNumber +
                '}';
    }
}
