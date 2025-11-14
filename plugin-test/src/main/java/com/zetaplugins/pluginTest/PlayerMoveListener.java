package com.zetaplugins.pluginTest;

import com.zetaplugins.zetacore.annotations.AutoRegisterListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

@AutoRegisterListener
public class PlayerMoveListener implements Listener {
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        event.getPlayer().sendMessage("You moved!");
    }
}
