package com.zetaplugins.zetacore.services.messages;

import org.bukkit.command.CommandSender;

public interface Messenger {
    /**
     * Send a message by path with placeholders
     * @param player target
     * @param addPrefix whether to add configured prefix
     * @param path message path in localization
     * @param fallback fallback message if path missing
     * @param replaceables placeholders
     */
    void send(CommandSender player, boolean addPrefix, String path, String fallback, Replaceable<?>... replaceables);

    /**
     * Send a single ad-hoc message string (raw message already formatted if desired)
     * @param player target
     * @param rawMessage raw message string
     * @param replaceables placeholders
     */
    void sendRaw(CommandSender player, String rawMessage, Replaceable<?>... replaceables);

    /**
     * Send a list of messages by path with placeholders
     * @param player target
     * @param path message path in localization
     * @param replaceables placeholders
     */
    void sendList(CommandSender player, String path, Replaceable<?>... replaceables);
}