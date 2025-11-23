package com.zetaplugins.zetacore.services.commands;

import com.zetaplugins.zetacore.annotations.AutoRegisterCommand;
import com.zetaplugins.zetacore.services.di.ManagerRegistry;
import org.bukkit.command.*;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * A command that can be registered
 * @param name The name of the command
 * @param aliases The aliases of the command
 * @param description The description of the command
 * @param usage The usage of the command
 * @param permission The permission of the command
 */
public record RegisterableCommand(
        String name,
        List<String> aliases,
        String description,
        String usage,
        String permission
) {
    private final static String UNSET = "__UNSET__";

    private @NotNull Command getCommand(CommandExecutor executor, TabCompleter tabCompleter) {
        return new BukkitCommand(name) {
            @Override
            public boolean execute(@NotNull CommandSender commandSender, @NotNull String commandLabel, @NotNull String @NotNull [] args) {
                return executor.onCommand(commandSender, this, commandLabel, args);
            }

            @Override
            public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args) throws IllegalArgumentException {
                List<String> completions = tabCompleter.onTabComplete(sender, this, alias, args);
                return completions == null ? List.of() : completions;
            }
        };
    }

    private void setPluginCommandAttributes(Command command) {
        String commandName = command.getName();
        if (!aliases.isEmpty()) command.setAliases(aliases);
        if (description != null && !description.equals(UNSET)) command.setDescription(description.replaceAll("%command%", commandName));
        if (usage != null && !usage.equals(UNSET)) command.setUsage(usage.replaceAll("%command%", commandName));
        if (permission != null && !permission.equals(UNSET)) command.setPermission(permission.replaceAll("%command%", commandName));
    }

    /**
     * Registers the command
     * @param plugin The plugin instance
     * @param commandNamespace The command namespace
     * @param commandMap The command map
     * @param executor The command executor
     * @param tabCompleter The tab completer
     * @return True if the command was registered successfully, false otherwise
     */
    public boolean register(
            JavaPlugin plugin,
            String commandNamespace,
            CommandMap commandMap,
            CommandExecutor executor,
            TabCompleter tabCompleter
    ) {
        PluginCommand command = plugin.getCommand(name);

        if (command != null) {
            command.setExecutor(executor);
            if (tabCompleter != null) command.setTabCompleter(tabCompleter);
            setPluginCommandAttributes(command);
            return true;
        }

        if (commandMap == null) {
            plugin.getLogger().severe("CommandMap is null, cannot register command: " + name);
            return false;
        }

        try {
            Command newCommand = getCommand(executor, tabCompleter);
            setPluginCommandAttributes(newCommand);

            boolean sucess = commandMap.register(
                    name,
                    commandNamespace,
                    newCommand
            );

            if (!sucess) {
                plugin.getLogger().severe("Failed to manually register command: " + name);
                return false;
            }

            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Exception while registering command: " + name);
            return false;
        }
    }

    /**
     * Creates a RegisterableCommand from an AutoRegisterCommand annotation
     * @param name The name of the command
     * @param annotation The AutoRegisterCommand annotation
     * @return The RegisterableCommand
     */
    public static RegisterableCommand fromAnnotation(
            String name,
            AutoRegisterCommand annotation
    ) {
        String[] aliases;
        String description;
        String usage;
        String permission;

        try {
            Method aliasesMethod = annotation.annotationType().getMethod("aliases");
            aliases = (String[]) aliasesMethod.invoke(annotation);
        } catch (Exception e) {
            aliases = new String[0];
        }

        try {
            Method descriptionMethod = annotation.annotationType().getMethod("description");
            description = (String) descriptionMethod.invoke(annotation);
        } catch (Exception e) {
            description = null;
        }

        try {
            Method usageMethod = annotation.annotationType().getMethod("usage");
            usage = (String) usageMethod.invoke(annotation);
        } catch (Exception e) {
            usage = null;
        }

        try {
            Method permissionMethod = annotation.annotationType().getMethod("permission");
            permission = (String) permissionMethod.invoke(annotation);
        } catch (Exception e) {
            permission = null;
        }

        return new RegisterableCommand(
                name,
                Arrays.asList(aliases),
                description,
                usage,
                permission
        );
    }
}
