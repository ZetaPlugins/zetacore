package com.zetaplugins.zetacore.permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class whose static {@link PermissionNode} fields should be registered automatically.
 * <p>
 * Enums and classes implementing {@link PermissionNode} in the plugin's package are discovered without this annotation.
 *
 * <pre>
 * {@literal @}PermissionHolder
 * public final class Permissions {
 *     public static final Permission ADMIN = Permission.of("myplugin.admin");
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PermissionHolder {
}
