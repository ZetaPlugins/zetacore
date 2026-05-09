package com.zetaplugins.zetacore.config.testconfigs;

import com.zetaplugins.zetacore.config.annotation.ConfigSection;

@ConfigSection
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
