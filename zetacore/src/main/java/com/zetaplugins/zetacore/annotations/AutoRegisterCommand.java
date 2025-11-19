package com.zetaplugins.zetacore.annotations;

import com.zetaplugins.zetacore.services.commands.AutoCommandRegistrar;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class for automatic command registration.
 * Use the {@link AutoCommandRegistrar} to register commands annotated with this.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoRegisterCommand {
    /**
     * The command to register.
     */
    String command() default "";

    /**
     * The commands to register.
     */
    String[] commands() default {};

    /**
     * The name of the command handler. NOT the actual command string, but a speaking name for the handler.
     */
    String name() default "";

    /**
     * Aliases for the command.
     */
    String[] aliases() default {};

    /**
     * Description of the command.
     */
    String description() default "__UNSET__";

    /**
     * Usage information for the command.
     */
    String usage() default "__UNSET__";

    /**
     * Permission required to execute the command.
     */
    String permission() default "__UNSET__";
}
