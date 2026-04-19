package com.zetaplugins.pluginTest;

import com.zetaplugins.zetacore.command.CommandContext;
import com.zetaplugins.zetacore.command.PluginCommand;
import com.zetaplugins.zetacore.command.annotation.*;
import com.zetaplugins.zetacore.command.exception.CommandException;
import org.bukkit.entity.Player;

import java.util.List;

@Command("greet")
@Description("A %command% that greets a player")
@Usage("/<command> <player>")
@Permission("myplugin.command.greet")
@Alias("hello")
public class GreetCommand extends PluginCommand<PluginTest> {

    public GreetCommand(PluginTest plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandContext ctx) throws CommandException {
        var args = ctx.getArgs();
        var sender = ctx.getSender();

        Player targetPlayer = args.getPlayer(0, getPlugin());

        if (targetPlayer == null) {
            sender.sendMessage("Player " + args.getString(0, "[no name]") + " not found.");
            return false;
        }

        sender.sendMessage("Hello, " + targetPlayer.getName() + "!");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandContext ctx) {
        if (ctx.getArgs().getCurrentArgIndex() == 0) return getPlayerOptions(ctx.getArgs().getCurrentArg());
        else return List.of();
    }
}
