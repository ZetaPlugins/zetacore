package com.zetaplugins.zetacore.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

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

    /**
     * Get the sender as a player.
     * @return The sender if it is a player, otherwise empty
     */
    public Optional<Player> getPlayerSender() {
        return sender instanceof Player player ? Optional.of(player) : Optional.empty();
    }

    /**
     * Get the online player whose exact name is given at the specified argument index.
     * @param index The index of the argument
     * @return The player, or empty if the argument is missing or no such player is online
     */
    public Optional<Player> getPlayer(int index) {
        if (!args.hasArg(index)) return Optional.empty();
        return Optional.ofNullable(sender.getServer().getPlayerExact(args.getArg(index)));
    }

    /**
     * Get the player given at the specified argument index, or the sender if the argument is missing.
     * <p>
     * If the argument is present but no such player is online, the result is empty; it does not fall back to the sender.
     * <pre>
     * Player target = ctx.getPlayerOrSender(0).orElseThrow(CommandSenderMustBeOrSpecifyPlayerException::new);
     * </pre>
     * @param index The index of the argument
     * @return The player, the sender if it is a player and the argument is missing, or empty
     */
    public Optional<Player> getPlayerOrSender(int index) {
        return args.hasArg(index) ? getPlayer(index) : getPlayerSender();
    }
}
