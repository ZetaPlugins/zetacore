package com.zetaplugins.zetacore.services;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for handling messages and localization
 */
public class MessageService {
    private final LocalizationService localizationService;

    /**
     * Constructor for MessageService
     * @param localizationService The LocalizationService instance to use for fetching localized messages
     */
    public MessageService(LocalizationService localizationService) {
        this.localizationService = localizationService;
    }

    private static final Map<String, String> colorMap;

    protected LocalizationService getLocalizationService() {
        return localizationService;
    }

    static {
        colorMap = new HashMap<>();
        colorMap.put("&0", "<black>");
        colorMap.put("&1", "<dark_blue>");
        colorMap.put("&2", "<dark_green>");
        colorMap.put("&3", "<dark_aqua>");
        colorMap.put("&4", "<dark_red>");
        colorMap.put("&5", "<dark_purple>");
        colorMap.put("&6", "<gold>");
        colorMap.put("&7", "<gray>");
        colorMap.put("&8", "<dark_gray>");
        colorMap.put("&9", "<blue>");
        colorMap.put("&a", "<green>");
        colorMap.put("&b", "<aqua>");
        colorMap.put("&c", "<red>");
        colorMap.put("&d", "<light_purple>");
        colorMap.put("&e", "<yellow>");
        colorMap.put("&f", "<white>");
        colorMap.put("&k", "<obfuscated>");
        colorMap.put("&l", "<bold>");
        colorMap.put("&m", "<strikethrough>");
        colorMap.put("&n", "<underlined>");
        colorMap.put("&o", "<italic>");
        colorMap.put("&r", "<reset>");
    }

    /**
     * Formats a message with placeholders
     *
     * @param msg The message to format
     * @param replaceables The placeholders to replace
     * @return The formatted message
     */
    public static Component formatMsg(String msg, Replaceable<?>... replaceables) {
        msg = replacePlaceholders(msg, replaceables);

        MiniMessage mm = MiniMessage.miniMessage();
        return mm.deserialize("<!i>" + msg);
    }

    /**
     * Gets and formats a message from the config
     *
     * @param addPrefix Whether to add the prefix to the message
     * @param path The path to the message in the config
     * @param fallback The fallback message
     * @param replaceables The placeholders to replace
     * @return The formatted message
     */
    public Component getAndFormatMsg(boolean addPrefix, String path, String fallback, Replaceable<?>... replaceables) {
        if (path.startsWith("messages.")) path = path.substring("messages.".length());

        MiniMessage mm = MiniMessage.miniMessage();
        String msg = "<!i>" + localizationService.getString(path, fallback);

        String prefix = localizationService.getString("prefix", "&8[<gradient:#00D26A:#00B24F>TimberZ&8]");
        msg = (!prefix.isEmpty() && addPrefix) ? prefix + " " + msg : msg;

        msg = replacePlaceholdersWithAccentColors(msg, replaceables);

        return mm.deserialize(msg);
    }

    public List<Component> getAndFormatMsgList(String path, Replaceable<?>... replaceables) {
        if (path.startsWith("messages.")) path = path.substring("messages.".length());

        MiniMessage mm = MiniMessage.miniMessage();
        List<String> msgList = localizationService.getStringList(path);
        List<Component> components = new ArrayList<>();

        for (String string : msgList) {
            String msg = "<!i>" + string;
            msg = replacePlaceholdersWithAccentColors(msg, replaceables);
            components.add(mm.deserialize(msg));
        }

        return components;
    }

    /**
     * Gets the accent color
     *
     * @return The accent color
     */
    public String getAccentColor() {
        return localizationService.getString("accentColor", "<#00D26A>");
    }

    @NotNull
    public static String replacePlaceholders(String msg, Replaceable<?>... replaceables) {
        StringBuilder msgBuilder = new StringBuilder(msg);

        for (Replaceable<?> replaceable : replaceables) {
            String placeholder = replaceable.placeholder();
            String value = String.valueOf(replaceable.value());
            replaceInBuilder(msgBuilder, placeholder, value);
        }

        colorMap.forEach((key, value) -> replaceInBuilder(msgBuilder, key, value));

        return msgBuilder.toString();
    }

    @NotNull
    public String replacePlaceholdersWithAccentColors(String msg, Replaceable<?>... replaceables) {
        String replacedMsg = replacePlaceholders(msg, replaceables);
        StringBuilder msgBuilder = new StringBuilder(replacedMsg);
        replaceInBuilder(msgBuilder, "%ac%", getAccentColor());
        return msgBuilder.toString();
    }

    public String convertToLegacy(String input) {
        // Replace color codes
        input = input.replace("<black>", "§0");
        input = input.replace("<dark_blue>", "§1");
        input = input.replace("<dark_green>", "§2");
        input = input.replace("<dark_aqua>", "§3");
        input = input.replace("<dark_red>", "§4");
        input = input.replace("<dark_purple>", "§5");
        input = input.replace("<gold>", "§6");
        input = input.replace("<gray>", "§7");
        input = input.replace("<dark_gray>", "§8");
        input = input.replace("<blue>", "§9");
        input = input.replace("<green>", "§a");
        input = input.replace("<aqua>", "§b");
        input = input.replace("<red>", "§c");
        input = input.replace("<light_purple>", "§d");
        input = input.replace("<yellow>", "§e");
        input = input.replace("<white>", "§f");

        // Replace formatting codes
        input = input.replace("<b>", "§l");
        input = input.replace("<i>", "§o");
        input = input.replace("<u>", "§n");
        input = input.replace("<s>", "§m");
        input = input.replace("<obf>", "§k");

        input = input.replace("<bold>", "§l");
        input = input.replace("<italic>", "§o");
        input = input.replace("<underlined>", "§n");
        input = input.replace("<strikethrough>", "§m");
        input = input.replace("<obfuscated>", "§k");

        // Replace reset tags
        input = input.replace("<!b>", "§r");
        input = input.replace("<!i>", "§r");
        input = input.replace("<!u>", "§r");
        input = input.replace("<!s>", "§r");
        input = input.replace("<!obf>", "§r");

        return input;
    }

    protected static void replaceInBuilder(StringBuilder builder, String placeholder, String replacement) {
        int index;
        while ((index = builder.indexOf(placeholder)) != -1) {
            builder.replace(index, index + placeholder.length(), replacement);
        }
    }

    public record Replaceable<T>(String placeholder, T value) {}
}