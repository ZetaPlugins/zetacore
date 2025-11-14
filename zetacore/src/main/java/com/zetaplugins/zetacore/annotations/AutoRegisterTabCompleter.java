package com.zetaplugins.zetacore.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a TabCompleter class for automatic registration.
 * Use the {@link com.zetaplugins.zetacore.services.CommandRegistrar} to register all annotated tab completers.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoRegisterTabCompleter {
    String command();
    String name() default "";
}
