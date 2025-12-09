package com.zetaplugins.zetacore.services.config;

import com.zetaplugins.zetacore.services.config.testconfigs.ItemConfigSection;
import com.zetaplugins.zetacore.services.config.testconfigs.MyConfig;
import com.zetaplugins.zetacore.services.config.testconfigs.SomeStatus;
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

        assertEquals(SomeStatus.ACTIVE, myConfig.someStatus);

        assertEquals(3, myConfig.statusList.size());
        assertEquals(SomeStatus.ACTIVE, myConfig.statusList.get(0));
        assertEquals(SomeStatus.INACTIVE, myConfig.statusList.get(1));
        assertEquals(SomeStatus.PENDING, myConfig.statusList.get(2));

        ItemConfigSection sword = myConfig.items.get(0);
        assertEquals("Schwert", sword.getName());
        assertEquals(276, sword.getId());
        assertEquals(15.99, sword.getPrice(), 0.001);
        assertEquals("Ein mächtiges Schwert", sword.getLore().get(0).getLoreLine());
        assertEquals(1, sword.getLore().get(0).getLineNumber());
        assertEquals("Geschmiedet in den Tiefen der Erde", sword.getLore().get(1).getLoreLine());
        assertEquals(2, sword.getLore().get(1).getLineNumber());


        ItemConfigSection shield = myConfig.items.get(1);
        assertEquals("Schild", shield.getName());
        assertEquals(311, shield.getId());
        assertEquals(10.49, shield.getPrice(), 0.001);
        assertEquals("Ein starker Schild", shield.getLore().get(0).getLoreLine());
        assertEquals(1, shield.getLore().get(0).getLineNumber());
        assertEquals("Schützt vor Angriffen", shield.getLore().get(1).getLoreLine());
        assertEquals(3, shield.getLore().get(1).getLineNumber());
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

    @Test
    void testMappingMaps() {
        MyConfig myConfig = ConfigMapper.map(config, MyConfig.class);
        assertNotNull(myConfig.advancedItems);
        assertEquals(2, myConfig.advancedItems.size());
        assertTrue(myConfig.advancedItems.containsKey("magicwand"));
        assertTrue(myConfig.advancedItems.containsKey("healingpotion"));

        var wand = myConfig.advancedItems.get("magicwand");
        assertEquals(280, wand.id);
        assertEquals("WAND", wand.type);
        assertEquals(2, wand.description.size());
        assertEquals("Eine Zauberstab voller Magie", wand.description.get(0));
        assertEquals("Kann mächtige Zauber wirken", wand.description.get(1));

        var healingpotion = myConfig.advancedItems.get("healingpotion");
        assertEquals(373, healingpotion.id);
        assertEquals("POTION", healingpotion.type);
        assertEquals(2, healingpotion.description.size());
        assertEquals("Ein Trank zur Heilung", healingpotion.description.get(0));
        assertEquals("Stellt Gesundheit wieder her", healingpotion.description.get(1));
    }
}