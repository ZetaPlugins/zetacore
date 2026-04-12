package com.zetaplugins.pluginTest;

import java.util.UUID;

public interface GenericCountService {
    void incrementCounter(UUID playerId);

    int getCounter(UUID playerId);
}
