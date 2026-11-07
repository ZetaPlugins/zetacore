package com.zetaplugins.zetacore.di.lifecycle;

import com.zetaplugins.zetacore.di.ServiceRegistry;
import com.zetaplugins.zetacore.di.ServiceScope;
import com.zetaplugins.zetacore.di.annotation.Bean;
import com.zetaplugins.zetacore.di.annotation.Inject;
import com.zetaplugins.zetacore.di.annotation.PreDestroy;
import com.zetaplugins.zetacore.di.annotation.Service;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LifecycleTest {

    static final List<String> events = new ArrayList<>();

    @Mock
    private JavaPlugin plugin;

    private ServiceRegistry registry;
    private final List<LogRecord> logged = new ArrayList<>();

    @BeforeEach
    void setUp() {
        events.clear();
        Logger logger = Logger.getLogger("lifecycle-test");
        logger.setUseParentHandlers(false);
        for (Handler h : logger.getHandlers()) logger.removeHandler(h);
        logger.addHandler(new Handler() {
            @Override public void publish(LogRecord record) { logged.add(record); }
            @Override public void flush() {}
            @Override public void close() {}
        });
        when(plugin.getLogger()).thenReturn(logger);
        registry = new ServiceRegistry(plugin, false, "com.zetaplugins.zetacore.di.lifecycle");
    }

    @Service
    static class Database {
        @PreDestroy
        void stop() {
            events.add("database");
        }
    }

    @Service
    static class Repository {
        Repository(Database database) {}

        @PreDestroy
        void stop() {
            events.add("repository");
        }
    }

    @Service
    static class FieldUser {
        @Inject Database database;

        @PreDestroy
        void stop() {
            events.add("fieldUser");
        }
    }

    static class Connection implements AutoCloseable {
        @Override
        public void close() {
            events.add("connection.close");
        }
    }

    static class AnnotatedClose implements AutoCloseable {
        @PreDestroy
        @Override
        public void close() {
            events.add("annotatedClose");
        }
    }

    @Service
    static class Failing {
        @PreDestroy
        void stop() {
            throw new IllegalStateException("boom");
        }
    }

    @Service(scope = ServiceScope.PROTOTYPE)
    static class Prototype {
        @PreDestroy
        void stop() {
            events.add("prototype");
        }
    }

    static class ConnectionBeans {
        @Bean
        Connection connection() {
            return new Connection();
        }

        @Bean
        Object databaseAsObject(Database database) {
            return database;
        }
    }

    static class Command {
        @PreDestroy
        void stop() {
            events.add("command");
        }
    }

    @Test
    void destroysInReverseCreationOrder() {
        registry.getOrCreate(Repository.class);
        registry.getOrCreate(FieldUser.class);

        registry.shutdown();

        assertEquals(List.of("fieldUser", "repository", "database"), events);
    }

    @Test
    void closesAutoCloseableBeansAndInstances() {
        registry.registerConfiguration(ConnectionBeans.class);
        registry.getOrCreate(Connection.class);
        registry.registerInstance(AnnotatedClose.class, new AnnotatedClose());

        registry.shutdown();

        assertEquals(List.of("annotatedClose", "connection.close"), events);
    }

    @Test
    void destroysPassthroughBeanOnlyOnce() {
        registry.registerConfiguration(ConnectionBeans.class);
        registry.getOrCreate(Object.class);

        registry.shutdown();

        assertEquals(List.of("database"), events);
    }

    @Test
    void continuesAfterErrorsAndLogsThem() {
        registry.getOrCreate(Database.class);
        registry.getOrCreate(Failing.class);

        registry.shutdown();

        assertEquals(List.of("database"), events);
        assertEquals(1, logged.size());
        assertTrue(logged.get(0).getMessage().contains("Failing#stop"));
        assertEquals("boom", logged.get(0).getThrown().getMessage());
    }

    @Test
    void skipsPrototypes() {
        registry.getOrCreate(Prototype.class);
        registry.shutdown();
        assertTrue(events.isEmpty());
    }

    @Test
    void destroysCreatedObjects() {
        registry.create(Command.class);
        registry.shutdown();
        assertEquals(List.of("command"), events);
    }

    @Test
    void secondShutdownDoesNothing() {
        registry.getOrCreate(Database.class);
        registry.shutdown();
        registry.shutdown();
        assertEquals(List.of("database"), events);
    }
}
