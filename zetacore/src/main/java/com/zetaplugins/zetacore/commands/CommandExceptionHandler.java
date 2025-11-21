package com.zetaplugins.zetacore.commands;

import com.zetaplugins.zetacore.commands.exceptions.CommandException;

/**
 * Functional interface for handling CommandExceptions.
 * @param <E> The type of CommandException to handle
 */
@FunctionalInterface
public interface CommandExceptionHandler<E extends CommandException> {
    /**
     * Handle a CommandException.
     * @param context The context of the command execution
     * @param exception The exception thrown
     * @return true if the exception was handled and onCommand should return true, false otherwise
     */
    boolean handle(CommandContext context, E exception);
}
