package com.zetaplugins.pluginTest;

import com.zetaplugins.zetacore.annotations.AutoRegisterCommand;
import com.zetaplugins.zetacore.commands.ArgumentList;
import com.zetaplugins.zetacore.commands.PluginCommand;
import com.zetaplugins.zetacore.commands.exceptions.CommandException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@AutoRegisterCommand(
        commands = "greet",
        description = "Greets a player",
        usage = "/greet <player>",
        permission = "myplugin.command.greet",
        aliases = {"hello"}
)
public class GreetCommand extends PluginCommand<PluginTest> {

    public GreetCommand(PluginTest plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, ArgumentList args) throws CommandException {
        Player targetPlayer = args.getPlayer(0, getPlugin());

        if (targetPlayer == null) {
            sender.sendMessage("Player " + args.getString(0, "[no name]") + " not found.");
            return false;
        }

        sender.sendMessage("Hello, " + targetPlayer.getName() + "!");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, ArgumentList args) {
        if (args.getCurrentArgIndex() == 0) return getPlayerOptions(args.getCurrentArg());
        else return List.of();
    }
}
