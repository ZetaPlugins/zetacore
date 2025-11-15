package com.zetaplugins.pluginTest;

import com.zetaplugins.zetacore.annotations.AutoRegisterTabCompleter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@AutoRegisterTabCompleter(commands = {"testcommand", "test2command"})
public class TestCommandTabCompleter implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        return List.of("test1", "test2", "test3");
    }
}
