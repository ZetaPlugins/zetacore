package com.zetaplugins.pluginTest;

import com.zetaplugins.zetacore.di.annotation.InjectService;
import com.zetaplugins.zetacore.di.annotation.Service;
import com.zetaplugins.zetacore.integration.papi.annotation.Papi;
import com.zetaplugins.zetacore.integration.papi.annotation.PapiParam;
import org.bukkit.entity.Player;

@Service
public class CountPlaceholders {

    @InjectService
    private CountService countService;

    @Papi(identifier = "count")
    public String getCountPlaceholder(Player player) {
        int count = countService.getCounter(player.getUniqueId());
        return String.valueOf(count);
    }

    @Papi(identifier = "count_plus_{num}_{id}")
    public String getCountPlusNumberPlaceholder(Player player, @PapiParam("id") String id, @PapiParam("num") int number) {
        int count = countService.getCounter(player.getUniqueId());
        return String.valueOf(count + number) + " (ID: " + id + ")";
    }
}
