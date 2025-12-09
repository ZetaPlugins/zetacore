package com.zetaplugins.zetacore.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method parameter as a PlaceholderAPI parameter.
 * This annotation indicates that the parameter's value should be used
 * to replace a corresponding placeholder in a PlaceholderAPI template.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface PapiParam {
    /**
     * The name of the placeholder parameter as used in the template, e.g. {index}.
     */
    String value();
}
