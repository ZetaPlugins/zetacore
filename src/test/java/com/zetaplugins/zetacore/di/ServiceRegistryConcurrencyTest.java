package com.zetaplugins.zetacore.di;

import com.zetaplugins.zetacore.di.annotation.Inject;
import com.zetaplugins.zetacore.di.annotation.PostConstruct;
import com.zetaplugins.zetacore.scan.ClassScanner;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ServiceRegistryConcurrencyTest {

    @Mock
    private JavaPlugin plugin;

    private ServiceRegistry registry;

    @BeforeEach
    void setUp() {
        ClassScanner scanner = new ClassScanner("com.zetaplugins.zetacore.di.nonexistent", getClass().getClassLoader());
        registry = new ServiceRegistry(plugin, false, scanner);
        SlowService.constructed.set(0);
        FailingService.constructed.set(0);
    }

    static class SlowService {
        static final AtomicInteger constructed = new AtomicInteger();
        @Inject Dependency dependency;
        volatile boolean initialized;

        SlowService() throws InterruptedException {
            constructed.incrementAndGet();
            Thread.sleep(20);
        }

        @PostConstruct
        void init() throws InterruptedException {
            Thread.sleep(20);
            initialized = true;
        }
    }

    static class Dependency {}

    static class FailingService {
        static final AtomicInteger constructed = new AtomicInteger();
        @Inject Dependency dependency;

        FailingService() {
            constructed.incrementAndGet();
        }

        @PostConstruct
        void init() {
            throw new IllegalStateException("boom");
        }
    }

    @Test
    void concurrentLookupsCreateSingletonOnceAndOnlySeeInitializedInstances() throws Exception {
        int threads = 16;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<SlowService>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    return registry.getOrCreate(SlowService.class);
                }));
            }
            start.countDown();

            SlowService first = futures.get(0).get(5, TimeUnit.SECONDS);
            for (Future<SlowService> future : futures) {
                SlowService service = future.get(5, TimeUnit.SECONDS);
                assertSame(first, service);
                assertTrue(service.initialized);
                assertNotNull(service.dependency);
            }
            assertEquals(1, SlowService.constructed.get());
            assertSame(first.dependency, registry.getOrCreate(Dependency.class));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void failedCreationIsNotPublished() {
        assertThrows(ServiceException.class, () -> registry.getOrCreate(FailingService.class));
        assertThrows(ServiceException.class, () -> registry.getOrCreate(FailingService.class));
        assertEquals(2, FailingService.constructed.get());
    }
}
