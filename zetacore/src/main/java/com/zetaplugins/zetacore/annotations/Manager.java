package com.zetaplugins.zetacore.annotations;

import com.zetaplugins.zetacore.services.di.ManagerScope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Manager {
    /**
     * Whether the manager should be eagerly loaded upon registration.<br/>
     * If true, the manager will be instantiated immediately when registered.<br/>
     * If false, the manager will be instantiated lazily when first requested.<br/>
     * Default is false (lazy loading).
     */
    boolean eagerlyLoad() default false;

    /**
     * The scope of the manager instance.<br/>
     * ManagerScope.SINGLETON - A single instance is shared across the application.<br/>
     * ManagerScope.PROTOTYPE - A new instance is created each time it is requested.<br/>
     * Default is ManagerScope.SINGLETON.
     */
    ManagerScope scope() default ManagerScope.SINGLETON;
}
