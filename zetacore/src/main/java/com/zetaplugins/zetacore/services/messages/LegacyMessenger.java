package com.zetaplugins.zetacore.services.messages;

import com.zetaplugins.zetacore.annotations.Manager;
import com.zetaplugins.zetacore.services.localization.LocalizationService;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

import java.util.List;

@Manager
public class LegacyMessenger extends BaseMessageService implements Messenger {
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacySection();

    public LegacyMessenger(LocalizationService localizationService) {
        super(localizationService);
    }

    @Override
    public void send(CommandSender player, boolean addPrefix, String path, String fallback, Replaceable<?>... replaceables) {
        String raw = getRawMessage(path, fallback, addPrefix);
        String processed = replacePlaceholdersWithAccentColors("<!i>" + raw, replaceables);
        // Convert to Component then to legacy-coded string
        String legacy = legacySerializer.serialize(mm.deserialize(processed));
        player.sendMessage(legacy);
    }

    @Override
    public void sendRaw(CommandSender player, String rawMessage, Replaceable<?>... replaceables) {
        String processed = replacePlaceholdersWithAccentColors("<!i>" + rawMessage);
        String legacy = legacySerializer.serialize(mm.deserialize(processed));
        player.sendMessage(legacy);
    }

    @Override
    public void sendList(CommandSender player, String path, Replaceable<?>... replaceables) {
        List<String> rawList = getRawMessageList(path);
        for (String item : rawList) {
            String processed = replacePlaceholdersWithAccentColors("<!i>" + item, replaceables);
            player.sendMessage(legacySerializer.serialize(mm.deserialize(processed)));
        }
    }
}