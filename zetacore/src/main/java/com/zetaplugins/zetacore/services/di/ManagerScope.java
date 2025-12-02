package com.zetaplugins.zetacore.services.di;

/**
 * Enum representing the scope of a manager instance.
 */
public enum ManagerScope {
    /**
     * Singleton scope - only one instance of the manager is created and shared.
     */
    SINGLETON,
    /**
     * Prototype scope - a new instance of the manager is created each time it is requested.
     */
    PROTOTYPE
}
