package com.zetaplugins.pluginTest.config;

import com.zetaplugins.zetacore.config.annotation.ConfigSection;

import java.util.List;

@ConfigSection
public class ItemConfigSection {
    private String name;
    private int id;
    private double price;
    private List<String> lore;

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public double getPrice() {
        return price;
    }

    public List<String> getLore() {
        return lore;
    }

    @Override
    public String toString() {
        return "ItemConfigSection{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", price=" + price +
                ", lore=" + lore +
                '}';
    }
}
