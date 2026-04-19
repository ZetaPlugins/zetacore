package com.zetaplugins.pluginTest;

import com.zetaplugins.zetacore.command.CommandContext;
import com.zetaplugins.zetacore.command.annotation.Command;
import com.zetaplugins.zetacore.command.annotation.Description;
import com.zetaplugins.zetacore.command.annotation.Permission;
import com.zetaplugins.zetacore.command.annotation.Usage;
import com.zetaplugins.zetacore.command.exception.CommandException;
import com.zetaplugins.zetacore.di.annotation.Inject;
import org.bukkit.entity.Player;

import java.util.List;

@Command("count")
@Description("A %command% that does nothing")
@Permission("testplugin.%command%")
@Usage("/<command>")
public class CountCommand extends TestPluginCommand {

    @Inject
    private GenericCountService countService;

    public CountCommand(PluginTest plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandContext ctx) throws CommandException {
        if (ctx.getSender() == null) throw new CommandException("Sender is null");
        if (!(ctx.getSender() instanceof Player player)) throw new CommandException("Sender is not a player");
        countService.incrementCounter(player.getUniqueId());
        ctx.getSender().sendMessage("Count is now: " + countService.getCounter(player.getUniqueId()));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandContext ctx) {
        return List.of();
    }
}
