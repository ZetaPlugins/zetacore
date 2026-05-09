package com.zetaplugins.zetacore.command.exception;

import com.zetaplugins.zetacore.permission.Permission;

public class CommandPermissionException extends CommandException {
    /**
     * @param permission The permission that is required to execute the command
     */
    public CommandPermissionException(String permission) {
        super(permission);
    }

    /**
     * @param permission The permission that is required to execute the command
     */
    public CommandPermissionException(Permission permission) {
        super(permission.getValue());
    }

    /**
     * @return The permission that is required to execute the command
     */
    public String getPermission() {
        return getMessage();
    }
}
