package com.zetaplugins.zetacore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Context for a command execution, containing the sender, command, label, and arguments.
 */
public class CommandContext {
    private final CommandSender sender;
    private final Command command;
    private final String label;
    private final ArgumentList args;

    public CommandContext(CommandSender sender, Command command, String label, ArgumentList args) {
        this.sender = sender;
        this.command = command;
        this.label = label;
        this.args = args;
    }

    /**
     * Get the sender of the command.
     * @return The CommandSender
     */
    public CommandSender getSender() {
        return sender;
    }

    /**
     * Get the command that was executed.
     * @return The Command
     */
    public Command getCommand() {
        return command;
    }

    /**
     * Get the label (alias) used to execute the command.
     * @return The command label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Get the arguments passed to the command.
     * @return The ArgumentList
     */
    public ArgumentList getArgs() {
        return args;
    }
}
