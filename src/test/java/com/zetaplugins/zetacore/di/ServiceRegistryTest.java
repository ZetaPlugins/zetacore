package com.zetaplugins.zetacore.di;

import com.zetaplugins.zetacore.di.annotation.Inject;
import com.zetaplugins.zetacore.di.annotation.Service;
import com.zetaplugins.zetacore.scan.ClassScanner;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceRegistryTest {

    @Mock
    private JavaPlugin plugin;

    private ClassScanner scanner;
    private ServiceRegistry registry;

    @BeforeEach
    void setUp() {
        scanner = new ClassScanner("com.zetaplugins.zetacore.di", getClass().getClassLoader());
        registry = new ServiceRegistry(plugin, true, scanner);
    }

    public interface Greeter {}

    @Service
    public static class EnglishGreeter implements Greeter {}

    @Service
    static class CoreConsumer {
        @Inject ServiceRegistry registry;
        @Inject ClassScanner scanner;
        @Inject Plugin plugin;
    }

    @Service
    static class LoggingService {
        final Logger constructorLogger;
        @Inject Logger fieldLogger;

        LoggingService(Logger logger) {
            this.constructorLogger = logger;
        }
    }

    static class LoggerBeans {
        @com.zetaplugins.zetacore.di.annotation.Bean
        Logger logger() {
            return Logger.getLogger("other");
        }
    }

    @Service
    static class FieldCycleA {
        @Inject FieldCycleB b;
    }

    @Service
    static class FieldCycleB {
        @Inject FieldCycleA a;
    }

    @Test
    void injectsCoreObjectsWithServiceAnnotationRequired() {
        CoreConsumer consumer = registry.getOrCreate(CoreConsumer.class);
        assertSame(registry, consumer.registry);
        assertSame(scanner, consumer.scanner);
        assertSame(plugin, consumer.plugin);
    }

    @Test
    void exposesScanner() {
        assertSame(scanner, registry.getClassScanner());
    }

    @Test
    void reusesConcreteInstanceWhenRequestedByInterface() {
        EnglishGreeter concrete = registry.getOrCreate(EnglishGreeter.class);
        assertSame(concrete, registry.getOrCreate(Greeter.class));
    }

    @Test
    void resolvesFieldCycles() {
        FieldCycleA a = registry.getOrCreate(FieldCycleA.class);
        assertSame(a, a.b.a);
    }

    @Test
    void injectsPluginLogger() {
        Logger logger = Logger.getLogger("test-plugin");
        when(plugin.getLogger()).thenReturn(logger);

        LoggingService service = registry.getOrCreate(LoggingService.class);

        assertSame(logger, service.constructorLogger);
        assertSame(logger, service.fieldLogger);
    }

    @Test
    void rejectsOtherLoggerProviders() {
        assertThrows(ServiceException.class, () -> registry.registerConfiguration(LoggerBeans.class));
        assertThrows(ServiceException.class, () -> registry.registerInstance(Logger.class, Logger.getLogger("other")));
    }
}
