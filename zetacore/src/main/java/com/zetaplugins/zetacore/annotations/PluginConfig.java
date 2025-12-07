package com.zetaplugins.zetacore.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as containing configuration properties.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginConfig {
    /**
     * The name of the configuration file. (e.g., "config.yml")
     * @return the configuration file name
     */
    String value();
}
