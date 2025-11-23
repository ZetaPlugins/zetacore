package com.zetaplugins.pluginTest;

import com.zetaplugins.zetacore.annotations.InjectManager;
import com.zetaplugins.zetacore.annotations.InjectPlugin;
import com.zetaplugins.zetacore.annotations.PostManagerConstruct;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CountManager {
    private final Map<Player, Integer> playerCounts;

    @InjectManager
    private GreetingManager greetingManager;

    @InjectPlugin
    private PluginTest plugin;

    public CountManager() {
        playerCounts = new HashMap<>();
    }

    @PostManagerConstruct
    private void postConstruct() {
        System.out.println(greetingManager.getGreeting("CountManager"));
    }

    public void incrementCounter(UUID playerId) {
        Player player = plugin.getServer().getPlayer(playerId);
        if (player != null) {
            playerCounts.put(player, getCounter(playerId) + 1);
        }
    }

    public int getCounter(UUID playerId) {
        Player player = plugin.getServer().getPlayer(playerId);
        return playerCounts.getOrDefault(player, 0);
    }
}
