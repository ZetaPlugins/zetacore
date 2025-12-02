package com.zetaplugins.zetacore.services.localization;

import com.zetaplugins.zetacore.annotations.Manager;

import java.util.List;

@Manager
public interface LocalizationService {
    /**
     * Get a string from the language file
     * @param key The key to get the string for
     * @return The string from the language file
     */
    String getString(String key);

    /**
     * Get a string from the language file with a fallback
     * @param key The key to get the string for
     * @param fallback The fallback string
     * @return The string from the language file or the fallback
     */
    String getString(String key, String fallback);

    /**
     * Get a list of strings from the language file
     * @param key The key to get the list of strings for
     * @return The list of strings from the language file
     */
    List<String> getStringList(String key);
}
