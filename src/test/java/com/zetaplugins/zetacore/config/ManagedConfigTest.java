package com.zetaplugins.zetacore.config;

import com.zetaplugins.zetacore.config.annotation.ConfigFile;
import com.zetaplugins.zetacore.di.ServiceRegistry;
import com.zetaplugins.zetacore.di.annotation.Service;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ManagedConfigTest {

    @Mock
    private JavaPlugin plugin;

    @TempDir
    Path tempDir;

    private ConfigService configService;

    @ConfigFile("greeting")
    public static class GreetingConfig {
        public String message = "default";
        public int count = 1;
    }

    public static class NotAConfig {}

    @Service
    static class Greeter {
        final GreetingConfig config;

        Greeter(GreetingConfig config) {
            this.config = config;
        }
    }

    @BeforeEach
    void setUp() {
        lenient().when(plugin.getDataFolder()).thenReturn(tempDir.toFile());
        configService = new ConfigService(plugin);
    }

    private void write(String content) throws IOException {
        Files.writeString(tempDir.resolve("greeting.yml"), content);
    }

    @Test
    void returnsSameInstance() throws IOException {
        write("message: hi");
        GreetingConfig a = configService.getConfig(GreetingConfig.class);
        assertSame(a, configService.getConfig(GreetingConfig.class));
        assertEquals("hi", a.message);
    }

    @Test
    void snapshotIsSeparateAndUnchangedByReload() throws IOException {
        write("message: hi");
        GreetingConfig managed = configService.getConfig(GreetingConfig.class);
        GreetingConfig snapshot = configService.getSnapshot(GreetingConfig.class);
        assertNotSame(managed, snapshot);
        assertEquals("hi", snapshot.message);

        write("message: bye");
        configService.reload();

        assertEquals("bye", managed.message);
        assertEquals("hi", snapshot.message);
        assertEquals("bye", configService.getSnapshot(GreetingConfig.class).message);
    }

    @Test
    void reloadUpdatesInstanceInPlaceAndResetsMissingKeysToDefaults() throws IOException {
        write("message: hi\ncount: 5");
        GreetingConfig config = configService.getConfig(GreetingConfig.class);

        write("message: bye");
        configService.reload();

        assertEquals("bye", config.message);
        assertEquals(1, config.count);
        verify(plugin).reloadConfig();
    }

    @Test
    void getConfigWithoutCacheRefreshesManagedInstance() throws IOException {
        write("message: hi");
        GreetingConfig config = configService.getConfig(GreetingConfig.class);

        write("message: changed");
        assertSame(config, configService.getConfig(GreetingConfig.class, false));
        assertEquals("changed", config.message);
    }

    @Test
    void reloadKeepsOldValuesWhenMappingFails() throws IOException {
        write("message: hi\ncount: 5");
        GreetingConfig config = configService.getConfig(GreetingConfig.class);

        write("count: not-a-number");
        assertThrows(ConfigMappingException.class, configService::reload);
        assertEquals("hi", config.message);
        assertEquals(5, config.count);
    }

    @Test
    void registryInjectsManagedConfig() throws IOException {
        write("message: injected");
        ServiceRegistry registry = new ServiceRegistry(plugin, true, "com.zetaplugins.zetacore.config");
        registry.addResolver(configService.asResolver());

        Greeter greeter = registry.getOrCreate(Greeter.class);

        assertSame(configService.getConfig(GreetingConfig.class), greeter.config);
        assertEquals("injected", greeter.config.message);
    }

    @Test
    void resolverIgnoresNonConfigClasses() {
        assertNull(configService.asResolver().resolve(NotAConfig.class));
    }
}
