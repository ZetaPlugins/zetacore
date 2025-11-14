package com.zetaplugins.zetacore.services.commands;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;

import java.util.List;

public interface CommandRegistrar {
    /**
     * Registers all commands.
     * @return A list of names of the registered commands.
     */
    List<String> registerAllCommands();

    /**
     * Registers a command.
     * @param name The name of the command.
     * @param executor The executor of the command.
     */
    default void registerCommand(String name, CommandExecutor executor) {
        if (executor instanceof TabCompleter) registerCommand(name, executor, (TabCompleter) executor);
        else registerCommand(name, executor, null);
    }

    /**
     * Registers a command.
     * @param name The name of the command.
     * @param executor The executor of the command.
     * @param tabCompleter The tab completer of the command.
     */
    void registerCommand(String name, CommandExecutor executor, TabCompleter tabCompleter);
}
