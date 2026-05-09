package com.zetaplugins.pluginTest;

import com.zetaplugins.pluginTest.config.MyConfig;
import com.zetaplugins.zetacore.command.CommandContext;
import com.zetaplugins.zetacore.command.annotation.Command;
import com.zetaplugins.zetacore.command.annotation.Description;
import com.zetaplugins.zetacore.command.annotation.Usage;
import com.zetaplugins.zetacore.command.exception.CommandException;
import com.zetaplugins.zetacore.config.ConfigService;
import com.zetaplugins.zetacore.di.annotation.Inject;

import java.util.List;

@Command("getconfig")
@Description("A %command% that retrieves and displays the plugin configuration")
@Usage("/<command>")
public class GetConfigCommand extends TestPluginCommand {

    @Inject
    private ConfigService configService;

    public GetConfigCommand(PluginTest plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandContext ctx) throws CommandException {
        var sender = ctx.getSender();
        MyConfig config = configService.getConfig(MyConfig.class);
        sender.sendMessage("Current language setting: " + config.getLang());
        sender.sendMessage("Feature enabled: " + config.getSettings().enableFeature);
        if (config.getSettings().funnynames != null) {
            for (String name : config.getSettings().funnynames) {
                sender.sendMessage("Funny name: " + name);
            }
        } else {
            sender.sendMessage("No funny names found.");
        }
        sender.sendMessage("Items in config:");
        for (var item : config.getItems()) {
            sender.sendMessage(item.toString());
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandContext ctx) {
        return List.of();
    }
}
