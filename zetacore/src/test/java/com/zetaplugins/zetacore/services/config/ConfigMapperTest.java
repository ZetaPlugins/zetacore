package com.zetaplugins.zetacore.services.config;

import com.zetaplugins.zetacore.services.config.testconfigs.ItemConfigSection;
import com.zetaplugins.zetacore.services.config.testconfigs.MyConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfigMapperTest {

    private FileConfiguration config;

    @BeforeEach
    void setUp() throws Exception {
        // Load a test YAML file
        File file = new File("src/test/resources/test-config.yml");
        config = YamlConfiguration.loadConfiguration(file);
    }

    @Test
    void testBasicMapping() {
        MyConfig myConfig = ConfigMapper.map(config, MyConfig.class);

        assertNotNull(myConfig);
        assertEquals("de-DE", myConfig.lang);
        assertNotNull(myConfig.settings);
        assertTrue(myConfig.settings.enableFeature);
        assertEquals(List.of("LustigerName1", "LustigerName2", "LustigerName3"), myConfig.settings.funnynames);

        assertNotNull(myConfig.items);
        assertEquals(2, myConfig.items.size());

        ItemConfigSection sword = myConfig.items.get(0);
        assertEquals("Schwert", sword.getName());
        assertEquals(276, sword.getId());
        assertEquals(15.99, sword.getPrice(), 0.001);
        assertEquals(List.of("Ein mächtiges Schwert", "Geschmiedet in den Tiefen der Erde"), sword.getLore());

        ItemConfigSection shield = myConfig.items.get(1);
        assertEquals("Schild", shield.getName());
        assertEquals(311, shield.getId());
        assertEquals(10.49, shield.getPrice(), 0.001);
        assertEquals(List.of("Ein starker Schild", "Schützt vor Angriffen"), shield.getLore());
    }

    @Test
    void testNestedConfigMapping() {
        MyConfig myConfig = ConfigMapper.map(config, MyConfig.class);
        assertNotNull(myConfig.settings);
        assertTrue(myConfig.settings.enableFeature);
        assertEquals(3, myConfig.settings.funnynames.size());
    }

    @Test
    void testListOfNestedObjects() {
        MyConfig myConfig = ConfigMapper.map(config, MyConfig.class);
        assertEquals(2, myConfig.items.size());

        for (ItemConfigSection item : myConfig.items) {
            assertNotNull(item.getName());
            assertTrue(item.getPrice() > 0);
            assertNotNull(item.getLore());
        }
    }

    @Test
    void testInvalidTypeThrowsException() {
        FileConfiguration invalidConfig = new YamlConfiguration();
        invalidConfig.set("invalid", new Object()); // arbitrary non-mappable type

        class InvalidConfig {
            public Object invalid;
        }

        assertThrows(ConfigMappingException.class, () -> ConfigMapper.map(invalidConfig, InvalidConfig.class));
    }
}