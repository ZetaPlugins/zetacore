package com.zetaplugins.zetacore.commands;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for command-related operations.
 */
public final class CommandUtils {
    private CommandUtils() {}

    /**
     * Gets a list of options that start with the input
     * @param options The available options
     * @param input The input
     * @return A list of options that start with the input
     */
    public static List<String> getDisplayOptions(Collection<String> options, String input) {
        return options.stream()
                .filter(option -> startsWithIgnoreCase(option, input))
                .collect(Collectors.toList());
    }

    /**
     * Get a list of player options that start with the input
     *
     * @param plugin The instance of the plugin
     * @return A list of player options
     */
    public static List<String> getPlayerOptions(JavaPlugin plugin, String input) {
        return getDisplayOptions(
                plugin.getServer().getOnlinePlayers()
                        .parallelStream()
                        .map(Player::getName)
                        .collect(Collectors.toList()),
                input
        );
    }

    /**
     * Checks if a string starts with another string (case-insensitive)
     * @param str The string
     * @param prefix The prefix
     * @return True if the string starts with the prefix, false otherwise
     */
    private static boolean startsWithIgnoreCase(String str, String prefix) {
        return str.regionMatches(true, 0, prefix, 0, prefix.length());
    }
}
