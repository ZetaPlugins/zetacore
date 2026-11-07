package com.zetaplugins.zetacore.di.condition;

/**
 * A custom condition used with {@link Conditional}.
 * <p>
 * Conditions are created by the {@link com.zetaplugins.zetacore.di.ServiceRegistry}, so they can receive services,
 * config classes, the plugin and its logger through their constructor. Each condition is evaluated at most once.
 *
 * <pre>
 * public class EconomyEnabled implements Condition {
 *     private final EconomyConfig config;
 *
 *     public EconomyEnabled(EconomyConfig config) {
 *         this.config = config;
 *     }
 *
 *     {@literal @}Override
 *     public boolean matches() {
 *         return config.isEnabled();
 *     }
 * }
 * </pre>
 */
@FunctionalInterface
public interface Condition {
    /**
     * Checks whether the condition is met.
     * @return True if the annotated component should be active.
     */
    boolean matches();
}
