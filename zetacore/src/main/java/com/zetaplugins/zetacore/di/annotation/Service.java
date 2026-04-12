package com.zetaplugins.zetacore.di.annotation;

import com.zetaplugins.zetacore.di.ServiceScope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as a service for dependency injection
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
    /**
     * Whether the service should be eagerly loaded upon registration.<br/>
     * If true, the service will be instantiated immediately when registered.<br/>
     * If false, the service will be instantiated lazily when first requested.<br/>
     * Default is false (lazy loading).
     */
    boolean eagerlyLoad() default false;

    /**
     * The scope of the service instance.<br/>
     * ServiceScope.SINGLETON - A single instance is shared across the application.<br/>
     * ServiceScope.PROTOTYPE - A new instance is created each time it is requested.<br/>
     * Default is ServiceScope.SINGLETON.
     */
    ServiceScope scope() default ServiceScope.SINGLETON;
}
