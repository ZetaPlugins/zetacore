package com.zetaplugins.pluginTest;

import com.zetaplugins.debug.command.DebugCommandHandler;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;

public final class CommandManager {
    private final PluginTest plugin;

    public CommandManager(PluginTest plugin) {
        this.plugin = plugin;
    }

    /**
     * Registers all commands
     */
    public void registerCommands() {
        registerCommand(
                "testpldebug",
                new DebugCommandHandler(plugin, plugin.getPluginFile(), "testplugin.debug"),
                new DebugCommandHandler(plugin, plugin.getPluginFile(), "testplugin.debug")
        );
    }

    /**
     * Registers a command
     *
     * @param name The name of the command
     * @param executor The executor of the command
     * @param tabCompleter The tab completer of the command
     */
    private void registerCommand(String name, CommandExecutor executor, TabCompleter tabCompleter) {
        PluginCommand command = plugin.getCommand(name);

        if (command != null) {
            command.setExecutor(executor);
            command.setTabCompleter(tabCompleter);
        }
    }
}
