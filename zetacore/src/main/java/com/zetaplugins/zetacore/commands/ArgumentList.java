package com.zetaplugins.zetacore.commands;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a list of command arguments
 */
public class ArgumentList implements Iterable<String> {
    private final String[] args;

    /**
     * Constructor for the ArgumentList class
     * @param args The list of arguments
     */
    public ArgumentList(String[] args) {
        this.args = args;
    }

    /**
     * Get the argument at the specified index
     * @param index The index of the argument
     * @return The argument at the specified index
     */
    public boolean hasArg(int index) {
        return args.length > index;
    }

    /**
     * Get the argument at the specified index
     * @param index The index of the argument
     * @return The argument at the specified index
     */
    public String getArg(int index) {
        if (hasArg(index)) return args[index];
        else return null;
    }

    /**
     * Get the argument at the specified index, with a default value
     * @param index The index of the argument
     * @param defaultValue The default value to return if the argument does not exist
     * @return The argument at the specified index, or the default value
     */
    public String getString(int index, String defaultValue) {
        if (hasArg(index)) return args[index];
        else return defaultValue;
    }

    /**
     * Get the joined string of arguments starting from the specified index with a space seperator
     * @param startIndex The index to start joining from
     * @return The joined string of arguments
     */
    public String getJoinedString(int startIndex) {
        return getJoinedString(startIndex, " ");
    }

    /**
     * Get the joined string of arguments starting from the specified index
     * @param startIndex The index to start joining from
     * @param seperator The seperator to use between arguments
     * @return The joined string of arguments
     */
    public String getJoinedString(int startIndex, String seperator) {
        if (!hasArg(startIndex)) return "";
        StringBuilder joined = new StringBuilder();
        for (int i = startIndex; i < args.length; i++) {
            joined.append(args[i]);
            if (i < args.length - 1) {
                joined.append(seperator);
            }
        }
        return joined.toString();
    }

    /**
     * Get the player at the specified index
     * @param index The index of the argument
     * @param plugin The instance of the plugin
     * @return The player at the specified index
     */
    public Player getPlayer(int index, JavaPlugin plugin) {
        if (!hasArg(index)) return null;
        return plugin.getServer().getPlayer(args[index]);
    }

    /**
     * Get the player at the specified index, with a default value
     * @param index The index of the argument
     * @param defaultPlayer The default player to return if the argument does not exist
     * @param plugin The instance of the plugin
     * @return The player at the specified index, or the default player
     */
    public Player getPlayer(int index, Player defaultPlayer, JavaPlugin plugin) {
        if (!hasArg(index)) return defaultPlayer;
        return plugin.getServer().getPlayer(args[index]);
    }

    /**
     * Get an integer at the specified index
     * @param index The index of the argument
     * @return The integer at the specified index
     * @throws NumberFormatException When the argument is not an integer
     */
    public int getInt(int index) throws NumberFormatException {
        if (!hasArg(index)) throw new NumberFormatException();
        return Integer.parseInt(args[index]);
    }

    /**
     * Get a double at the specified index
     * @param index The index of the argument
     * @return The double at the specified index
     * @throws NumberFormatException When the argument is not a double
     */
    public double getDouble(int index) throws NumberFormatException {
        if (!hasArg(index)) throw new NumberFormatException();
        return Double.parseDouble(args[index]);
    }

    /**
     * Get a long at the specified index
     * @param index The index of the argument
     * @return The long at the specified index
     * @throws NumberFormatException When the argument is not a long
     */
    public long getLong(int index) throws NumberFormatException {
        if (!hasArg(index)) throw new NumberFormatException();
        return Long.parseLong(args[index]);
    }

