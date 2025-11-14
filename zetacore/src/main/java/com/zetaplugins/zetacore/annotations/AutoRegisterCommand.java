package com.zetaplugins.zetacore.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class for automatic command registration.
 * Use the {@link com.zetaplugins.zetacore.services.CommandRegistrar} to register commands annotated with this.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoRegisterCommand {
    String command();
    String name() default "";
}
