package com.zetaplugins.zetacore.services.messages;

import com.zetaplugins.zetacore.services.localization.LocalizationService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

import java.util.List;

public class AdventureMessenger extends BaseMessageService implements Messenger {
    private final MiniMessage mm = MiniMessage.miniMessage();

    public AdventureMessenger(LocalizationService localizationService) {
        super(localizationService);
    }

    @Override
    public void send(CommandSender player, boolean addPrefix, String path, String fallback, Replaceable<?>... replaceables) {
        String raw = getRawMessage(path, fallback, addPrefix);
        String processed = replacePlaceholdersWithAccentColors("<!i>" + raw, replaceables);
        Component comp = mm.deserialize(processed);
        player.sendMessage(comp);
    }

    @Override
    public void sendRaw(CommandSender player, String rawMessage, Replaceable<?>... replaceables) {
        String processed = replacePlaceholdersWithAccentColors("<!i>" + rawMessage, replaceables);
        Component comp = mm.deserialize(processed);
        player.sendMessage(comp);
    }

    @Override
    public void sendList(CommandSender player, String path, Replaceable<?>... replaceables) {
        List<String> rawList = getRawMessageList(path);
        for (String item : rawList) {
            String processed = replacePlaceholdersWithAccentColors("<!i>" + item, replaceables);
            player.sendMessage(mm.deserialize(processed));
        }
    }
}
