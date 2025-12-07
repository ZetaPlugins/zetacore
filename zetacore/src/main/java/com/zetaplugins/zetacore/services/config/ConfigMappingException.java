package com.zetaplugins.zetacore.services.config;

public class ConfigMappingException extends RuntimeException {

    public ConfigMappingException(String message) {
        super(message);
    }

    public ConfigMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
