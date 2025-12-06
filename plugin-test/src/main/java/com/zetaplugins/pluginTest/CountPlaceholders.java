package com.zetaplugins.pluginTest;

import com.zetaplugins.zetacore.annotations.InjectManager;
import com.zetaplugins.zetacore.annotations.Manager;
import com.zetaplugins.zetacore.annotations.Papi;
import org.bukkit.entity.Player;

@Manager
public class CountPlaceholders {

    @InjectManager
    private CountManager countManager;

    @Papi(identifier = "count")
    public String getCountPlaceholder(Player player) {
        int count = countManager.getCounter(player.getUniqueId());
        return String.valueOf(count);
    }
}
