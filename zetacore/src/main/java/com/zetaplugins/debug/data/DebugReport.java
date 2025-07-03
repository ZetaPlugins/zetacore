package com.zetaplugins.debug.data;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a debug report containing various system and plugin information.
 * @param timestamp the time the report was generated, in milliseconds since epoch
 * @param pluginName the name of the plugin generating the report
 * @param pluginVersion the version of the plugin generating the report
 * @param pluginHash the hash of the plugin generating the report
 * @param minecraftVersion the version of Minecraft the server is running
 * @param javaVersion the version of Java the server is running
 * @param serverSoftware the software the server is running (e.g., Paper, Spigot)
 * @param osName the name of the operating system the server is running on
 * @param osVersion the version of the operating system the server is running on
 * @param latestLogs the latest log file from the server
 * @param installedPlugins a set of installed plugins, each represented by an {@link InstalledPlugin} object
 * @param configurations a map of configuration settings, where the key is the configuration file name and the value is the configuration as a string
 */
public record DebugReport(
        long timestamp,
        String pluginName,
        String pluginVersion,
        String pluginHash,
        String minecraftVersion,
        String javaVersion,
        String serverSoftware,
        String osName,
        String osVersion,
        String latestLogs,
        Set<InstalledPlugin> installedPlugins,
        Map<String, String> configurations
) implements JsonSeriaizable {
    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("timestamp", timestamp);
        json.put("pluginName", pluginName);
        json.put("pluginVersion", pluginVersion);
        json.put("pluginHash", pluginHash);
        json.put("minecraftVersion", minecraftVersion);
        json.put("javaVersion", javaVersion);
        json.put("serverSoftware", serverSoftware);
        json.put("osName", osName);
        json.put("osVersion", osVersion);
        json.put("latestLogs", latestLogs);

        JSONArray pluginsArray = new JSONArray();
        for (InstalledPlugin plugin : installedPlugins) {
            if (plugin != null) pluginsArray.add(((JsonSeriaizable) plugin).toJson());
        }
        json.put("installedPlugins", pluginsArray);

        JSONObject configJson = new JSONObject();
        if (configurations != null) configJson.putAll(configurations);
        json.put("configurations", configJson);

        return json;
    }

    public String toJsonString() {
        return toJson().toString();
    }

    public String toReadableText() {
        StringBuilder res = new StringBuilder();
        res.append("---- LifeStealZ Debug Dump ----").append("\n");
        res.append("This is an automatically generated debug report").append("\n");
        res.append("Timestamp: ").append(timestamp).append("\n\n");
        res.append("-- Plugin Details --").append("\n");
        res.append("Plugin Name: ").append(pluginName).append("\n");
        res.append("Plugin Version: ").append(pluginVersion).append("\n");
        res.append("Plugin Hash: ").append(pluginHash).append("\n");
        res.append("Minecraft Version: ").append(minecraftVersion).append("\n");
        res.append("Java Version: ").append(javaVersion).append("\n");
        res.append("Server Software: ").append(serverSoftware).append("\n");
        res.append("OS: ").append(osName).append(" ").append(osVersion).append("\n\n");

        res.append("-- Installed Plugins --").append("\n");
        if (installedPlugins.isEmpty()) {
            res.append("No plugins installed.").append("\n");
        } else {
            for (InstalledPlugin plugin : installedPlugins) {
                res.append(plugin.toString()).append("\n");
            }
        }

        res.append("\n-- Configurations --").append("\n");
        if (configurations == null || configurations.isEmpty()) {
            res.append("No configurations found.").append("\n");
        } else {
            for (Map.Entry<String, String> entry : configurations.entrySet()) {
                res.append(entry.getKey()).append(": ").append("\n---").append(entry.getValue()).append("\n---").append("\n");
            }
        }
        res.append("\n");

        res.append("-- Latest Logs --").append("\n");
        res.append(latestLogs).append("\n\n");

        res.append("---- End of Debug Dump ----").append("\n");

        return res.toString();
    }

    @Override
    public String toString() {
        return "DebugData{" +
                "timestamp=" + timestamp +
                ", pluginName='" + pluginName + '\'' +
                ", pluginVersion='" + pluginVersion + '\'' +
                ", pluginHash='" + pluginHash + '\'' +
                ", minecraftVersion='" + minecraftVersion + '\'' +
                ", javaVersion='" + javaVersion + '\'' +
                ", serverSoftware='" + serverSoftware + '\'' +
                ", osName='" + osName + '\'' +
                ", osVersion='" + osVersion + '\'' +
                ", latestLogs='" + latestLogs + '\'' +
                ", installedPlugins=[" + installedPlugins.stream().map(InstalledPlugin::toString).collect(Collectors.joining(", ")) + "]" +
                ", configurations=" + configurations +
                '}';
    }
}