package com.zetaplugins.zetacore.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Optional settings for a {@link com.zetaplugins.zetacore.ZetaCorePlugin} main class.
 * Every value has a sensible default, so the annotation is only needed to override them.
 *
 * <pre>
 * {@literal @}ZetaPlugin(packagePrefix = "com.example.myplugin")
 * public final class MyPlugin extends ZetaCorePlugin {}
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ZetaPlugin {
    /**
     * The package to scan for services, commands and listeners.<br/>
     * Defaults to the package of the main class.
     */
    String packagePrefix() default "";

    /**
     * Whether managed classes must be annotated with {@link com.zetaplugins.zetacore.di.annotation.Service}.<br/>
     * Default is true.
     */
    boolean requireServiceAnnotation() default true;

    /**
     * The namespace for registered commands (e.g. "myplugin" for /myplugin:command).<br/>
     * Defaults to the lowercase plugin name.
     */
    String commandNamespace() default "";

    /**
     * The bStats plugin id. If set, metrics are started automatically and
     * {@link com.zetaplugins.zetacore.integration.bstats.MetricsCustomizer}s can add charts.<br/>
     * Default is 0 (disabled).
     */
    int bstatsId() default 0;
}
