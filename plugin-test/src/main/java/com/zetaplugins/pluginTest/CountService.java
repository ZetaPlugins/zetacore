package com.zetaplugins.pluginTest;

import com.zetaplugins.zetacore.di.annotation.InjectService;
import com.zetaplugins.zetacore.di.annotation.InjectPlugin;
import com.zetaplugins.zetacore.di.annotation.Service;
import com.zetaplugins.zetacore.di.annotation.PostConstruct;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class CountService {
    private final Map<Player, Integer> playerCounts;

    @InjectService
    private GreetingService greetingService;

    @InjectPlugin
    private PluginTest plugin;

    public CountService() {
        playerCounts = new HashMap<>();
        System.out.println("CountManager constructor called: " + this.hashCode());
    }

    @PostConstruct
    private void postConstruct() {
        System.out.println(greetingService.getGreeting("CountManager"));
    }

    public void incrementCounter(UUID playerId) {
        Player player = plugin.getServer().getPlayer(playerId);
        if (player != null) {
            playerCounts.put(player, getCounter(playerId) + 1);
        }
    }

    public int getCounter(UUID playerId) {
        System.out.println(greetingService.getGreeting("CountManager.getCounter"));
        Player player = plugin.getServer().getPlayer(playerId);
        return playerCounts.getOrDefault(player, 0);
    }
}
