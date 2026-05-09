package com.zetaplugins.zetacore.command.annotation;

import com.zetaplugins.zetacore.command.registration.AutoCommandRegistrar;

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
public @interface TabCompleterHandler {
    /**
     * The command(s) to register the tab completer for.
     */
    String[] value() default {};
    /**
     * The command(s) to register the tab completer for.
     */
    String[] commands() default {};
}
