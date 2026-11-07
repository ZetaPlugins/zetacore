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

import static org.junit.jupiter.api.Assertions.*;

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
}
