package com.zetaplugins.pluginTest;

import com.zetaplugins.zetacore.command.CommandContext;
import com.zetaplugins.zetacore.command.annotation.*;
import com.zetaplugins.zetacore.command.exception.CommandException;
import com.zetaplugins.zetacore.command.exception.CommandSenderMustBePlayerException;
import org.bukkit.entity.Player;

import java.util.List;

@Command({"testcommand", "test2command"})
@Description("A %command% for demonstration purposes")
@Permission("testplugin.%command%")
@Usage("/<command> <test1, test2. test3>")
public class TestCommand extends TestPluginCommand {

    public TestCommand(PluginTest plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandContext ctx) throws CommandException {
        var sender = ctx.getSender();
        var command = ctx.getCommand();
        var args = ctx.getArgs();

        if (!(sender instanceof Player player)) throw new CommandSenderMustBePlayerException();

        player.sendMessage("You executed the " + command.getName() + " command with arguments: " + args.toString());
        return true;
    }

    @Override
    public List<String> tabComplete(CommandContext ctx) {
        return List.of("man");
    }
}
