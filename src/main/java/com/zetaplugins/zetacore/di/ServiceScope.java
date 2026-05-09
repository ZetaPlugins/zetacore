package com.zetaplugins.zetacore.di;

/**
 * Enum representing the scope of a service instance.
 */
public enum ServiceScope {
    /**
     * Singleton scope - only one instance of the service is created and shared.
     */
    SINGLETON,
    /**
     * Prototype scope - a new instance of the service is created each time it is requested.
     */
    PROTOTYPE
}
