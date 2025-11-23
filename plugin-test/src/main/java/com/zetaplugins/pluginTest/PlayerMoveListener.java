package com.zetaplugins.pluginTest;

import com.zetaplugins.zetacore.annotations.AutoRegisterListener;
import com.zetaplugins.zetacore.annotations.InjectManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

@AutoRegisterListener
public class PlayerMoveListener implements Listener {

    @InjectManager
    CountManager countManager;

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        event.getPlayer().sendMessage("You moved!");
        countManager.incrementCounter(event.getPlayer().getUniqueId());
        event.getPlayer().sendMessage("Count: " + countManager.getCounter(event.getPlayer().getUniqueId()));
    }
}
