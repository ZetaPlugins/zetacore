package com.zetaplugins.zetacore.di.conditions;

import com.zetaplugins.zetacore.di.ServiceException;
import com.zetaplugins.zetacore.di.ServiceRegistry;
import com.zetaplugins.zetacore.di.annotation.Bean;
import com.zetaplugins.zetacore.di.annotation.Configuration;
import com.zetaplugins.zetacore.di.annotation.Service;
import com.zetaplugins.zetacore.di.condition.Condition;
import com.zetaplugins.zetacore.di.condition.Conditional;
import com.zetaplugins.zetacore.di.condition.ConditionalOnMissingPlugin;
import com.zetaplugins.zetacore.di.condition.ConditionalOnPlugin;
import com.zetaplugins.zetacore.event.annotation.EventListener;
import com.zetaplugins.zetacore.event.registration.AutoEventListenerRegistrar;
import org.bukkit.Server;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConditionTest {

    @Mock private JavaPlugin plugin;
    @Mock private Server server;
    @Mock private PluginManager pluginManager;
    @Mock private Plugin vault;

    private ServiceRegistry registry;

    @BeforeEach
    void setUp() {
        when(plugin.getServer()).thenReturn(server);
        when(server.getPluginManager()).thenReturn(pluginManager);
        when(pluginManager.getPlugin("Vault")).thenReturn(vault);
        FeatureEnabled.enabled = true;
        FeatureEnabled.evaluations = 0;
        registry = new ServiceRegistry(plugin, true, "com.zetaplugins.zetacore.di.conditions");
    }

    @Service
    public static class Settings {
        boolean feature() {
            return FeatureEnabled.enabled;
        }
    }

    public static class FeatureEnabled implements Condition {
        static boolean enabled;
        static int evaluations;
        private final Settings settings;

        public FeatureEnabled(Settings settings) {
            this.settings = settings;
        }

        @Override
        public boolean matches() {
            evaluations++;
            return settings.feature();
        }
    }

    public interface Economy {}

    @Service
    @ConditionalOnPlugin("Vault")
    public static class VaultEconomy implements Economy {}

    @Service
    @ConditionalOnMissingPlugin("Vault")
    public static class BuiltinEconomy implements Economy {}

    @Service(eagerlyLoad = true)
    @ConditionalOnPlugin("Missing")
    public static class NeedsMissingPlugin {
        static int created;

        NeedsMissingPlugin() {
            created++;
        }
    }

    public interface Mode {}

    public record NamedMode(String name) implements Mode {}

    @Configuration
    public static class ModeBeans {
        @Bean
        @Conditional(FeatureEnabled.class)
        Mode featureMode() {
            return new NamedMode("feature");
        }

        @Bean
        @ConditionalOnPlugin("Missing")
        Mode missingMode() {
            return new NamedMode("missing");
        }
    }

    public static class Unused {}

    @Configuration
    @ConditionalOnPlugin("Missing")
    public static class InactiveBeans {
        @Bean
        Unused unused() {
            return new Unused();
        }
    }

    @Configuration
    public static class ReplacesInactiveServiceBeans {
        @Bean
        NeedsMissingPlugin replacement() {
            return null;
        }
    }

    @EventListener
    public static class AlwaysListener implements Listener {}

    @EventListener
    @Conditional(FeatureEnabled.class)
    public static class FeatureListener implements Listener {}

    @Test
    void pluginConditions() {
        var conditions = registry.getConditionEvaluator();
        assertTrue(conditions.matches(VaultEconomy.class));
        assertFalse(conditions.matches(BuiltinEconomy.class));
        assertEquals("@ConditionalOnPlugin(\"Missing\")", conditions.findFailedCondition(NeedsMissingPlugin.class));
    }

    @Test
    void customConditionsGetInjectionAndAreCached() {
        var conditions = registry.getConditionEvaluator();
        assertTrue(conditions.matches(FeatureListener.class));
        assertTrue(conditions.matches(FeatureListener.class));
        assertEquals(1, FeatureEnabled.evaluations);
    }

    @Test
    void discoversOnlyActiveImplementation() {
        assertInstanceOf(VaultEconomy.class, registry.getOrCreate(Economy.class));

        when(pluginManager.getPlugin("Vault")).thenReturn(null);
        ServiceRegistry withoutVault = new ServiceRegistry(plugin, true, "com.zetaplugins.zetacore.di.conditions");
        assertInstanceOf(BuiltinEconomy.class, withoutVault.getOrCreate(Economy.class));
    }

    @Test
    void rejectsInjectingInactiveService() {
        ServiceException e = assertThrows(ServiceException.class, () -> registry.getOrCreate(NeedsMissingPlugin.class));
        assertTrue(e.getMessage().contains("@ConditionalOnPlugin(\"Missing\")"));
    }

    @Test
    void skipsInactiveEagerServices() {
        NeedsMissingPlugin.created = 0;
        registry.initializeEagerServices();
        assertEquals(0, NeedsMissingPlugin.created);
    }

    @Test
    void registersOnlyActiveBeans() {
        registry.registerConfiguration(ModeBeans.class);
        assertEquals(new NamedMode("feature"), registry.getOrCreate(Mode.class));
    }

    @Test
    void skipsInactiveConfigurationClass() {
        registry.registerConfiguration(InactiveBeans.class);
        ServiceException e = assertThrows(ServiceException.class, () -> registry.getOrCreate(Unused.class));
        assertFalse(e.getMessage().contains("returned"));
    }

    @Test
    void beanMayReplaceInactiveService() {
        assertDoesNotThrow(() -> registry.registerConfiguration(ReplacesInactiveServiceBeans.class));
    }

    @Test
    void skipsInactiveListeners() {
        FeatureEnabled.enabled = false;
        List<String> registered = new AutoEventListenerRegistrar(plugin, registry.getClassScanner(), registry)
                .registerAllListeners();

        assertEquals(List.of("AlwaysListener"), registered);
        verify(pluginManager, times(1)).registerEvents(any(AlwaysListener.class), eq(plugin));
    }
}
