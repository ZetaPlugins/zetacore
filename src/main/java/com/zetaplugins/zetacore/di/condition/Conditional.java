package com.zetaplugins.zetacore.di.condition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Activates the annotated component only if all given {@link Condition}s match.
 * <p>
 * Supported on services, {@link com.zetaplugins.zetacore.di.annotation.Configuration} classes,
 * {@link com.zetaplugins.zetacore.di.annotation.Bean} methods, commands, tab completers and event listeners.
 * Inactive services are ignored when discovering implementations, so several services can implement the same
 * interface as long as only one of them is active.
 *
 * <pre>
 * {@literal @}Service
 * {@literal @}Conditional(EconomyEnabled.class)
 * public class BuiltinEconomyService implements EconomyService { ... }
 * </pre>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Conditional {
    /**
     * The conditions that must all match.
     */
    Class<? extends Condition>[] value();
}
