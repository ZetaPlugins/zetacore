package com.zetaplugins.zetacore.command.exception;

public class CommandUsageException extends CommandException {
    /**
     * @param usage The correct usage of the command
     */
    public CommandUsageException(String usage) {
        super(usage);
    }

    /**
     * @return The correct usage of the command
     */
    public String getUsage() {
        return getMessage();
    }
}
