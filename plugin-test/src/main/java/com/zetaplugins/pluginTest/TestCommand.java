package com.zetaplugins.pluginTest;

import com.zetaplugins.zetacore.annotations.AutoRegisterCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@AutoRegisterCommand(
        commands = {"testcommand", "test2command"},
        description = "A test command for demonstration purposes",
        permission = "testplugin.use",
        usage = "/<command> <test1, test2. test3>"
)
public class TestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        commandSender.sendMessage("Test command executed!");
        return true;
    }
}
