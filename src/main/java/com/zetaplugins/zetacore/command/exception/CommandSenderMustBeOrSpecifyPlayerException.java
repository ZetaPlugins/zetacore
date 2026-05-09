package com.zetaplugins.zetacore.command.exception;

public class CommandSenderMustBeOrSpecifyPlayerException extends CommandException {
    public CommandSenderMustBeOrSpecifyPlayerException() {
        super("The command sender must be a player or specify a player to execute this command.");
    }
}