    /**
     * Get an integer at the specified index, with a default value
     * @param index The index of the argument
     * @param defaultValue The default value to return if the argument is not an integer or does not exist
     * @return The integer at the specified index, or the default value
     */
    public int getInt(int index, int defaultValue) {
        if (!hasArg(index)) return defaultValue;
        try {
            return Integer.parseInt(args[index]);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Get an integer at the specified index, with a default value and bounds
     * @param index The index of the argument
     * @param defaultValue The default value to return if the argument is not an integer or does not exist
     * @param min The minimum value (inclusive)
     * @param max The maximum value (inclusive)
     * @return The integer at the specified index, or the default value, clamped to the specified bounds
     */
    public int getInt(int index, int defaultValue, int min, int max) {
        int val = getInt(index, defaultValue);
        return Math.max(min, Math.min(max, val));
    }

    /**
     * Get a double at the specified index, with a default value
     * @param index The index of the argument
     * @param defaultValue The default value to return if the argument is not a double or does not exist
     * @return The double at the specified index, or the default value
     */
    public double getDouble(int index, double defaultValue) {
        if (!hasArg(index)) return defaultValue;
        try {
            return Double.parseDouble(args[index]);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Get a long at the specified index, with a default value
     * @param index The index of the argument
     * @param defaultValue The default value to return if the argument is not a long or does not exist
     * @return The long at the specified index, or the default value
     */
    public long getLong(int index, long defaultValue) {
        if (!hasArg(index)) return defaultValue;
        try {
            return Long.parseLong(args[index]);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Get a boolean at the specified index
     * @param index The index of the argument
     * @return The boolean at the specified index
     */
    public boolean getBoolean(int index, boolean defaultValue) {
        if (!hasArg(index)) return defaultValue;
        String val = args[index].toLowerCase();
        return switch (val) {
            case "true", "yes", "y", "1" -> true;
            case "false", "no", "n", "0" -> false;
            default -> defaultValue;
        };
    }

    /**
     * Get an enum at the specified index
     * @param index The index of the argument
     * @param enumClass The class of the enum
     * @param <E> The type of the enum
     * @return The enum at the specified index
     */
    public <E extends Enum<E>> E getEnum(int index, Class<E> enumClass) {
        if (!hasArg(index)) return null;
        try {
            return Enum.valueOf(enumClass, args[index].toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Get an enum at the specified index, with a default value
     * @param index The index of the argument
     * @param enumClass The class of the enum
     * @param defaultValue The default value to return if the argument is not a valid enum or does not exist
     * @param <E> The type of the enum
     * @return The enum at the specified index, or the default value
     */
    public <E extends Enum<E>> E getEnum(int index, Class<E> enumClass, E defaultValue) {
        if (!hasArg(index)) return defaultValue;
        try {
            return Enum.valueOf(enumClass, args[index].toUpperCase());
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    /**
     * Get an enum at the specified index, ignoring case
     * @param index The index of the argument
     * @param enumClass The class of the enum
     * @param <E> The type of the enum
     * @return The enum at the specified index
     */
    public <E extends Enum<E>> E getEnumIgnoreCase(int index, Class<E> enumClass, E defaultValue) {
        if (!hasArg(index)) return defaultValue;
        String input = args[index];
        for (E constant : enumClass.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(input)) return constant;
        }
        return defaultValue;
    }

    /**
     * Get the last argument index
     * @return The last argument index or -1 if there are no arguments
     */
    public int getCurrentArgIndex() {
        return args.length - 1;
    }

    /**
     * Get the last argument
     * @return The last argument or null if there are no arguments
     */
    public String getCurrentArg() {
        if (args.length == 0) return null;
        return args[getCurrentArgIndex()];
    }

    /**
     * Get the number of arguments
     * @return The number of arguments
     */
    public int size() {
        return args.length;
    }

    /**
     * Get the arguments
     * @return The arguments
     */
    public String[] getArgs() {
        return args;
    }

    /**
     * Get the arguments as a list
     * @return The arguments as a list
     */
    public List<String> getAllArgs() {
        return new ArrayList<>(List.of(args));
    }

    @Override
    public @NotNull Iterator<String> iterator() {
        return Arrays.asList(args).iterator();
    }

    @Override
    public String toString() {
        return "ArgumentList{" +
                "args=" + Arrays.toString(args) +
                '}';
    }
}
