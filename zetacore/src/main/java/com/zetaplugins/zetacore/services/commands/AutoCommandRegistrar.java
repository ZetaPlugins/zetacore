package com.zetaplugins.zetacore.services.commands;

import com.zetaplugins.zetacore.annotations.AutoRegisterCommand;
import com.zetaplugins.zetacore.annotations.AutoRegisterTabCompleter;
import com.zetaplugins.zetacore.services.di.ManagerRegistry;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;

/**
 * Manages the registration of commands and tab completers for a plugin.
 * Use the {@link AutoRegisterCommand} annotation to mark command classes for automatic registration.
 * Use the {@link AutoRegisterTabCompleter} annotation to mark tab completer classes for automatic registration.
 */
public class AutoCommandRegistrar implements CommandRegistrar {
    private final JavaPlugin plugin;
    private final String packagePrefix;
    private final String commandNamespace;
    private final ManagerRegistry managerRegistry;

    /**
     * @param plugin The JavaPlugin instance.
     * @param packagePrefix The package prefix to scan for annotated classes.
     */
    public AutoCommandRegistrar(JavaPlugin plugin, String packagePrefix) {
        this.plugin = plugin;
        this.packagePrefix = packagePrefix;
        this.commandNamespace = plugin.getName().toLowerCase();
        this.managerRegistry = null;
    }

    /**
     * @param plugin The JavaPlugin instance.
     * @param packagePrefix The package prefix to scan for annotated classes.
     * @param commandNamespace The namespace to use for the commands. (e.g. "myplugin" for /myplugin:command)
     */
    public AutoCommandRegistrar(JavaPlugin plugin, String packagePrefix, String commandNamespace) {
        this.plugin = plugin;
        this.packagePrefix = packagePrefix;
        this.commandNamespace = commandNamespace;
        this.managerRegistry = null;
    }

    public AutoCommandRegistrar(JavaPlugin plugin, String packagePrefix, String commandNamespace, ManagerRegistry managerRegistry) {
        this.plugin = plugin;
        this.packagePrefix = packagePrefix;
        this.commandNamespace = commandNamespace;
        this.managerRegistry = managerRegistry;
    }

