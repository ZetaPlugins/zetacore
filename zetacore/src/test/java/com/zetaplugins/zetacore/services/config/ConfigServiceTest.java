package com.zetaplugins.zetacore.services.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class ConfigServiceTest {

    @Mock
    private JavaPlugin plugin;

    private ConfigService configService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        when(plugin.getDataFolder()).thenReturn(tempDir.toFile());
        configService = new ConfigService(plugin);
    }

    private void writeYamlFile(Path dir, String name, String content) throws IOException {
        Path file = dir.resolve(name + ".yml");
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
    }

    @Test
    void getConfig_loadsExistingFile_andCaches() throws IOException {
        writeYamlFile(tempDir, "chat", "greeting: \"hello-test\"");

        FileConfiguration cfg1 = configService.getConfig("chat");
        FileConfiguration cfg2 = configService.getConfig("chat"); // should hit cache

        assertEquals("hello-test", cfg1.getString("greeting"));
        assertSame(cfg1, cfg2);
    }

    @Test
    void getConfig_withUseCacheFalse_returnsFreshInstances() throws IOException {
        writeYamlFile(tempDir, "storage", "size: 42");

        FileConfiguration a = configService.getConfig("storage", false);
        FileConfiguration b = configService.getConfig("storage", false);

        assertEquals(42, a.getInt("size"));
        assertEquals(42, b.getInt("size"));
        assertNotSame(a, b);
    }

    @Test
    void getConfig_whenFileMissing_callsSaveResource_andLoadsCreatedFile() {
        when(plugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("TestLogger"));
        Path cfgPath = tempDir.resolve("materials.yml");
        assertFalse(Files.exists(cfgPath));

        doAnswer(invocation -> {
            String resourceName = invocation.getArgument(0, String.class); // e.g., "materials.yml"
            Path dest = tempDir.resolve(resourceName);
            Files.createDirectories(dest.getParent());
            Files.writeString(dest, "default: created-by-saveResource\n");
            return null;
        }).when(plugin).saveResource(anyString(), anyBoolean());

        FileConfiguration cfg = configService.getConfig("materials");

        verify(plugin, times(1)).saveResource(eq("materials.yml"), eq(false));
        assertEquals("created-by-saveResource", cfg.getString("default"));
        assertTrue(Files.exists(cfgPath));
    }

    @Test
    void clearCache_removesCachedEntries() throws IOException {
        writeYamlFile(tempDir, "chat", "value: 1");
        FileConfiguration first = configService.getConfig("chat");

        FileConfiguration cached = configService.getConfig("chat");
        assertSame(first, cached);

        configService.clearCache();

        writeYamlFile(tempDir, "chat", "value: 2");

        FileConfiguration afterClear = configService.getConfig("chat");
        assertNotSame(first, afterClear, "Expected a new FileConfiguration instance after clearing cache");
        assertEquals(2, afterClear.getInt("value"));
    }

    @Test
    void getConfig_usingPluginConfig_overload_works() throws IOException {
        writeYamlFile(tempDir, "myconfig", "enabled: true");

        PluginConfig cfgEnum = new PluginConfig() {
            @Override
            public String getFileName() {
                return "myconfig";
            }
        };

        FileConfiguration loaded = configService.getConfig(cfgEnum);

        assertTrue(loaded.getBoolean("enabled"));
    }

    @Test
    void getConfig_callsCreateDirectories_whenDataFolderMissing() throws IOException {
        Path missingDir = tempDir.resolve("nonexistent");

        Files.deleteIfExists(missingDir);
        when(plugin.getDataFolder()).thenReturn(missingDir.toFile());

        doAnswer(invocation -> {
            String resourceName = invocation.getArgument(0, String.class);
            Path dest = missingDir.resolve(resourceName);
            Files.createDirectories(dest.getParent());
            Files.writeString(dest, "ok: test\n");
            return null;
        }).when(plugin).saveResource(anyString(), anyBoolean());

        FileConfiguration cfg = configService.getConfig("newconf");

        verify(plugin).saveResource(eq("newconf.yml"), eq(false));
        assertEquals("test", cfg.getString("ok"));
        assertTrue(Files.exists(missingDir.resolve("newconf.yml")));
    }
}