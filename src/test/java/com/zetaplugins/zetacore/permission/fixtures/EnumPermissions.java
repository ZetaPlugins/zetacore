package com.zetaplugins.zetacore.permission.fixtures;

import com.zetaplugins.zetacore.permission.PermissionNode;
import org.bukkit.permissions.PermissionDefault;

public enum EnumPermissions implements PermissionNode {
    FLY("test.fly"),
    HEAL("test.heal");

    private final String value;

    EnumPermissions(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public PermissionDefault getPermissionDefault() {
        return PermissionDefault.OP;
    }
}
