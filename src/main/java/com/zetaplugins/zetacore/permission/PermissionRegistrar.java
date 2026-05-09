package com.zetaplugins.zetacore.permission;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility for collecting and registering permission constants from classes.
 *
 * <p>Typical usage:</p>
 * <pre>{@code
 * PermissionRegistrar.registerAll(MyPermissions.class, AdminPermissions.class);
 * }</pre>
 */
public final class PermissionRegistrar {
    private PermissionRegistrar() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Collects all static {@link PermissionNode} fields from a class.
     *
     * @param holderClass Class that contains static permission constants.
     * @return Collected permission nodes (deduplicated by value, insertion order preserved).
     */
    public static List<PermissionNode> collectFromClass(Class<?> holderClass) {
        return collectFromClasses(List.of(holderClass));
    }

    /**
     * Collects all static {@link PermissionNode} fields from multiple classes.
     *
     * @param holderClasses Classes that contain static permission constants.
     * @return Collected permission nodes (deduplicated by value, insertion order preserved).
     */
    public static List<PermissionNode> collectFromClasses(Class<?>... holderClasses) {
        return collectFromClasses(Arrays.asList(holderClasses));
    }

    /**
     * Collects all static {@link PermissionNode} fields from multiple classes.
     *
     * @param holderClasses Classes that contain static permission constants.
     * @return Collected permission nodes (deduplicated by value, insertion order preserved).
     */
    public static List<PermissionNode> collectFromClasses(Collection<Class<?>> holderClasses) {
        if (holderClasses == null || holderClasses.isEmpty()) return List.of();

        Map<String, PermissionNode> byValue = new LinkedHashMap<>();

        for (Class<?> holderClass : holderClasses) {
            if (holderClass == null) continue;

            for (Field field : holderClass.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) continue;
                if (!PermissionNode.class.isAssignableFrom(field.getType())) continue;

                field.setAccessible(true);
                try {
                    PermissionNode node = (PermissionNode) field.get(null);
                    if (node == null) continue;
                    byValue.putIfAbsent(node.getValue(), node);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(
                            "Could not access permission field '" + field.getName() + "' in " + holderClass.getName(),
                            e
                    );
                }
            }
        }

        return new ArrayList<>(byValue.values());
    }

    /**
     * Registers all static {@link PermissionNode} fields from a class.
     *
     * @param holderClass Class that contains static permission constants.
     * @return Number of permissions newly registered.
     */
    public static int registerAll(Class<?> holderClass) {
        return registerAll(collectFromClass(holderClass));
    }

    /**
     * Registers all static {@link PermissionNode} fields from multiple classes.
     *
     * @param holderClasses Classes that contain static permission constants.
     * @return Number of permissions newly registered.
     */
    public static int registerAll(Class<?>... holderClasses) {
        return registerAll(collectFromClasses(holderClasses));
    }

    /**
     * Registers permission nodes in Bukkit's permission manager.
     *
     * @param permissions Permissions to register.
     * @return Number of permissions newly registered.
     */
    public static int registerAll(Collection<? extends PermissionNode> permissions) {
        if (permissions == null || permissions.isEmpty()) return 0;

        int registered = 0;
        for (PermissionNode node : permissions) {
            if (node == null) continue;

            Permission existing = Bukkit.getPluginManager().getPermission(node.getValue());
            if (existing != null) continue;

            Bukkit.getPluginManager().addPermission(node.toBukkitPermission());
            registered++;
        }

        return registered;
    }
}