    /**
     * Gets the command map
     * @return The command map
     */
    private CommandMap getCommandMap() {
        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            return (CommandMap) field.get(Bukkit.getServer());
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to get command map: " + e.getMessage());
            return null;
        }
    }

    /**
     * Registers all commands annotated with {@link AutoRegisterCommand} and tab completers annotated with {@link AutoRegisterTabCompleter}.
     * @return A list of names of the registered commands.
     */
    public List<String> registerAllCommands() {
        return registerAllCommands(name -> true);
    }

    /**
     * Registers all commands annotated with {@link AutoRegisterCommand} and tab completers annotated with {@link AutoRegisterTabCompleter}.
     * @param commandNameFilter A predicate to filter which command names to register.
     * @return A list of names of the registered commands.
     */
    public List<String> registerAllCommands(Predicate<String> commandNameFilter) {
        Reflections reflections = new Reflections(packagePrefix);
        List<String> registeredCommands = new ArrayList<>();

        Map<String, TabCompleter> tabCompleters = new HashMap<>();
        Set<Class<?>> tabCompleterClasses = reflections.getTypesAnnotatedWith(AutoRegisterTabCompleter.class);

        for (Class<?> clazz : tabCompleterClasses) {
            if (TabCompleter.class.isAssignableFrom(clazz)) {
                AutoRegisterTabCompleter annotation = clazz.getAnnotation(AutoRegisterTabCompleter.class);
                TabCompleter completer = createTabCompleter(clazz);
                if (completer == null) continue;

                injectManagers(completer);

                List<String> names = new ArrayList<>();
                try {
                    Method commandsMethod = annotation.annotationType().getMethod("commands");
                    String[] arr = (String[]) commandsMethod.invoke(annotation);
                    if (arr != null && arr.length > 0) {
                        for (String n : arr) {
                            if (n != null && !n.isEmpty() && commandNameFilter.test(n)) names.add(n);
                        }
                    } else {
                        throw new NoSuchMethodException();
                    }
                } catch (NoSuchMethodException ignored) {
                    try {
                        Method commandMethod = annotation.annotationType().getMethod("command");
                        String n = (String) commandMethod.invoke(annotation);
                        if (n != null && !n.isEmpty() && commandNameFilter.test(n)) {
                            names.add(n);
                        } else {
                            throw new NoSuchMethodException();
                        }
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
                List<String> names = registerCommand(clazz, tabCompleters, commandNameFilter);
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
     * @param commandNameFilter A predicate to filter which command names to register.
     * @return The list of display names for the registered commands, or an empty list if none registered.
     */
    private List<String> registerCommand(Class<?> commandClass, Map<String, TabCompleter> tabCompleters, Predicate<String> commandNameFilter) {
        List<String> result = new ArrayList<>();

        try {
            AutoRegisterCommand annotation = commandClass.getAnnotation(AutoRegisterCommand.class);
            if (annotation == null) return result;

            List<RegisterableCommand> commandsToRegister = new ArrayList<>();

            try {
                Method commandsMethod = annotation.annotationType().getMethod("commands");
                String[] names = (String[]) commandsMethod.invoke(annotation);

                if (names != null && names.length > 0) {
                    for (String n : names) if (n != null && !n.isEmpty() && commandNameFilter.test(n)) {
                        commandsToRegister.add(RegisterableCommand.fromAnnotation(n, annotation));
                    }
                } else {
                    throw new NoSuchMethodException();
                }
            } catch (NoSuchMethodException ignored) {
                try {
                    Method commandMethod = annotation.annotationType().getMethod("command");
                    String name = (String) commandMethod.invoke(annotation);
                    if (name != null && !name.isEmpty() && commandNameFilter.test(name)) {
                        commandsToRegister.add(RegisterableCommand.fromAnnotation(name, annotation));
                    } else {
                        throw new NoSuchMethodException();
                    }
                } catch (NoSuchMethodException ignored2) {
                    plugin.getLogger().warning("AutoRegisterCommand annotation on " + commandClass.getSimpleName() +
                            " has no 'commands' or 'command' method");
                }
            }

            if (commandsToRegister.isEmpty()) return result;

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

            injectManagers(executor);

            for (var registerableCommand : commandsToRegister) {
                TabCompleter tabCompleter =
                        (executor instanceof TabCompleter)
                                ? (TabCompleter) executor
                                : (tabCompleters.getOrDefault(registerableCommand.name(), null));

                boolean success = registerableCommand.register(
                        plugin,
                        commandNamespace,
                        getCommandMap(),
                        executor,
                        tabCompleter
                );

                if (success) result.add(registerableCommand.name());
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to register command: " + commandClass.getSimpleName(), e);
        }
        return result;
    }

    /**
     * Manually registers a command defined in the plugin.yml
     * @param name The name of the command
     * @param executor The executor of the command
     * @param tabCompleter The tab completer of the command
     */
    public void registerCommand(String name, CommandExecutor executor, TabCompleter tabCompleter) {
        PluginCommand command = plugin.getCommand(name);

        if (executor == null) {
            plugin.getLogger().warning("Cannot register command '" + name + "' with null executor.");
            return;
        }

        if (command == null) {
            plugin.getLogger().warning("Command '" + name + "' not found in plugin.yml.");
            return;
        }

        injectManagers(executor);

        command.setExecutor(executor);
        if (tabCompleter != null) {
            injectManagers(tabCompleter);
            command.setTabCompleter(tabCompleter);
        } else if (executor instanceof TabCompleter tabComp) {
            injectManagers(tabComp);
            command.setTabCompleter(tabComp);
        }
    }

    /**
     * Injects managers into the target object using the ManagerRegistry.
     * @param target The target object to inject managers into.
     */
    private void injectManagers(Object target) {
        if (managerRegistry != null) managerRegistry.injectManagers(target);
    }

    /**
     * Builder class for constructing an AutoCommandRegistrar instance.
     */
    public static class Builder {
        private JavaPlugin plugin;
        private String packagePrefix;
        private String commandNamespace;
        private ManagerRegistry managerRegistry;

        public Builder setPlugin(JavaPlugin plugin) {
            this.plugin = plugin;
            return this;
        }

        public Builder setPackagePrefix(String packagePrefix) {
            this.packagePrefix = packagePrefix;
            return this;
        }

        public Builder setCommandNamespace(String commandNamespace) {
            this.commandNamespace = commandNamespace;
            return this;
        }

        public Builder setManagerRegistry(ManagerRegistry managerRegistry) {
            this.managerRegistry = managerRegistry;
            return this;
        }

        public AutoCommandRegistrar build() {
            if (plugin == null) throw new IllegalStateException("Plugin must be set");
            if (packagePrefix == null) throw new IllegalStateException("Package prefix must be set");
            if (commandNamespace == null) commandNamespace = plugin.getName().toLowerCase();
            return new AutoCommandRegistrar(plugin, packagePrefix, commandNamespace, managerRegistry);
        }
    }
}