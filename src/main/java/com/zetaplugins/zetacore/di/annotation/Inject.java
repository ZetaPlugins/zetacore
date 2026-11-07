package com.zetaplugins.zetacore.di.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a field or constructor should be automatically injected by the {@link com.zetaplugins.zetacore.di.ServiceRegistry}.
 * <p>
 * <b>Fields:</b> If the field type is {@link org.bukkit.plugin.java.JavaPlugin} or a subclass, the plugin instance is injected.
 * Otherwise, the field is resolved as a service from the registry.
 * <p>
 * <b>Constructors:</b> Marks the constructor the registry should use to create the instance. Each parameter is resolved
 * the same way as an injected field. At most one constructor per class may be annotated. If a class has no annotated
 * constructor, the registry falls back to a constructor taking only the plugin, then a no-argument constructor, and finally
 * to the single declared constructor if there is exactly one.
 *
 * <pre>
 * {@literal @}Service
 * public class StatsService {
 *     private final Storage storage;
 *
 *     {@literal @}Inject
 *     public StatsService(Storage storage) {
 *         this.storage = storage;
 *     }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.CONSTRUCTOR})
public @interface Inject {
}
