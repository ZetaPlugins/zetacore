package com.zetaplugins.pluginTest;

import com.zetaplugins.zetacore.command.ArgumentList;
import com.zetaplugins.zetacore.command.annotation.AutoRegisterCommand;
import com.zetaplugins.zetacore.command.exception.CommandException;
import com.zetaplugins.zetacore.di.annotation.InjectService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@AutoRegisterCommand(
        commands = {"count"},
        description = "A %command% that does nothing",
        permission = "testplugin.%command%",
        usage = "/<command>"
)
public class CountCommand extends TestPluginCommand {

    @InjectService
    private CountService countService;

    public CountCommand(PluginTest plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, ArgumentList args) throws CommandException {
        if (sender == null) throw new CommandException("Sender is null");
        if (!(sender instanceof Player player)) throw new CommandException("Sender is not a player");
        countService.incrementCounter(player.getUniqueId());
        sender.sendMessage("Count is now: " + countService.getCounter(player.getUniqueId()));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, ArgumentList args) {
        return List.of();
    }
}
