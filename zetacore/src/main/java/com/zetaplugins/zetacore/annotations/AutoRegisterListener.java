package com.zetaplugins.zetacore.annotations;

import com.zetaplugins.zetacore.services.events.AutoEventRegistrar;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark event listener classes for automatic registration.
 * Classes annotated with this will be registered by the {@link AutoEventRegistrar}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoRegisterListener {
    String name() default "";
}
