package com.zetaplugins.zetacore.services.commands;

import com.zetaplugins.zetacore.annotations.AutoRegisterCommand;
import com.zetaplugins.zetacore.annotations.AutoRegisterTabCompleter;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;

/**
 * Manages the registration of commands and tab completers for a plugin.
 * Use the {@link AutoRegisterCommand} annotation to mark command classes for automatic registration.
 * Use the {@link AutoRegisterTabCompleter} annotation to mark tab completer classes for automatic registration.
 */
public class AutoCommandRegistrar implements CommandRegistrar {
    private final JavaPlugin plugin;
    private final String packagePrefix;

    /**
     * @param plugin The JavaPlugin instance.
     * @param packagePrefix The package prefix to scan for annotated classes.
     */
    public AutoCommandRegistrar(JavaPlugin plugin, String packagePrefix) {
        this.plugin = plugin;
        this.packagePrefix = packagePrefix;
    }

    /**
     * Registers all commands annotated with {@link AutoRegisterCommand} and tab completers annotated with {@link AutoRegisterTabCompleter}.
     * @return A list of names of the registered commands.
     */
    public List<String> registerAllCommands() {
        Reflections reflections = new Reflections(packagePrefix);
        List<String> registeredCommands = new ArrayList<>();

        Map<String, TabCompleter> tabCompleters = new HashMap<>();
        Set<Class<?>> tabCompleterClasses = reflections.getTypesAnnotatedWith(AutoRegisterTabCompleter.class);

        for (Class<?> clazz : tabCompleterClasses) {
            if (TabCompleter.class.isAssignableFrom(clazz)) {
                AutoRegisterTabCompleter annotation = clazz.getAnnotation(AutoRegisterTabCompleter.class);
                TabCompleter completer = createTabCompleter(clazz);
                if (completer == null) continue;

                // support old command and new commands annotation methods
                List<String> names = new ArrayList<>();
                try {
                    Method commandsMethod = annotation.annotationType().getMethod("commands");
                    String[] arr = (String[]) commandsMethod.invoke(annotation);
                    if (arr != null && arr.length > 0) {
                        for (String n : arr) if (n != null && !n.isEmpty()) names.add(n);
                    } else {
                        throw new NoSuchMethodException();
                    }
                } catch (NoSuchMethodException ignored) {
                    try {
                        Method commandMethod = annotation.annotationType().getMethod("command");
                        String n = (String) commandMethod.invoke(annotation);
                        if (n != null && !n.isEmpty()) names.add(n);
                    } catch (NoSuchMethodException ignored2) {
                        plugin.getLogger().warning("AutoRegisterTabCompleter annotation on " + clazz.getSimpleName() +
                                " has no 'commands' or 'command' method");
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, "Failed to read AutoRegisterTabCompleter annotation on "
                                + clazz.getSimpleName(), e);
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to read AutoRegisterTabCompleter annotation on "
                            + clazz.getSimpleName(), e);
                }

                for (String name : names) {
                    tabCompleters.put(name, completer);
                }
            }
        }

        Set<Class<?>> commandClasses = reflections.getTypesAnnotatedWith(AutoRegisterCommand.class);

        for (Class<?> clazz : commandClasses) {
            if (CommandExecutor.class.isAssignableFrom(clazz)) {
                List<String> names = registerCommand(clazz, tabCompleters);
                if (names != null && !names.isEmpty()) registeredCommands.addAll(names);
            }
        }

        registeredCommands.sort(String::compareTo);
        return registeredCommands;
    }

    /**
     * Creates an instance of a TabCompleter from the given class.
     * @param completerClass The class of the TabCompleter to create.
     * @return The created TabCompleter instance, or null if creation failed.
     */
    private TabCompleter createTabCompleter(Class<?> completerClass) {
        try {
            try {
                Constructor<?> constructor = completerClass.getConstructor(plugin.getClass());
                return (TabCompleter) constructor.newInstance(plugin);
            } catch (NoSuchMethodException e) {
                Constructor<?> constructor = completerClass.getConstructor();
                return (TabCompleter) constructor.newInstance();
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to create tab completer: " + completerClass.getSimpleName(), e);
            return null;
        }
    }

    /**
     * Registers a command class for all names declared on the annotation.
     * Supports both the new `commands()` (String[]) and the old `command()` (String) annotation shapes.
     * @param commandClass The command class to register.
     * @param tabCompleters A map of command names to their corresponding tab completers.
     * @return The list of display names for the registered commands, or an empty list if none registered.
     */
    private List<String> registerCommand(Class<?> commandClass, Map<String, TabCompleter> tabCompleters) {
        List<String> result = new ArrayList<>();

        try {
            AutoRegisterCommand annotation = commandClass.getAnnotation(AutoRegisterCommand.class);
            if (annotation == null) return result;

            List<String> commandNames = new ArrayList<>();
            try {
                Method commandsMethod = annotation.annotationType().getMethod("commands");
                String[] names = (String[]) commandsMethod.invoke(annotation);
                if (names != null && names.length > 0) {
                    for (String n : names) if (n != null && !n.isEmpty()) commandNames.add(n);
                } else {
                    throw new NoSuchMethodException();
                }
            } catch (NoSuchMethodException ignored) {
                try {
                    Method commandMethod = annotation.annotationType().getMethod("command");
                    String name = (String) commandMethod.invoke(annotation);
                    if (name != null && !name.isEmpty()) commandNames.add(name);
                } catch (NoSuchMethodException ignored2) {
                    plugin.getLogger().warning("AutoRegisterCommand annotation on " + commandClass.getSimpleName() +
                            " has no 'commands' or 'command' method");
                }
            }

            if (commandNames.isEmpty()) return result;

            CommandExecutor executor;
            try {
                Constructor<?> constructor = commandClass.getConstructor(plugin.getClass());
                executor = (CommandExecutor) constructor.newInstance(plugin);
            } catch (NoSuchMethodException e) {
                try {
                    Constructor<?> constructor = commandClass.getConstructor();
                    executor = (CommandExecutor) constructor.newInstance();
                } catch (NoSuchMethodException ex) {
                    plugin.getLogger().severe("No suitable constructor found for command class: " + commandClass.getSimpleName());
                    return null;
                }
            }

            String displayName = annotation.name().isEmpty() ? commandClass.getSimpleName() : annotation.name();

            for (String commandName : commandNames) {
                PluginCommand command = plugin.getCommand(commandName);
                if (command != null) {
                    command.setExecutor(executor);

                    if (executor instanceof TabCompleter) {
                        command.setTabCompleter((TabCompleter) executor);
                    } else if (tabCompleters.containsKey(commandName)) {
                        command.setTabCompleter(tabCompleters.get(commandName));
                    }

                    result.add(displayName);
                } else {
                    plugin.getLogger().warning("Command '" + commandName + "' not found in plugin.yml");
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to register command: " + commandClass.getSimpleName(), e);
        }
        return result;
    }

    /**
     * Manually registers a command
     * @param name The name of the command
     * @param executor The executor of the command
     * @param tabCompleter The tab completer of the command
     */
    public void registerCommand(String name, CommandExecutor executor, TabCompleter tabCompleter) {
        PluginCommand command = plugin.getCommand(name);

        if (command != null) {
            command.setExecutor(executor);
            if (tabCompleter != null) command.setTabCompleter(tabCompleter);
        }
    }
}