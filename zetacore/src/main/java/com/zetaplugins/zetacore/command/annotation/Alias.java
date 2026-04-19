package com.zetaplugins.zetacore.command.annotation;

import com.zetaplugins.zetacore.command.registration.AutoCommandRegistrar;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to set the command aliases for a command handler.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Alias {
    /**
     * The alias(es) to register for this command.
     */
    String[] value();
}
