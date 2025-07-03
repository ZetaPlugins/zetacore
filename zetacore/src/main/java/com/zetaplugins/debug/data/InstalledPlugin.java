package com.zetaplugins.debug.data;

import org.json.simple.JSONObject;

/**
 * Represents an installed plugin with its name and version.
 * @param pluginName the name of the plugin
 * @param pluginVersion the version of the plugin
 */
public record InstalledPlugin(String pluginName, String pluginVersion, boolean enabled) implements JsonSeriaizable {
    @Override
    public String toString() {
        return pluginName + " v" + pluginVersion + (enabled ? " (enabled)" : " (disabled)");
    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("pluginName", pluginName);
        json.put("pluginVersion", pluginVersion);
        json.put("enabled", enabled);
        return json;
    }
}
