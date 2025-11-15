package com.zetaplugins.zetacore.annotations;

import com.zetaplugins.zetacore.services.commands.AutoCommandRegistrar;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a TabCompleter class for automatic registration.
 * Use the {@link AutoCommandRegistrar} to register all annotated tab completers.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoRegisterTabCompleter {
    /**
     * The command to register.
     * @deprecated Use {@link #commands()} instead for multiple commands.
     */
    String command() default "";

    /**
     * The commands to register.
     */
    String[] commands() default {};

    /**
     * The name of the tab completer. NOT the actual command string, but a speaking name for the completer.
     */
    String name() default "";
}
