package com.zetaplugins.zetacore.di;

/**
 * Thrown when the {@link ServiceRegistry} encounters an error during service creation, injection, or lifecycle management.
 */
public class ServiceException extends RuntimeException {

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
