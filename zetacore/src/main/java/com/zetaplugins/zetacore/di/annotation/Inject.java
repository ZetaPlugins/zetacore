package com.zetaplugins.zetacore.di.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a field should be automatically injected by the {@link com.zetaplugins.zetacore.di.ServiceRegistry}.
 * <p>
 * If the field type is {@link org.bukkit.plugin.java.JavaPlugin} or a subclass, the plugin instance is injected.
 * Otherwise, the field is resolved as a service from the registry.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Inject {
}
