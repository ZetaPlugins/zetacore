package com.zetaplugins.zetacore.di.condition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Activates the annotated component only if all given plugins are installed.
 * <p>
 * Add the plugins to {@code softdepend} in your plugin.yml so they are loaded before yours.
 * Supported on the same elements as {@link Conditional}.
 *
 * <pre>
 * {@literal @}Service
 * {@literal @}ConditionalOnPlugin("Vault")
 * public class VaultEconomyService implements EconomyService { ... }
 * </pre>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConditionalOnPlugin {
    /**
     * The names of the plugins that must be installed.
     */
    String[] value();
}
