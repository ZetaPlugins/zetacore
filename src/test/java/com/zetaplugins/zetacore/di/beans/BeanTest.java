package com.zetaplugins.zetacore.di.beans;

import com.zetaplugins.zetacore.di.ServiceException;
import com.zetaplugins.zetacore.di.ServiceRegistry;
import com.zetaplugins.zetacore.di.annotation.Bean;
import com.zetaplugins.zetacore.di.annotation.Inject;
import com.zetaplugins.zetacore.di.annotation.PostConstruct;
import com.zetaplugins.zetacore.di.annotation.Service;
import com.zetaplugins.zetacore.di.beans.discovered.DiscoveredBeans;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BeanTest {

    @Mock
    private JavaPlugin plugin;

    private ServiceRegistry registry;

    @BeforeEach
    void setUp() {
        DiscoveredBeans.lazyCreated = 0;
        DiscoveredBeans.eagerCreated = 0;
        registry = new ServiceRegistry(plugin, true, "com.zetaplugins.zetacore.di.beans");
    }

    public interface Shape {}

    public static class Square implements Shape {
        @Inject Helper helper;
        int postConstructCalls;

        @PostConstruct
        void init() {
            postConstructCalls++;
        }
    }

    @Service
    public static class Helper {
        int postConstructCalls;

        @PostConstruct
        void init() {
            postConstructCalls++;
        }
    }

    @Service
    public static class AnnotatedService {}

    public interface Bound {}

    @Service(binds = Bound.class)
    public static class BoundService implements Bound {}

    static class ShapeBeans {
        @Bean
        Shape shape() {
            return new Square();
        }
    }

    static class OtherShapeBeans {
        @Bean
        Shape anotherShape() {
            return new Square();
        }
    }

    static class HelperPassthroughBeans {
        @Bean
        Object helperAsObject(Helper helper) {
            return helper;
        }
    }

    static class ServiceConflictBeans {
        @Bean
        AnnotatedService service() {
            return new AnnotatedService();
        }
    }

    static class BindsConflictBeans {
        @Bean
        Bound bound() {
            return new BoundService();
        }
    }

    static class NullBeans {
        @Bean
        Shape shape() {
            return null;
        }
    }

    static class VoidBeans {
        @Bean
        void nothing() {}
    }

    static class StaticBeans {
        @Bean
        static Shape shape() {
            return new Square();
        }
    }

    public interface A {}
    public interface B {}

    static class CircularBeans {
        @Bean
        A a(B b) {
            return new A() {};
        }

        @Bean
        B b(A a) {
            return new B() {};
        }
    }

    @Test
    void discoversConfigurationsAndCreatesBeansLazily() {
        ServiceRegistry discovering = new ServiceRegistry(plugin, true, "com.zetaplugins.zetacore.di.beans.discovered");
        discovering.registerConfigurations();
        assertEquals(0, DiscoveredBeans.lazyCreated);

        DiscoveredBeans.Storage storage = discovering.getOrCreate(DiscoveredBeans.Storage.class);

        assertEquals(1, DiscoveredBeans.lazyCreated);
        assertSame(plugin, storage.plugin());
        assertSame(discovering.getOrCreate(DiscoveredBeans.Dependency.class), storage.dependency());
        assertSame(storage, discovering.getOrCreate(DiscoveredBeans.Storage.class));
        assertEquals(1, DiscoveredBeans.lazyCreated);
    }

    @Test
    void createsEagerBeansOnInitialization() {
        ServiceRegistry discovering = new ServiceRegistry(plugin, true, "com.zetaplugins.zetacore.di.beans.discovered");
        discovering.registerConfigurations();
        discovering.initializeEagerServices();

        assertEquals(1, DiscoveredBeans.eagerCreated);
        assertEquals(0, DiscoveredBeans.lazyCreated);
    }

    @Test
    void injectsBeanResult() {
        registry.registerConfiguration(ShapeBeans.class);
        Square square = (Square) registry.getOrCreate(Shape.class);

        assertSame(registry.getOrCreate(Helper.class), square.helper);
        assertEquals(1, square.postConstructCalls);
    }

    @Test
    void doesNotReinitializeAlreadyManagedResult() {
        registry.registerConfiguration(HelperPassthroughBeans.class);
        Helper helper = registry.getOrCreate(Helper.class);

        assertSame(helper, registry.getOrCreate(Object.class));
        assertEquals(1, helper.postConstructCalls);
    }

    @Test
    void supportsStaticBeanMethods() {
        registry.registerConfiguration(StaticBeans.class);
        assertInstanceOf(Square.class, registry.getOrCreate(Shape.class));
    }

    @Test
    void registeringSameConfigurationTwiceIsIgnored() {
        registry.registerConfiguration(ShapeBeans.class);
        assertDoesNotThrow(() -> registry.registerConfiguration(ShapeBeans.class));
    }

    @Test
    void rejectsTwoBeansForSameType() {
        registry.registerConfiguration(ShapeBeans.class);
        ServiceException e = assertThrows(ServiceException.class, () -> registry.registerConfiguration(OtherShapeBeans.class));
        assertTrue(e.getMessage().contains("ShapeBeans#shape()"));
        assertTrue(e.getMessage().contains("OtherShapeBeans#anotherShape()"));
    }

    @Test
    void rejectsBeanForServiceType() {
        ServiceException e = assertThrows(ServiceException.class, () -> registry.registerConfiguration(ServiceConflictBeans.class));
        assertTrue(e.getMessage().contains("@Service"));
    }

    @Test
    void rejectsBeanForTypeBoundByService() {
        ServiceException e = assertThrows(ServiceException.class, () -> registry.registerConfiguration(BindsConflictBeans.class));
        assertTrue(e.getMessage().contains("BoundService"));
    }

    @Test
    void rejectsBeanForRegisteredInstance() {
        registry.registerInstance(Shape.class, new Square());
        assertThrows(ServiceException.class, () -> registry.registerConfiguration(ShapeBeans.class));
    }

    @Test
    void rejectsInstanceForBeanType() {
        registry.registerConfiguration(ShapeBeans.class);
        assertThrows(ServiceException.class, () -> registry.registerInstance(Shape.class, new Square()));
    }

    @Test
    void rejectsNullResult() {
        registry.registerConfiguration(NullBeans.class);
        ServiceException e = assertThrows(ServiceException.class, () -> registry.getOrCreate(Shape.class));
        assertTrue(e.getMessage().contains("returned null"));
    }

    @Test
    void rejectsVoidBeanMethod() {
        assertThrows(ServiceException.class, () -> registry.registerConfiguration(VoidBeans.class));
    }

    @Test
    void detectsCircularBeans() {
        registry.registerConfiguration(CircularBeans.class);
        ServiceException e = assertThrows(ServiceException.class, () -> registry.getOrCreate(A.class));
        assertTrue(e.getMessage().contains("Circular dependency"));
    }
}
