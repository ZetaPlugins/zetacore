package com.zetaplugins.zetacore.services.papi;

import org.bukkit.OfflinePlayer;

@FunctionalInterface
public interface PlaceholderFunction {
    /**
     * Applies the placeholder function to the given arguments.
     * @param player The offline player for whom the placeholder is being applied
     * @param identifier The identifier of the placeholder
     * @return The result of the placeholder function
     */
    String apply(OfflinePlayer player, String identifier);
}
