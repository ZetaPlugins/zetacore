package com.zetaplugins.zetacore.di.beans.discovered;

import com.zetaplugins.zetacore.di.annotation.Bean;
import com.zetaplugins.zetacore.di.annotation.Configuration;
import com.zetaplugins.zetacore.di.annotation.Service;
import org.bukkit.plugin.java.JavaPlugin;

@Configuration
public class DiscoveredBeans {
    public static int lazyCreated;
    public static int eagerCreated;

    public interface Storage {
        JavaPlugin plugin();
        Dependency dependency();
    }

    public record SimpleStorage(JavaPlugin plugin, Dependency dependency) implements Storage {}

    @Service
    public static class Dependency {}

    public static class EagerThing {}

    @Bean
    public Storage storage(JavaPlugin plugin, Dependency dependency) {
        lazyCreated++;
        return new SimpleStorage(plugin, dependency);
    }

    @Bean(eagerlyLoad = true)
    public EagerThing eagerThing() {
        eagerCreated++;
        return new EagerThing();
    }
}
