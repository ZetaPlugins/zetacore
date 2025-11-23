package com.zetaplugins.zetacore.commands;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommandUtilsTest {

    @Mock
    private JavaPlugin plugin;

    @Mock
    private Server server;

    @BeforeEach
    void setup() {
        lenient().when(plugin.getServer()).thenReturn(server);
    }

    @Test
    void getDisplayOptions_caseInsensitive_and_emptyInput_returnsAll_preservesOrder() {
        List<String> options = List.of("Apple", "apricot", "banana", "App");
        List<String> matched = CommandUtils.getDisplayOptions(options, "ap");
        assertEquals(List.of("Apple", "apricot", "App"), matched);
        List<String> all = CommandUtils.getDisplayOptions(options, "");
        assertEquals(options, all);
    }

    @Test
    void getDisplayOptions_noMatches_returnsEmptyList() {
        List<String> options = List.of("one", "two");
        List<String> matched = CommandUtils.getDisplayOptions(options, "z");
        assertTrue(matched.isEmpty());
    }

    @Test
    void getPlayerOptions_filtersByName_caseInsensitive() {
        Player pBob = mock(Player.class);
        Player pAlice = mock(Player.class);
        org.mockito.Mockito.when(pBob.getName()).thenReturn("bob");
        org.mockito.Mockito.when(pAlice.getName()).thenReturn("Alice");

        Set<Player> online = Set.of(pBob, pAlice);
        doReturn(online).when(server).getOnlinePlayers();

        List<String> bobMatches = CommandUtils.getPlayerOptions(plugin, "bo");
        assertEquals(List.of("bob"), bobMatches);

        List<String> bobMatchesUpper = CommandUtils.getPlayerOptions(plugin, "BO");
        assertEquals(List.of("bob"), bobMatchesUpper);

        List<String> all = CommandUtils.getPlayerOptions(plugin, "");
        List<String> sorted = new ArrayList<>(all);
        Collections.sort(sorted);
        List<String> expected = new ArrayList<>(List.of("Alice", "bob"));
        Collections.sort(expected);
        assertEquals(expected, sorted);
    }

    @Test
    void getPlayerOptions_noOnlinePlayers_returnsEmptyList() {
        when(server.getOnlinePlayers()).thenReturn(Collections.emptySet());
        List<String> result = CommandUtils.getPlayerOptions(plugin, "any");
        assertTrue(result.isEmpty());
    }
}
