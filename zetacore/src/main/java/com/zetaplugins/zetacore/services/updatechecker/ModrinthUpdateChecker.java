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

/**
 * ModrinthUpdateChecker checks for updates of a JavaPlugin by querying the Modrinth API.
 */
public class ModrinthUpdateChecker extends UpdateChecker {
    private final String projectId;

    /**
     * Constructs a ModrinthUpdateChecker for the given plugin and project ID.
     * @param plugin The JavaPlugin to check updates for
     * @param projectId The Modrinth project ID
     */
    public ModrinthUpdateChecker(JavaPlugin plugin, String projectId) {
        super(plugin);
        this.projectId = projectId;
    }

    @Override
    public void checkForUpdates(boolean logMessage) {
        String latestVersion = fetchLatestVersion();
        if (latestVersion == null) return;

        String currentVersion = getPlugin().getDescription().getVersion();
        if (modrinthVersionIsNewer(latestVersion, currentVersion)) {
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

    private String getModrinthProjectUrl() {
        return "https://api.modrinth.com/v2/project/" + projectId;
    }

    public String getVersionUrl(String version) {
        return "https://modrinth.com/plugin/" + projectId +  "/version/" + version;
    }

    private boolean modrinthVersionIsNewer(String latestVersion, String currentVersion) {
        try {
            SemanticVersion latest = new SemanticVersion(latestVersion);
            SemanticVersion current = new SemanticVersion(currentVersion);
            return latest.isGreaterThan(current);
        } catch (IllegalArgumentException e) {
            // fallback to string comparison if parsing fails
            return !latestVersion.trim().equals(currentVersion.trim());
        }
    }

    /**
     * Fetches the latest version of the plugin from Modrinth.
     *
     * @return The latest version number as a String, or null if it could not be fetched.
     */
    private String fetchLatestVersion() {
        String mcVersion = getPlugin().getServer().getMinecraftVersion();
        String encodedGameVersion = URLEncoder.encode("[\"" + mcVersion + "\"]", StandardCharsets.UTF_8);
        String versionsUrl = getModrinthProjectUrl() + "/version?game_versions=" + encodedGameVersion;

        JSONArray versionsArray = fetchJsonArrayFromUrl(versionsUrl);
        if (versionsArray == null || versionsArray.isEmpty()) return null;

        JSONObject latestVersion = (JSONObject) versionsArray.get(0);
        return (String) latestVersion.get("version_number");
    }

    /**
     * Fetches a JSON array from a given URL.
     * @param urlString The URL to fetch the JSON array from.
     * @return A JSONArray containing the parsed JSON data, or null if an error occurs.
     */
    private JSONArray fetchJsonArrayFromUrl(String urlString) {
        try {
            HttpURLConnection connection = createHttpConnection(urlString);
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String response = readResponse(connection);
                return (JSONArray) new JSONParser().parse(response);
            } else {
                getLogger().warning("Failed to retrieve data from " + urlString + " Response code: " + connection.getResponseCode());
            }
        } catch (IOException | org.json.simple.parser.ParseException e) {
            getLogger().warning("Error fetching data: " + e.getMessage());
        }
        return null;
    }

    /**
     * Fetches JSON data from a given URL.
     * @param urlString The URL to fetch the JSON data from.
     * @return A JSONObject containing the parsed JSON data, or null if an error occurs.
     */
    private JSONObject fetchJsonFromUrl(String urlString) {
        try {
            HttpURLConnection connection = createHttpConnection(urlString);
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String response = readResponse(connection);
                return (JSONObject) new JSONParser().parse(response);
            } else {
                getLogger().warning("Failed to retrieve data from " + urlString + " Response code: " + connection.getResponseCode());
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
