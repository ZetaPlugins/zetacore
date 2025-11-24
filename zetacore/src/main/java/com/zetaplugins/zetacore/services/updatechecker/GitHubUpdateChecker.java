package com.zetaplugins.zetacore.services.updatechecker;

import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Checks for updates of a plugin by querying the GitHub Releases API.
 */
public class GitHubUpdateChecker extends UpdateChecker {
    private final String repoOwner;
    private final String repoName;

    /**
     * Constructs a GitHubUpdateChecker for the given plugin and GitHub repository.
     * @param plugin The JavaPlugin instance
     * @param repoOwner GitHub username or organization
     * @param repoName Repository name
     */
    public GitHubUpdateChecker(JavaPlugin plugin, String repoOwner, String repoName) {
        super(plugin);
        this.repoOwner = repoOwner;
        this.repoName = repoName;
    }

    @Override
    public void checkForUpdates(boolean logMessage) {
        String latestTag = fetchLatestReleaseTag();
        if (latestTag == null) return;

        String currentVersion = getPlugin().getDescription().getVersion();

        if (githubVersionIsNewer(latestTag, currentVersion)) {
            setNewVersionAvailable(true);
            setLatestVersion(latestTag);

            getLogger().info(getNewVersionConsoleMessage(
                    latestTag,
                    currentVersion,
                    getLatestReleaseUrl()
            ));
        } else {
            setNewVersionAvailable(false);
        }
    }

    private String getReleasesApiUrl() {
        return "https://api.github.com/repos/" + repoOwner + "/" + repoName + "/releases/latest";
    }

    private String getLatestReleaseUrl() {
        return "https://github.com/" + repoOwner + "/" + repoName + "/releases/latest";
    }

    /**
     * Use the tag name as version.
     */
    private boolean githubVersionIsNewer(String latest, String current) {
        try {
            SemanticVersion latestSem = new SemanticVersion(latest);
            SemanticVersion currentSem = new SemanticVersion(current);
            return latestSem.isGreaterThan(currentSem);
        } catch (IllegalArgumentException e) {
            return !latest.trim().equals(current.trim());
        }
    }

    /**
     * Fetches the latest GitHub release and returns the tag name.
     */
    private String fetchLatestReleaseTag() {
        try {
            HttpURLConnection connection = createHttpConnection(getReleasesApiUrl());

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String response = readResponse(connection);

                JSONObject releaseJson = (JSONObject) new JSONParser().parse(response);

                return (String) releaseJson.get("tag_name");
            } else {
                getLogger().warning("GitHub API request failed: " + connection.getResponseCode());
            }
        } catch (Exception e) {
            getLogger().warning("Error fetching GitHub release: " + e.getMessage());
        }

        return null;
    }

    private HttpURLConnection createHttpConnection(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0"); // GitHub requires UA header

        return connection;
    }

    private String readResponse(HttpURLConnection connection) throws IOException {
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            return response.toString();
        }
    }
}