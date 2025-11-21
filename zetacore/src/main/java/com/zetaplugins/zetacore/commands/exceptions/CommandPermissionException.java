package com.zetaplugins.zetacore.commands.exceptions;

public class CommandPermissionException extends CommandException {
    /**
     * @param permission The permission that is required to execute the command
     */
    public CommandPermissionException(String permission) {
        super(permission);
    }

    /**
     * @return The permission that is required to execute the command
     */
    public String getPermission() {
        return getMessage();
    }
}
