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
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArgumentListTest {

    @Mock
    private JavaPlugin plugin;

    @Mock
    private Server server;

    @Mock
    private Player player;

    @BeforeEach
    void setup() {
        lenient().when(plugin.getServer()).thenReturn(server);
    }

    private enum SampleEnum { ALPHA, BETA }

    @Test
    void hasArgAndGetArg() {
        ArgumentList al = new ArgumentList(new String[]{"one", "two"});
        assertTrue(al.hasArg(0));
        assertTrue(al.hasArg(1));
        assertFalse(al.hasArg(2));
        assertEquals("one", al.getArg(0));
        assertEquals("two", al.getArg(1));
        assertNull(al.getArg(2));
    }

    @Test
    void getStringWithDefault() {
        ArgumentList al = new ArgumentList(new String[]{"a"});
        assertEquals("a", al.getString(0, "x"));
        assertEquals("x", al.getString(1, "x"));
    }

    @Test
    void getJoinedStringDefaultAndCustomSeparator() {
        ArgumentList al = new ArgumentList(new String[]{"a", "b", "c"});
        assertEquals("a b c", al.getJoinedString(0));
        assertEquals("b,c", al.getJoinedString(1, ","));
        assertEquals("", al.getJoinedString(3));
    }

    @Test
    void getPlayerAndDefaultPlayer() {
        when(server.getPlayer("bob")).thenReturn(player);
        ArgumentList al = new ArgumentList(new String[]{"bob"});
        assertSame(player, al.getPlayer(0, plugin));

        Player defaultPlayer = player;
        ArgumentList empty = new ArgumentList(new String[]{});
        assertSame(defaultPlayer, empty.getPlayer(0, defaultPlayer, plugin));
    }

    @Test
    void getIntDoubleLong_and_defaultsAnd_exceptions() {
        ArgumentList al = new ArgumentList(new String[]{"10", "3.14", "12345678901"});
        assertEquals(10, al.getInt(0));
        assertEquals(10, al.getInt(0, 5));
        assertThrows(NumberFormatException.class, () -> new ArgumentList(new String[]{}).getInt(0));
        assertEquals(3.14, al.getDouble(1), 1e-9);
        assertEquals(3.14, al.getDouble(1, 2.0), 1e-9);
        assertEquals(12345678901L, al.getLong(2));
        assertEquals(12345678901L, al.getLong(2, 5L));
    }

    @Test
    void getIntWithBounds() {
        ArgumentList al = new ArgumentList(new String[]{"50"});
        assertEquals(50, al.getInt(0, 0, 10, 100));
        assertEquals(20, al.getInt(0, 0, 10, 20));
        ArgumentList alSmall = new ArgumentList(new String[]{"5"});
        assertEquals(10, alSmall.getInt(0, 0, 10, 20));
        assertEquals(50, al.getInt(0, 0, 0, 100));
    }

    @Test
    void getBooleanVariousInputs() {
        ArgumentList al = new ArgumentList(new String[]{"true", "yes", "n", "0", "maybe"});
        assertTrue(al.getBoolean(0, false));
        assertTrue(al.getBoolean(1, false));
        assertFalse(al.getBoolean(2, true));
        assertFalse(al.getBoolean(3, true));
        assertEquals("maybe", al.getArg(4));
        assertTrue(al.getBoolean(4, true));
        assertFalse(al.getBoolean(4, false));
    }

    @Test
    void getEnumAndIgnoreCase() {
        ArgumentList al = new ArgumentList(new String[]{"alpha", "BETA", "gamma"});
        assertEquals(SampleEnum.ALPHA, al.getEnum(0, SampleEnum.class));
        assertEquals(SampleEnum.BETA, al.getEnum(1, SampleEnum.class));
        assertNull(al.getEnum(2, SampleEnum.class));
        assertEquals(SampleEnum.ALPHA, al.getEnumIgnoreCase(0, SampleEnum.class, SampleEnum.BETA));
        assertEquals(SampleEnum.BETA, al.getEnumIgnoreCase(2, SampleEnum.class, SampleEnum.BETA));
    }

    @Test
    void currentArgIndexAndCurrentArgAndSizeAndGetArgsAndGetAllArgs() {
        ArgumentList al = new ArgumentList(new String[]{"x", "y", "z"});
        assertEquals(2, al.getCurrentArgIndex());
        assertEquals("z", al.getCurrentArg());
        assertEquals(3, al.size());
        String[] raw = al.getArgs();
        assertArrayEquals(new String[]{"x", "y", "z"}, raw);
        List<String> all = al.getAllArgs();
        assertEquals(List.of("x", "y", "z"), all);
        // ensure getAllArgs returns a mutable copy
        all.add("new");
        assertEquals(3, al.size());
    }

    @Test
    void iteratorAndToString() {
        String[] data = {"a", "b", "c"};
        ArgumentList al = new ArgumentList(data);
        Iterator<String> it = al.iterator();
        List<String> collected = new ArrayList<>();
        while (it.hasNext()) collected.add(it.next());
        assertEquals(List.of(data), collected);
        String s = al.toString();
        assertTrue(s.contains("args="));
        assertTrue(s.contains("a"));
        assertTrue(s.contains("b"));
        assertTrue(s.contains("c"));
    }
}
