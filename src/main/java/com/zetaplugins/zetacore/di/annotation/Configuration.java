package com.zetaplugins.zetacore.di.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class that declares {@link Bean} methods.
 * <p>
 * Configuration classes in the plugin's package are discovered automatically. They are created
 * like services (constructor and field injection), but only once one of their beans is needed.
 *
 * <pre>
 * {@literal @}Configuration
 * public class StorageBeans {
 *     {@literal @}Bean
 *     public Storage storage(StorageConfig config, JavaPlugin plugin) {
 *         return config.type.equals("mysql") ? new MySQLStorage(plugin, config) : new SQLiteStorage(plugin);
 *     }
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Configuration {
}
