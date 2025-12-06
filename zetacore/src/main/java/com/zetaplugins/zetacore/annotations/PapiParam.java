package com.zetaplugins.zetacore.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface PapiParam {
    /**
     * The name of the placeholder parameter as used in the template, e.g. {index}.
     */
    String value();
}
