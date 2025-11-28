package com.zetaplugins.zetacore.commands;

import com.zetaplugins.zetacore.commands.exceptions.CommandException;
import com.zetaplugins.zetacore.commands.exceptions.CommandPermissionException;
import com.zetaplugins.zetacore.commands.exceptions.CommandUsageException;
import com.zetaplugins.zetacore.commands.exceptions.GenericCommandException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a custom command for a plugin
 * @param <T> The type of the plugin
 */
public abstract class PluginCommand<T extends JavaPlugin> implements CommandExecutor, TabCompleter {
    private final T plugin;
    private final Map<Class<? extends CommandException>, CommandExceptionHandler<? extends CommandException>> exceptionHandlers = new HashMap<>();

    /**
     * Constructor for the PluginCommand class
     *
     * @param plugin The instance of the plugin
     */
    public PluginCommand(T plugin) {
        this.plugin = plugin;

        registerExceptionHandler(
                GenericCommandException.class,
                (ctx, e) -> e.getHandler().handle(ctx, e)
        );
    }

    protected T getPlugin() {
        return plugin;
    }

    /**
     * Execute the command
     *
     * @param sender The sender of the command
     * @param command The command that was executed
     * @param label The label of the command (The alias used)
     * @param args The arguments of the command
     * @return Whether the command was executed successfully
     * @throws CommandPermissionException If the sender does not have permission to execute the command
     * @throws CommandUsageException If the command was used incorrectly
     */
    public abstract boolean execute(CommandSender sender, Command command, String label, ArgumentList args) throws CommandException ;

    /**
     * The Tabcompletion method for the command
     *
     * @param sender The sender of the command
     * @param command The command that is being tab completed
     * @param args The current arguments of the command
     * @return A list of possible completions
     */
    public abstract List<String> tabComplete(CommandSender sender, Command command, ArgumentList args);

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        var argumentList = new ArgumentList(args);

        try {
            return execute(commandSender, command, label, argumentList);
        } catch (CommandException e) {
            return handleCommandException(commandSender, command, label, argumentList, e);
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return tabComplete(sender, command, new ArgumentList(args));
    }

    /**
     * Register an exception handler for a specific CommandException subclass
     * @param exceptionClass The class of the exception to handle
     * @param handler The handler to execute when the exception is thrown
     * @param <E> The type of CommandException
     */
    public final <E extends CommandException> void registerExceptionHandler(Class<E> exceptionClass, CommandExceptionHandler<E> handler) {
        exceptionHandlers.put(exceptionClass, handler);
    }

    /**
     * Handle a CommandException by finding the appropriate registered handler
     * @param sender The sender of the command
     * @param command The command that was executed
     * @param label The label of the command (The alias used)
     * @param args The arguments of the command
     * @param e The exception that was thrown
     * @return Whether the exception was handled
     */
    private boolean handleCommandException(CommandSender sender, Command command, String label, ArgumentList args, CommandException e) {
        Class<?> clazz = e.getClass();
        while (clazz != null && CommandException.class.isAssignableFrom(clazz)) {
            var handler = getHandler(clazz);
            if (handler != null) return handler.handle(new CommandContext(sender, command, label, args), e);
            clazz = clazz.getSuperclass();
        }
        return false;
    }

    /**
     * Get the registered handler for a specific CommandException subclass
     * @param clazz The class of the exception
     * @return The registered handler, or null if none exists
     * @param <E> The type of CommandException
     */
    @SuppressWarnings("unchecked")
    private <E extends CommandException> CommandExceptionHandler<E> getHandler(Class<?> clazz) {
        return (CommandExceptionHandler<E>) exceptionHandlers.get(clazz);
    }

    /**
     * Get a list of options that start with the input
     *
     * @param options The list of options
     * @param input The input to check against
     * @return A list of options that start with the input
     */
    protected final List<String> getDisplayOptions(List<String> options, String input) {
        return CommandUtils.getDisplayOptions(options, input);
    }

    /**
     * Get a list of player options
     *
     * @param input The input to check against
     * @return A list of player options
     */
    protected final List<String> getPlayerOptions(String input) {
        return CommandUtils.getPlayerOptions(getPlugin(), input);
    }
}
