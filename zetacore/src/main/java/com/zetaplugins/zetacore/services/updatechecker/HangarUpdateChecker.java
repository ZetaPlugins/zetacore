package com.zetaplugins.zetacore.services.updatechecker;

import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class HangarUpdateChecker extends UpdateChecker {
    private final String slugOrId;
    private final String ownerString;

    /**
     * Constructs a HangarUpdateChecker for the given plugin and Hangar slug or ID.
     * @param plugin The JavaPlugin instance
     * @param ownerString The Hangar owner string
     * @param slugOrId Hangar project slug or ID
     */
    public HangarUpdateChecker(JavaPlugin plugin, String ownerString, String slugOrId) {
        super(plugin);
        this.ownerString = ownerString;
        this.slugOrId = slugOrId;
    }

    @Override
    public void checkForUpdates(boolean logMessage) {
        String latestVersion = fetchLatestVersion();
        if (latestVersion == null) return;

        String currentVersion = getPlugin().getDescription().getVersion();
        if (hangarVersionIsNewer(latestVersion, currentVersion)) {
            setNewVersionAvailable(true);
            setLatestVersion(latestVersion);

            getLogger().info(getNewVersionConsoleMessage(
                    latestVersion,
                    currentVersion,
                    getVersionUrl(latestVersion)
            ));
        } else {
            setNewVersionAvailable(false);
        }
    }

    @Override
    public String getLatestVersionUrl() {
        return getVersionUrl(getLatestVersion());
    }

    public String getVersionUrl(String version) {
        return "https://hangar.papermc.io/" + ownerString + "/" + slugOrId + "/versions/" + version;
    }

    private boolean hangarVersionIsNewer(String latestVersion, String currentVersion) {
        try {
            SemanticVersion latest = new SemanticVersion(latestVersion);
            SemanticVersion current = new SemanticVersion(currentVersion);
            return latest.isGreaterThan(current);
        } catch (IllegalArgumentException e) {
            // fallback to string comparison if parsing fails
            return !latestVersion.trim().equals(currentVersion.trim());
        }
    }

    private String fetchLatestVersion() {
        String mcVersion = getPlugin().getServer().getMinecraftVersion();
        String encodedGameVersion = URLEncoder.encode(mcVersion, StandardCharsets.UTF_8);

        String versionsUrl = "https://hangar.papermc.io/api/v1/projects/" + slugOrId + "/versions?platform=PAPER&platformVersion=" + encodedGameVersion;

        JSONObject json = fetchJsonObjectFromUrl(versionsUrl);
        if (json == null) return null;

        JSONArray results = (JSONArray) json.get("result");
        if (results == null || results.isEmpty()) return null;

        JSONObject latest = (JSONObject) results.get(0);
        return (String) latest.get("name");
    }

    private JSONObject fetchJsonObjectFromUrl(String urlString) {
        try {
            HttpURLConnection connection = createHttpConnection(urlString);
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String response = readResponse(connection);
                return (JSONObject) new JSONParser().parse(response);
            } else {
                getLogger().warning("Failed to retrieve data from " + urlString +
                        " Response code: " + connection.getResponseCode());
            }
        } catch (IOException | org.json.simple.parser.ParseException e) {
            getLogger().warning("Error fetching data: " + e.getMessage());
        }
        return null;
    }

    /**
     * Creates an HTTP connection to the specified URL.
     * @param urlString The URL to connect to.
     * @return An HttpURLConnection object for the specified URL.
     * @throws IOException If an I/O error occurs while opening the connection.
     */
    private HttpURLConnection createHttpConnection(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        return connection;
    }

    /**
     * Reads the response from the given HttpURLConnection.
     * @param connection The HttpURLConnection to read the response from.
     * @return The response as a String.
     * @throws IOException If an I/O error occurs while reading the response.
     */
    private String readResponse(HttpURLConnection connection) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
}
