package com.zetaplugins.zetacore.command.exception;

public class CommandSenderMustBePlayerException extends CommandException {
    public CommandSenderMustBePlayerException() {
        super("The command sender must be a player to execute this command.");
    }
}
