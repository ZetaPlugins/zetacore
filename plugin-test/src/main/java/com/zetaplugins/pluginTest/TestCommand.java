package com.zetaplugins.pluginTest;

import com.zetaplugins.zetacore.annotations.AutoRegisterCommand;
import com.zetaplugins.zetacore.commands.ArgumentList;
import com.zetaplugins.zetacore.commands.exceptions.CommandException;
import com.zetaplugins.zetacore.commands.exceptions.CommandSenderMustBePlayerException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@AutoRegisterCommand(
        commands = {"testcommand", "test2command"},
        description = "A %command% for demonstration purposes",
        permission = "testplugin.%command%",
        usage = "/<command> <test1, test2. test3>"
)
public class TestCommand extends TestPluginCommand {

    public TestCommand(PluginTest plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, ArgumentList args) throws CommandException {
        if (!(sender instanceof Player player)) throw new CommandSenderMustBePlayerException();

        player.sendMessage("You executed the " + command.getName() + " command with arguments: " + args.toString());
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, ArgumentList args) {
        return List.of();
    }
}
