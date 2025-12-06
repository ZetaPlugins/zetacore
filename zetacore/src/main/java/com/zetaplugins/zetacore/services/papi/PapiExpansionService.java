package com.zetaplugins.zetacore.services.papi;

import com.zetaplugins.zetacore.annotations.Papi;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PapiExpansionService {
    private String identifier;
    private String author;
    private String version;
    private Map<String, PlaceholderFunction> placeholders;
    private Logger logger;

    private String playerNotFoundMessage = "PlayerNotFound";
    private String playerNotOnlineMessage = "PlayerNotOnline";

    /**
     * Creates a new empty PapiExpansionService.
     */
    public PapiExpansionService() {
        this.placeholders = new HashMap<>();
    }

    /**
     * Creates a new PapiExpansionService with the given identifier, author, and version.
     * @param identifier The identifier of the expansion
     * @param author The author of the expansion
     * @param version The version of the expansion
     */
    public PapiExpansionService(String identifier, String author, String version, Logger logger) {
        this.identifier = identifier;
        this.author = author;
        this.version = version;
        this.placeholders = new HashMap<>();
        this.logger = logger;
    }

    /**
     * Creates a new PapiExpansionService using the given plugin's information.
     * @param plugin The plugin to use for the expansion information
     */
    public PapiExpansionService(JavaPlugin plugin) {
        this.identifier = plugin.getName().toLowerCase();
        this.author = plugin.getDescription().getAuthors().isEmpty() ? "Unknown" : plugin.getDescription().getAuthors().get(0);
        this.version = plugin.getDescription().getVersion();
        this.placeholders = new HashMap<>();
        this.logger = plugin.getLogger();
    }

    public String getIdentifier() {
        return identifier;
    }

    public PapiExpansionService setIdentifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public PapiExpansionService setAuthor(String author) {
        this.author = author;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public PapiExpansionService setVersion(String version) {
        this.version = version;
        return this;
    }

    public Logger getLogger() {
        return logger;
    }

    public PapiExpansionService setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    public String getPlayerNotFoundMessage() {
        return playerNotFoundMessage;
    }

    /**
     * Sets the message to return when a player is not found.
     * @param playerNotFoundMessage The message to set (default: "PlayerNotFound")
     * @return The PapiExpansionService instance
     */
    public PapiExpansionService setPlayerNotFoundMessage(@Nullable String playerNotFoundMessage) {
        this.playerNotFoundMessage = playerNotFoundMessage;
        return this;
    }

    public String getPlayerNotOnlineMessage() {
        return playerNotOnlineMessage;
    }

    /**
     * Sets the message to return when a player is not online.
     * @param playerNotOnlineMessage The message to set (default: "PlayerNotOnline")
     * @return The PapiExpansionService instance
     */
    public PapiExpansionService setPlayerNotOnlineMessage(@Nullable String playerNotOnlineMessage) {
        this.playerNotOnlineMessage = playerNotOnlineMessage;
        return this;
    }

    public Map<String, PlaceholderFunction> getPlaceholders() {
        return placeholders;
    }

    /**
     * Sets the placeholders for the expansion.
     * @param placeholders The map of placeholders
     * @return The PapiExpansionService instance
     */
    public PapiExpansionService setPlaceholders(Map<String, PlaceholderFunction> placeholders) {
        this.placeholders = placeholders;
        return this;
    }

    /**
     * Adds a placeholder to the expansion.
     * @param key The key of the placeholder
     * @param function The function to execute for the placeholder
     * @return The PapiExpansionService instance
     */
    public PapiExpansionService addPlaceholder(String key, PlaceholderFunction function) {
        this.placeholders.put(key, function);
        return this;
    }

    /**
     * Adds annotated placeholders from the given object.
     * Methods annotated with {@link Papi} will be registered as placeholders.
     * @param obj The object containing annotated methods
     * @return The PapiExpansionService instance
     */
    public PapiExpansionService addAnnotatedPlaceholders(Object obj) {
        Class<?> clazz = obj.getClass();

        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Papi.class)) continue;

            Papi annotation = method.getAnnotation(Papi.class);
            String key = annotation.identifier();

            method.setAccessible(true);

            this.addPlaceholder(key, (player, identifier) -> {
                try {
                    // Must be of signature: () -> String or (OfflinePlayer) -> String or (Player) -> String
                    if (method.getParameterCount() == 0) return (String) method.invoke(obj);
                    else if (method.getParameterCount() == 1) {
                        Class<?> paramType = method.getParameterTypes()[0];
                        if (paramType == OfflinePlayer.class) {
                            return (String) method.invoke(obj, player);
                        } else if (paramType == Player.class) {
                            if (!player.isOnline()) return playerNotOnlineMessage;
                            return (String) method.invoke(obj, player.getPlayer());
                        } else {
                            throw new IllegalArgumentException("Unsupported parameter type for Papi method: " + paramType.getName());
                        }
                    } else {
                        throw new IllegalArgumentException("Papi method must have 0 or 1 parameters.");
                    }
                } catch (Exception e) {
                    if (logger != null) {
                        logger.log(Level.SEVERE, "Error executing PAPI placeholder method for identifier '" + key + "': " + e.getMessage(), e);
                    } else e.printStackTrace();
                    return "ErrorExecutingPlaceholder";
                }
            });
        }
        return this;
    }

    /**
     * Checks if PlaceholderAPI is installed
     * @return True if PlaceholderAPI is installed, false otherwise
     */
    public static boolean hasPapi() {
        return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    /**
     * Registers the expansion with PlaceholderAPI
     * @return True if the expansion was registered successfully, false otherwise
     */
    public boolean register() throws IllegalStateException {
        if (identifier == null || author == null || version == null || placeholders == null) {
            throw new IllegalStateException("Identifier, author, version, and placeholders must be set before registering the expansion.");
        }
        if (!hasPapi()) return false;

        var expansion = new me.clip.placeholderapi.expansion.PlaceholderExpansion() {
            @Override
            public @NotNull String getIdentifier() {
                return identifier;
            }

            @Override
            public @NotNull String getAuthor() {
                return author;
            }

            @Override
            public @NotNull String getVersion() {
                return version;
            }

            @Override
            public String onRequest(OfflinePlayer player, @NotNull String identifier) {
                if (player == null) return playerNotFoundMessage;

                PlaceholderFunction function = placeholders.get(identifier);
                if (function != null) return function.apply(player, identifier);
                return null;
            }
        };

        if (!expansion.canRegister()) return false;
        expansion.register();
        return true;
    }
}
