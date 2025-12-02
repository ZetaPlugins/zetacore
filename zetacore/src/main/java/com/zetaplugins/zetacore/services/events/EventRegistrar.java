package com.zetaplugins.zetacore.services.events;

import org.bukkit.event.Listener;

import java.util.List;

/**
 * Service for registering event listeners.
 */
public interface EventRegistrar {
    /**
     * Registers all event listeners.
     * @return A list of names of the registered listeners.
     */
    List<String> registerAllListeners();

    /**
     * Registers one or more event listeners.
     * @param listener The listener(s) to register.
     */
    void registerListener(Listener... listener);
}
