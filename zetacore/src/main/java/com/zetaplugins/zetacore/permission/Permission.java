package com.zetaplugins.zetacore.permission;

import org.bukkit.permissions.PermissionDefault;

public class Permission implements PermissionNode {
    private final String value;
    private final String description;
    private final PermissionDefault permissionDefault;

    public Permission(String value, PermissionDefault permissionDefault, String description) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Permission value cannot be null or empty");
        }
        this.value = value;
        this.permissionDefault = permissionDefault == null ? PermissionDefault.OP : permissionDefault;
        this.description = description == null ? "" : description;
    }

    public Permission(String value, PermissionDefault permissionDefault) {
        this(value, permissionDefault, null);
    }

    public Permission(String value) {
        this(value, null, null);
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public PermissionDefault getPermissionDefault() {
        return permissionDefault;
    }

    public static Permission of(String value) {
        return new Permission(value);
    }

    public Permission child(String part) {
        return new Permission(value + "." + part, permissionDefault, description);
    }

    public Permission child(String part, PermissionDefault permissionDefault) {
        return new Permission(value + "." + part, permissionDefault, description);
    }

    public Permission child(String part, PermissionDefault permissionDefault, String description) {
        return new Permission(value + "." + part, permissionDefault, description);
    }
}
