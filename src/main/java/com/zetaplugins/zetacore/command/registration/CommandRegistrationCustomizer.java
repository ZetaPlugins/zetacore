package com.zetaplugins.zetacore.command.registration;

import java.util.List;

/**
 * Customizes automatic command registration in {@link com.zetaplugins.zetacore.ZetaCorePlugin}.
 * <p>
 * Provide implementations as services or beans; all of them are applied. A command is only
 * registered if every customizer allows it.
 *
 * <pre>
 * {@literal @}Service
 * public class CommandToggles implements CommandRegistrationCustomizer {
 *     private final CommandsConfig config;
 *
 *     public CommandToggles(CommandsConfig config) {
 *         this.config = config;
 *     }
 *
 *     {@literal @}Override
 *     public boolean shouldRegister(String commandName) {
 *         return config.isEnabled(commandName);
 *     }
 * }
 * </pre>
 */
public interface CommandRegistrationCustomizer {
    /**
     * Decides whether a command should be registered.
     * @param commandName The command name, as declared on {@link com.zetaplugins.zetacore.command.annotation.Command}.
     * @return False to skip the command. Defaults to true.
     */
    default boolean shouldRegister(String commandName) {
        return true;
    }

    /**
     * Called once after all commands are registered.
     * @param registeredCommands The names of the registered commands, sorted alphabetically.
     */
    default void onCommandsRegistered(List<String> registeredCommands) {
    }
}
