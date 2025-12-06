package com.zetaplugins.zetacore.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods as PlaceholderAPI placeholders.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Papi {
    /**
     * The identifier for the placeholder.
     * @return The placeholder identifier
     */
    String identifier();
}
