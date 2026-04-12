package com.zetaplugins.pluginTest;

import com.zetaplugins.pluginTest.config.MyConfig;
import com.zetaplugins.zetacore.command.ArgumentList;
import com.zetaplugins.zetacore.command.annotation.AutoRegisterCommand;
import com.zetaplugins.zetacore.command.exception.CommandException;
import com.zetaplugins.zetacore.config.ConfigService;
import com.zetaplugins.zetacore.di.annotation.InjectService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

@AutoRegisterCommand(
        commands = "getconfig",
        description = "Get the plugin configuration",
        usage = "/getconfig"
)
public class GetConfigCommand extends TestPluginCommand {

    @InjectService
    private ConfigService configService;

    public GetConfigCommand(PluginTest plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, ArgumentList args) throws CommandException {
        MyConfig config = configService.getConfig(MyConfig.class);
        sender.sendMessage("Current language setting: " + config.lang);
        sender.sendMessage("Feature enabled: " + config.settings.enableFeature);
        if (config.settings.funnynames != null) {
            for (String name : config.settings.funnynames) {
                sender.sendMessage("Funny name: " + name);
            }
        } else {
            sender.sendMessage("No funny names found.");
        }
        sender.sendMessage("Items in config:");
        for (var item : config.items) {
            sender.sendMessage(item.toString());
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, ArgumentList args) {
        return List.of();
    }
}
