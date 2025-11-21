package com.zetaplugins.zetacore.commands.exceptions;

public class CommandSenderMustBePlayerException extends CommandException {
    public CommandSenderMustBePlayerException() {
        super("The command sender must be a player to execute this command.");
    }
}
