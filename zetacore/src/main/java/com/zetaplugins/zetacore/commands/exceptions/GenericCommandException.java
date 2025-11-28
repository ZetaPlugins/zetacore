package com.zetaplugins.zetacore.commands.exceptions;

import com.zetaplugins.zetacore.commands.CommandExceptionHandler;

/**
 * A generic command exception that can be used for various command errors.
 * This exception holds a handler to process the exception when it occurs.
 * <br/>This <b>must NOT</b> be explicitly registered using {@link com.zetaplugins.zetacore.commands.PluginCommand#registerExceptionHandler(Class, CommandExceptionHandler)},
 */
public class GenericCommandException extends CommandException {
    private final CommandExceptionHandler<GenericCommandException> handler;

    public GenericCommandException(CommandExceptionHandler<GenericCommandException> handler) {
        super("A generic command exception occurred.");
        this.handler = handler;
    }

    public GenericCommandException(String message, CommandExceptionHandler<GenericCommandException> handler) {
        super(message);
        this.handler = handler;
    }

    public CommandExceptionHandler<GenericCommandException> getHandler() {
        return handler;
    }
}
