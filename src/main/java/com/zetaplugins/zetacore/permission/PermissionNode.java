package com.zetaplugins.zetacore.permission;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionDefault;

/**
 * Interface representing a permission node.
 */
public interface PermissionNode {
    /**
     * Gets the permission node string.
     * @return The permission node.
     */
    String getValue();

    /**
     * Gets the description of the permission node.
     * @return The description.
     */
    String getDescription();

    /**
     * Gets the default permission setting.
     * @return The default permission.
     */
    PermissionDefault getPermissionDefault();

    /**
     * Checks if a player has this permission.
     *
     * @param player The player to check.
     * @return true if the player has this permission, false otherwise.
     */
    default boolean has(Permissible player) {
        return player.hasPermission(getValue());
    }

    /**
     * Creates a Bukkit permission from this permission node.
     * @return The Bukkit permission.
     */
    default org.bukkit.permissions.Permission toBukkitPermission() {
        return new org.bukkit.permissions.Permission(getValue(), getDescription(), getPermissionDefault());
    }

    /**
     * Registers the permission node with the Bukkit permission manager.
     */
    default void register() {
        if (Bukkit.getPluginManager().getPermission(getValue()) == null) {
            Bukkit.getPluginManager().addPermission(toBukkitPermission());
        }
    }
}
