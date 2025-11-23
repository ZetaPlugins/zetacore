package com.zetaplugins.pluginTest;

import com.zetaplugins.zetacore.annotations.AutoRegisterCommand;
import com.zetaplugins.zetacore.annotations.InjectManager;
import com.zetaplugins.zetacore.commands.ArgumentList;
import com.zetaplugins.zetacore.commands.exceptions.CommandException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

//@AutoRegisterCommand(
//        commands = {"count"},
//        description = "A %command% that does nothing",
//        permission = "testplugin.%command%",
//        usage = "/<command>"
//)
public class CountCommand extends TestPluginCommand {

    @InjectManager
    private CountManager countManager;

    public CountCommand(PluginTest plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, ArgumentList args) throws CommandException {
        if (sender == null) throw new CommandException("Sender is null");
        if (!(sender instanceof Player player)) throw new CommandException("Sender is not a player");
        countManager.incrementCounter(player.getUniqueId());
        sender.sendMessage("Count is now: " + countManager.getCounter(player.getUniqueId()));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, ArgumentList args) {
        return List.of();
    }
}
