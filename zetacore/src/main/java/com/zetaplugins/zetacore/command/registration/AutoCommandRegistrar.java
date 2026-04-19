package com.zetaplugins.zetacore.command.registration;

import com.zetaplugins.zetacore.command.annotation.Command;
import com.zetaplugins.zetacore.command.annotation.TabCompleterHandler;
import com.zetaplugins.zetacore.di.ServiceRegistry;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;

/**
 * Manages the registration of commands and tab completers for a plugin.
 * Use the {@link Command} annotation to mark command classes for automatic registration.
 * Use the {@link TabCompleterHandler} annotation to mark tab completer classes for automatic registration.
 */
public class AutoCommandRegistrar implements CommandRegistrar {
    private final JavaPlugin plugin;
    private final String packagePrefix;
    private final String commandNamespace;
    private final ServiceRegistry serviceRegistry;

    /**
     * @param plugin The JavaPlugin instance.
     * @param packagePrefix The package prefix to scan for annotated classes.
     */
    public AutoCommandRegistrar(JavaPlugin plugin, String packagePrefix) {
        this.plugin = plugin;
        this.packagePrefix = packagePrefix;
        this.commandNamespace = plugin.getName().toLowerCase();
        this.serviceRegistry = null;
    }

    /**
     * @param plugin The JavaPlugin instance.
     * @param packagePrefix The package prefix to scan for annotated classes.
     * @param commandNamespace The namespace to use for the command. (e.g. "myplugin" for /myplugin:command)
     */
    public AutoCommandRegistrar(JavaPlugin plugin, String packagePrefix, String commandNamespace) {
        this.plugin = plugin;
        this.packagePrefix = packagePrefix;
        this.commandNamespace = commandNamespace;
        this.serviceRegistry = null;
    }

    public AutoCommandRegistrar(JavaPlugin plugin, String packagePrefix, String commandNamespace, ServiceRegistry serviceRegistry) {
        this.plugin = plugin;
        this.packagePrefix = packagePrefix;
        this.commandNamespace = commandNamespace;
        this.serviceRegistry = serviceRegistry;
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
     * Registers all commands annotated with {@link Command} and tab completers annotated with {@link TabCompleterHandler}.
     * @return A list of names of the registered commands.
     */
    public List<String> registerAllCommands() {
        return registerAllCommands(name -> true);
    }

    /**
     * Registers all commands annotated with {@link Command} and tab completers annotated with {@link TabCompleterHandler}.
     * @param commandNameFilter A predicate to filter which command names to register.
     * @return A list of names of the registered commands.
     */
    public List<String> registerAllCommands(Predicate<String> commandNameFilter) {
        Reflections reflections = new Reflections(packagePrefix);
        List<String> registeredCommands = new ArrayList<>();

        Map<String, TabCompleter> tabCompleters = new HashMap<>();
        Set<Class<?>> tabCompleterClasses = reflections.getTypesAnnotatedWith(TabCompleterHandler.class);

        for (Class<?> clazz : tabCompleterClasses) {
            if (TabCompleter.class.isAssignableFrom(clazz)) {
                TabCompleterHandler annotation = clazz.getAnnotation(TabCompleterHandler.class);
                TabCompleter completer = createTabCompleter(clazz);
                if (completer == null) continue;

                injectServices(completer);

                List<String> names = new ArrayList<>();
                try {
                    String[] cmds = annotation.value().length > 0
                            ? annotation.value()
                            : annotation.commands();
                    if (cmds != null && cmds.length > 0) {
                        for (String n : cmds) {
                            if (n != null && !n.isEmpty() && commandNameFilter.test(n)) names.add(n);
                        }
                    } else {
                        throw new NoSuchMethodException();
                    }
                } catch (NoSuchMethodException ignored) {
                    plugin.getLogger().warning("TabCompleterHandler annotation on " + clazz.getSimpleName() +
                            " has no 'value' or 'commands' method");
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to read TabCompleterHandler annotation on "
                            + clazz.getSimpleName(), e);
                }

                for (String name : names) {
                    tabCompleters.put(name, completer);
                }
            }
        }

        Set<Class<?>> commandClasses = reflections.getTypesAnnotatedWith(Command.class);

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
     * Supports both the new `value()` (String[]) and the old `command()` (String) annotation shapes.
     * @param commandClass The command class to register.
     * @param tabCompleters A map of command names to their corresponding tab completers.
     * @param commandNameFilter A predicate to filter which command names to register.
     * @return The list of display names for the registered commands, or an empty list if none registered.
     */
    private List<String> registerCommand(Class<?> commandClass, Map<String, TabCompleter> tabCompleters, Predicate<String> commandNameFilter) {
        List<String> result = new ArrayList<>();

        try {
            Command commandAnnotation = commandClass.getAnnotation(Command.class);
            if (commandAnnotation == null) return result;

            List<RegisterableCommand> commandsToRegister = new ArrayList<>();

            try {
                String[] commands = commandAnnotation.value();
                if (commands != null && commands.length > 0) {
                    for (String n : commands) if (n != null && !n.isEmpty() && commandNameFilter.test(n)) {
                        commandsToRegister.add(RegisterableCommand.fromClass(n, commandClass));
                    }
                } else {
                    plugin.getLogger().warning("Command annotation on " + commandClass.getSimpleName() +
                            " has no 'value' method or it returned an empty array");
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to read 'value' from Command annotation on "
                        + commandClass.getSimpleName(), e);
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

            injectServices(executor);

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

        injectServices(executor);

        command.setExecutor(executor);
        if (tabCompleter != null) {
            injectServices(tabCompleter);
            command.setTabCompleter(tabCompleter);
        } else if (executor instanceof TabCompleter tabComp) {
            injectServices(tabComp);
            command.setTabCompleter(tabComp);
        }
    }

    /**
     * Injects services into the target object using the ServiceRegistry.
     * @param target The target object to inject services into.
     */
    private void injectServices(Object target) {
        if (serviceRegistry != null) serviceRegistry.injectServices(target);
    }

    /**
     * Builder class for constructing an AutoCommandRegistrar instance.
     */
    public static class Builder {
        private JavaPlugin plugin;
        private String packagePrefix;
        private String commandNamespace;
        private ServiceRegistry serviceRegistry;

        public Builder withPlugin(JavaPlugin plugin) {
            this.plugin = plugin;
            return this;
        }

        public Builder withPackagePrefix(String packagePrefix) {
            this.packagePrefix = packagePrefix;
            return this;
        }

        public Builder withCommandNamespace(String commandNamespace) {
            this.commandNamespace = commandNamespace;
            return this;
        }

        public Builder withServiceRegistry(ServiceRegistry serviceRegistry) {
            this.serviceRegistry = serviceRegistry;
            return this;
        }

        public AutoCommandRegistrar build() {
            if (plugin == null) throw new IllegalStateException("Plugin must be set");
            if (packagePrefix == null) throw new IllegalStateException("Package prefix must be set");
            if (commandNamespace == null) commandNamespace = plugin.getName().toLowerCase();
            return new AutoCommandRegistrar(plugin, packagePrefix, commandNamespace, serviceRegistry);
        }
    }
}