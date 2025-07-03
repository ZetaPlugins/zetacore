package com.zetaplugins.zetacore.debug.data;

import org.json.simple.JSONObject;

/**
 * Interface for objects that can be serialized to JSON.
 */
public interface JsonSeriaizable {
    /**
     * Converts the implementing object to a JSON representation.
     * @return a JSONObject representing the object
     */
    JSONObject toJson();
}
