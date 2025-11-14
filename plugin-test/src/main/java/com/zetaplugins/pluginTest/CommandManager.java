package com.zetaplugins.pluginTest;

import com.zetaplugins.zetacore.debug.command.DebugCommandHandler;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;

import java.util.HashMap;
import java.util.Map;

public final class CommandManager {
    private final PluginTest plugin;

    public CommandManager(PluginTest plugin) {
        this.plugin = plugin;
    }

    /**
     * Registers all commands
     */
    public void registerCommands() {
        Map<String, String> configs = new HashMap<>();
        configs.put("config.yml", plugin.getConfig().saveToString());
        registerCommand(
                "testpldebug",
                new DebugCommandHandler("MODRINTHID", plugin, plugin.getPluginFile(), "testplugin.debug", configs, plugin.getMessenger()),
                new DebugCommandHandler("MODRINTHID", plugin, plugin.getPluginFile(), "testplugin.debug", configs, plugin.getMessenger())
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
