package com.zetaplugins.pluginTest;

import com.zetaplugins.zetacore.di.annotation.Inject;
import com.zetaplugins.zetacore.event.annotation.EventListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

@EventListener
public class PlayerMoveListener implements Listener {

    @Inject
    CountService countService;

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        event.getPlayer().sendMessage("You moved!");
        countService.incrementCounter(event.getPlayer().getUniqueId());
        event.getPlayer().sendMessage("Count: " + countService.getCounter(event.getPlayer().getUniqueId()));
    }
}
