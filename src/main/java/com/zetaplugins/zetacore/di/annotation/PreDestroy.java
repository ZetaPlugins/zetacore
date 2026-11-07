package com.zetaplugins.zetacore.di.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to be executed when the plugin is disabled.
 * <p>
 * Called by {@link com.zetaplugins.zetacore.di.ServiceRegistry#shutdown()} on singleton services, beans,
 * registered instances and objects created with {@link com.zetaplugins.zetacore.di.ServiceRegistry#create(Class)},
 * in reverse creation order. The method must have no parameters.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PreDestroy {
}
