package com.zetaplugins.pluginTest;

import com.zetaplugins.zetacore.commands.PluginCommand;
import com.zetaplugins.zetacore.commands.exceptions.CommandSenderMustBePlayerException;

public abstract class TestPluginCommand extends PluginCommand<PluginTest> {

    public TestPluginCommand(PluginTest plugin) {
        super(plugin);

        registerExceptionHandler(
                CommandSenderMustBePlayerException.class,
                (ctx, exception) -> {
                    ctx.getSender().sendMessage("You must be a player to use this command.");
                    if (ctx.getArgs().size() > 0) ctx.getSender().sendMessage("Arguments provided: " + ctx.getArgs().toString());
                    return true;
                }
        );
    }
}
