package com.zetaplugins.zetacore.services.papi;

import com.zetaplugins.zetacore.annotations.Papi;
import com.zetaplugins.zetacore.annotations.PapiParam;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class PapiExpansionService {
    private String identifier;
    private String author;
    private String version;
    private Map<String, PlaceholderFunction> placeholders;
    private List<PatternEntry> patternPlaceholders = new ArrayList<>();
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
            String placeholderIdentifier = annotation.identifier();

            method.setAccessible(true);

            if (!placeholderIdentifier.contains("{")) {
                PlaceholderFunction pf = createPlaceholderFunctionForMethod(obj, method, placeholderIdentifier);
                this.addPlaceholder(placeholderIdentifier, pf);
                continue;
            }

            patternPlaceholders.add(createPatternEntryForMethod(obj, method, placeholderIdentifier));
        }
        return this;
    }

    private PlaceholderFunction createPlaceholderFunctionForMethod(Object obj, Method method, String placeholderIdentifier) {
        return (player, identifier) -> {
            try {
                // Must be of signature: () -> String or (OfflinePlayer) -> String or (Player) -> String
                if (method.getParameterCount() == 0) return Objects.toString(method.invoke(obj), null);
                else if (method.getParameterCount() == 1) {
                    Class<?> paramType = method.getParameterTypes()[0];
                    if (paramType == OfflinePlayer.class) {
                        return (String) method.invoke(obj, player);
                    } else if (paramType == Player.class) {
                        if (!player.isOnline()) return playerNotOnlineMessage;
                        return (String) method.invoke(obj, player.getPlayer());
                    } else if (paramType == String.class) {
                        return (String) method.invoke(obj, player.getName());
                    } else {
                        throw new IllegalArgumentException("Unsupported parameter type for Papi method: " + paramType.getName());
                    }
                } else {
                    throw new IllegalArgumentException("Papi method must have 0 or 1 parameters.");
                }
            } catch (Exception e) {
                if (logger != null) logger.log(Level.SEVERE, "Error executing PAPI placeholder method for identifier '" + placeholderIdentifier + "': " + e.getMessage(), e);
                else e.printStackTrace();
                return null;
            }
        };
    }

    private PatternEntry createPatternEntryForMethod(Object obj, Method method, String placeholderIdentifier) {
        Pair<Pattern, String[]> compiled = compilePattern(placeholderIdentifier);
        Pattern regex = compiled.key();
        String[] paramNames = compiled.value();

        PatternPlaceholderFunction pFunc = (player, identifier1, args) -> {
            try {
                List<Object> invokeArgs = new ArrayList<>();
                Parameter[] params = method.getParameters();

                for (Parameter param : params) {
                    Class<?> t = param.getType();

                    PapiParam anno = param.getAnnotation(PapiParam.class);
                    if (anno != null) {
                        String name = anno.value();
                        int idx = -1;
                        for (int k = 0; k < paramNames.length; k++) {
                            if (paramNames[k].equals(name)) {
                                idx = k;
                                break;
                            }
                        }
                        if (idx == -1) {
                            throw new IllegalArgumentException("Method " + method.getName() + " expects Papi param '" + name + "' which is not present in pattern " + placeholderIdentifier);
                        }
                        String raw = args[idx];
                        invokeArgs.add(convertStringToType(raw, t, method, placeholderIdentifier));
                        continue;
                    }

                    // No annotation -> treat as player-related
                    if (t == OfflinePlayer.class) {
                        invokeArgs.add(player);
                    } else if (t == Player.class) {
                        if (!player.isOnline()) return playerNotOnlineMessage;
                        invokeArgs.add(player.getPlayer());
                    } else if (t == String.class) {
                        // interpret as player name
                        invokeArgs.add(player.getName());
                    } else {
                        throw new IllegalArgumentException("Unsupported unannotated parameter type: " + t.getName() + " in method " + method.getName());
                    }
                }

                Object result = method.invoke(obj, invokeArgs.toArray());
                return result == null ? null : String.valueOf(result);
            } catch (Exception e) {
                if (logger != null) logger.log(Level.SEVERE, "Error executing patterned PAPI method for '" + placeholderIdentifier + "': " + e.getMessage(), e);
                else e.printStackTrace();
                return null;
            }
        };

        return new PatternEntry(regex, paramNames, pFunc);
    }

    private static Object convertStringToType(String raw, Class<?> targetType, Method method, String placeholderIdentifier) {
        if (targetType == String.class) return raw;
        if (targetType == int.class || targetType == Integer.class) {
            try { return Integer.parseInt(raw); }
            catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Failed to parse int param '" + raw + "' for method " + method.getName() + " (pattern: " + placeholderIdentifier + ")", ex);
            }
        }
        if (targetType == long.class || targetType == Long.class) {
            try { return Long.parseLong(raw); }
            catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Failed to parse long param '" + raw + "' for method " + method.getName() + " (pattern: " + placeholderIdentifier + ")", ex);
            }
        }
        if (targetType == double.class || targetType == Double.class) {
            try { return Double.parseDouble(raw); }
            catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Failed to parse double param '" + raw + "' for method " + method.getName() + " (pattern: " + placeholderIdentifier + ")", ex);
            }
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(raw);
        }
        if (targetType == float.class || targetType == Float.class) {
            try { return Float.parseFloat(raw); }
            catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Failed to parse float param '" + raw + "' for method " + method.getName() + " (pattern: " + placeholderIdentifier + ")", ex);
            }
        }

        throw new IllegalArgumentException("Unsupported parameter type for @PapiParam: " + targetType.getName() + " in method " + method.getName());
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

                PlaceholderFunction exact = placeholders.get(identifier);
                if (exact != null) return exact.apply(player, identifier);

                for (PatternEntry pe : patternPlaceholders) {
                    var m = pe.getPattern().matcher(identifier);
                    if (!m.matches()) continue;
                    String[] args = new String[pe.getParamNames().length];
                    for (int k = 0; k < args.length; k++) args[k] = m.group(k + 1);
                    return pe.getFunction().apply(player, identifier, args);
                }

                return null;
            }
        };

        if (!expansion.canRegister()) return false;
        expansion.register();
        return true;
    }

    private record Pair<K, V>(K key, V value) {}

    /**
     * Compiles a pattern string with placeholders into a regex pattern and parameter names.
     * @param template The pattern string with placeholders (e.g., "count_plus_{number}_{id}")
     * @return A Pair containing the compiled regex Pattern and an array of parameter names
     */
    private static Pair<java.util.regex.Pattern, String[]> compilePattern(String template) {
        java.util.List<String> names = new ArrayList<>();
        StringBuilder regex = new StringBuilder();
        regex.append("^");
        int i = 0;
        while (i < template.length()) {
            char c = template.charAt(i);
            if (c == '{') {
                int j = template.indexOf('}', i);
                if (j == -1) throw new IllegalArgumentException("Unclosed { in pattern: " + template);
                String name = template.substring(i + 1, j);
                names.add(name);
                regex.append("([^_]+)"); // capture group for placeholder
                i = j + 1;
            } else {
                // escape regex metachars
                if ("\\.^$|?*+[](){}".indexOf(c) >= 0) regex.append("\\");
                regex.append(c);
                i++;
            }
        }
        regex.append("$");
        return new Pair<>(java.util.regex.Pattern.compile(regex.toString()), names.toArray(new String[0]));
    }

    private static class PatternEntry {
        private final java.util.regex.Pattern pattern;
        private final String[] paramNames;
        private final PatternPlaceholderFunction function;

        PatternEntry(java.util.regex.Pattern pattern, String[] paramNames, PatternPlaceholderFunction function) {
            this.pattern = pattern;
            this.paramNames = paramNames;
            this.function = function;
        }

        public Pattern getPattern() {
            return pattern;
        }

        public String[] getParamNames() {
            return paramNames;
        }

        public PatternPlaceholderFunction getFunction() {
            return function;
        }
    }
}
