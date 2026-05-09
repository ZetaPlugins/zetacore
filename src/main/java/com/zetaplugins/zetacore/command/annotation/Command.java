package com.zetaplugins.zetacore.command.annotation;

import com.zetaplugins.zetacore.command.registration.AutoCommandRegistrar;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class for automatic command registration.
 * Use the {@link AutoCommandRegistrar} to register value annotated with this.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    /**
     * The command(s) to register the command for.
     */
    String[] value();
}
