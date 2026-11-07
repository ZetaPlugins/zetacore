package com.zetaplugins.zetacore.di.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a factory method in a {@link Configuration} class whose return value is registered as a singleton.
 * <p>
 * The bean is registered under the method's declared return type, so returning an interface makes the
 * bean injectable by that interface. Method parameters are resolved like constructor parameters.
 * The returned object gets field injection and {@link PostConstruct} unless it is already managed by the registry.
 * <p>
 * Each type can only be provided once: a second {@link Bean} for the same type, a {@link Service} class of
 * that type, a {@link Service} that binds to it, or an instance registered under it are reported as errors.
 * A bean takes precedence over automatic implementation discovery for interfaces and abstract classes.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean {
    /**
     * Whether the bean is created on startup instead of when it is first needed.<br/>
     * Default is false (lazy loading).
     */
    boolean eagerlyLoad() default false;
}
