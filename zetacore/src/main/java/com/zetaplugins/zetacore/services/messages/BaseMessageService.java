package com.zetaplugins.zetacore.services.messages;

import com.zetaplugins.zetacore.services.localization.LocalizationService;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class BaseMessageService {
    protected final LocalizationService localizationService;
    protected final Map<String, String> colorMap;

    public BaseMessageService(LocalizationService localizationService) {
        this.localizationService = localizationService;

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

    protected LocalizationService getLocalizationService() {
        return localizationService;
    }

    public String getAccentColor() {
        return localizationService.getString("accentColor", "<#00D26A>");
    }

    @NotNull
    public String replacePlaceholders(String msg, Replaceable<?>... replaceables) {
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

    protected void replaceInBuilder(StringBuilder builder, String placeholder, String replacement) {
        int index;
        while ((index = builder.indexOf(placeholder)) != -1) {
            builder.replace(index, index + placeholder.length(), replacement);
        }
    }

    public List<String> getRawMessageList(String path) {
        if (path.startsWith("messages.")) path = path.substring("messages.".length());
        return localizationService.getStringList(path);
    }

    public String getRawMessage(String path, String fallback, boolean addPrefix) {
        if (path.startsWith("messages.")) path = path.substring("messages.".length());
        String msg = localizationService.getString(path, fallback);
        String prefix = localizationService.getString("prefix", "&8[<gradient:#00D26A:#00B24F>TimberZ&8]");
        return (!prefix.isEmpty() && addPrefix) ? prefix + " " + msg : msg;
    }
}