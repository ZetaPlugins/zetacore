package com.zetaplugins.zetacore.services.config.testconfigs;

import com.zetaplugins.zetacore.annotations.NestedConfig;

import java.util.List;

@NestedConfig
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
