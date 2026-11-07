package com.zetaplugins.zetacore.command;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandContextTest {

    @Mock private Server server;
    @Mock private Player playerSender;
    @Mock private Player target;
    @Mock private CommandSender console;
    @Mock private Command command;

    private CommandContext ctx(CommandSender sender, String... args) {
        return new CommandContext(sender, command, "test", new ArgumentList(args));
    }

    @Test
    void playerSenderPresentOnlyForPlayers() {
        assertEquals(Optional.of(playerSender), ctx(playerSender).getPlayerSender());
        assertEquals(Optional.empty(), ctx(console).getPlayerSender());
    }

    @Test
    void getPlayerResolvesExactOnlineName() {
        when(console.getServer()).thenReturn(server);
        when(server.getPlayerExact("bob")).thenReturn(target);
        assertEquals(Optional.of(target), ctx(console, "bob").getPlayer(0));
    }

    @Test
    void getPlayerEmptyWhenOfflineOrMissing() {
        when(console.getServer()).thenReturn(server);
        assertEquals(Optional.empty(), ctx(console, "nobody").getPlayer(0));
        assertEquals(Optional.empty(), ctx(console).getPlayer(0));
    }

    @Test
    void getPlayerOrSenderFallsBackToSenderWhenArgMissing() {
        assertEquals(Optional.of(playerSender), ctx(playerSender).getPlayerOrSender(0));
        assertEquals(Optional.empty(), ctx(console).getPlayerOrSender(0));
    }

    @Test
    void getPlayerOrSenderDoesNotFallBackWhenTargetOffline() {
        when(playerSender.getServer()).thenReturn(server);
        assertEquals(Optional.empty(), ctx(playerSender, "nobody").getPlayerOrSender(0));
    }
}
