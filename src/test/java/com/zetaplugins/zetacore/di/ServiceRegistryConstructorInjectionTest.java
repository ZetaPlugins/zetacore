package com.zetaplugins.zetacore.di;

import com.zetaplugins.zetacore.di.annotation.Inject;
import com.zetaplugins.zetacore.di.annotation.PostConstruct;
import com.zetaplugins.zetacore.di.annotation.Service;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ServiceRegistryConstructorInjectionTest {

    @Mock
    private JavaPlugin plugin;

    private ServiceRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ServiceRegistry(plugin, false, "com.zetaplugins.zetacore.di");
    }

    static class Repository {}

    static class FieldService {
        @Inject Repository repository;
    }

    static class AnnotatedCtorService {
        final Repository repository;
        final JavaPlugin plugin;
        final boolean usedInjectCtor;

        AnnotatedCtorService() {
            this.repository = null;
            this.plugin = null;
            this.usedInjectCtor = false;
        }

        @Inject
        AnnotatedCtorService(Repository repository, JavaPlugin plugin) {
            this.repository = repository;
            this.plugin = plugin;
            this.usedInjectCtor = true;
        }
    }

    static class SingleCtorService {
        final Repository repository;
        final Plugin plugin;
        final FieldService fieldService;

        SingleCtorService(Repository repository, Plugin plugin, FieldService fieldService) {
            this.repository = repository;
            this.plugin = plugin;
            this.fieldService = fieldService;
        }
    }

    static class MixedService {
        final Repository repository;
        @Inject FieldService fieldService;
        boolean postConstructCalled;

        MixedService(Repository repository) {
            this.repository = repository;
        }

        @PostConstruct
        void init() {
            assertNotNull(fieldService);
            postConstructCalled = true;
        }
    }

    static class CircularA {
        CircularA(CircularB b) {}
    }

    static class CircularB {
        CircularB(CircularA a) {}
    }

    static class TwoInjectCtors {
        @Inject TwoInjectCtors() {}
        @Inject TwoInjectCtors(Repository r) {}
    }

    static class AmbiguousCtors {
        AmbiguousCtors(Repository r) {}
        AmbiguousCtors(FieldService f) {}
    }

    static class PrimitiveCtor {
        PrimitiveCtor(int value) {}
    }

    @Service(scope = ServiceScope.PROTOTYPE)
    static class PrototypeService {
        final Repository repository;
        PrototypeService(Repository repository) {
            this.repository = repository;
        }
    }

    static class NotAService {
        final Repository repository;
        NotAService(Repository repository) {
            this.repository = repository;
        }
    }

    @Test
    void usesInjectAnnotatedConstructor() {
        AnnotatedCtorService service = registry.getOrCreate(AnnotatedCtorService.class);
        assertTrue(service.usedInjectCtor);
        assertSame(plugin, service.plugin);
        assertSame(registry.getOrCreate(Repository.class), service.repository);
    }

    @Test
    void usesSingleDeclaredConstructorWithoutAnnotation() {
        SingleCtorService service = registry.getOrCreate(SingleCtorService.class);
        assertSame(plugin, service.plugin);
        assertSame(registry.getOrCreate(Repository.class), service.repository);
        assertSame(registry.getOrCreate(Repository.class), service.fieldService.repository);
    }

    @Test
    void combinesConstructorAndFieldInjection() {
        MixedService service = registry.getOrCreate(MixedService.class);
        assertNotNull(service.repository);
        assertNotNull(service.fieldService);
        assertTrue(service.postConstructCalled);
    }

    @Test
    void detectsCircularConstructorDependencies() {
        ServiceException e = assertThrows(ServiceException.class, () -> registry.getOrCreate(CircularA.class));
        assertTrue(e.getMessage().contains("Circular dependency"));
    }

    @Test
    void rejectsMultipleInjectConstructors() {
        ServiceException e = assertThrows(ServiceException.class, () -> registry.getOrCreate(TwoInjectCtors.class));
        assertTrue(e.getMessage().contains("multiple @Inject constructors"));
    }

    @Test
    void rejectsAmbiguousConstructors() {
        ServiceException e = assertThrows(ServiceException.class, () -> registry.getOrCreate(AmbiguousCtors.class));
        assertTrue(e.getMessage().contains("No suitable constructor"));
    }

    @Test
    void rejectsPrimitiveParameters() {
        ServiceException e = assertThrows(ServiceException.class, () -> registry.getOrCreate(PrimitiveCtor.class));
        assertTrue(e.getMessage().contains("primitive"));
    }

    @Test
    void prototypeServicesGetConstructorInjection() {
        PrototypeService a = registry.getOrCreate(PrototypeService.class);
        PrototypeService b = registry.getOrCreate(PrototypeService.class);
        assertNotSame(a, b);
        assertSame(a.repository, b.repository);
    }

    @Test
    void createDoesNotRegisterInstance() {
        NotAService a = registry.create(NotAService.class);
        NotAService b = registry.create(NotAService.class);
        assertNotSame(a, b);
        assertSame(a.repository, b.repository);
    }
}
