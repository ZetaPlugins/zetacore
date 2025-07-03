package com.zetaplugins.debug.uploader;

import com.zetaplugins.debug.data.DebugReport;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public final class MclogsReportUploader extends ReportUploader {
    public MclogsReportUploader(JavaPlugin plugin) {
        super(plugin);
    }

    public static String uploadReport(DebugReport debugReport, JavaPlugin plugin) {
        return new MclogsReportUploader(plugin).uploadReport(debugReport);
    }

    @Override
    public String uploadReport(DebugReport debugReport) {
        try {
            HttpURLConnection connection = getHttpURLConnection();
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // Encode content for HTTP request
            String encodedContent = "content=" + URLEncoder.encode(debugReport.toReadableText(), StandardCharsets.UTF_8);

            // Write data to request body
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = encodedContent.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Read response
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine);
                    }

                    return parseMclogsResponse(response.toString());
                }
            } else {
                getPlugin().getLogger().warning("Failed to upload to mclo.gs. Response code: " + responseCode);
            }
        } catch (Exception e) {
            getPlugin().getLogger().log(Level.WARNING, "Failed to upload debug report", e);
        }

        return null;
    }

    /**
     * Extracts the URL from the mclo.gs JSON response.
     * @param jsonResponse The raw JSON response.
     * @return The URL of the uploaded log or null if failed.
     */
    private String parseMclogsResponse(String jsonResponse) {
        if (jsonResponse.contains("\"success\":true") && jsonResponse.contains("\"url\":\"")) {
            int urlStart = jsonResponse.indexOf("\"url\":\"") + 7;
            int urlEnd = jsonResponse.indexOf("\"", urlStart);
            return jsonResponse.substring(urlStart, urlEnd);
        }
        getPlugin().getLogger().warning("Invalid response from mclo.gs: " + jsonResponse);
        return null;
    }

    @Override
    public boolean isReady() {
        // We assume mclo.gs is always ready to accept reports so it can be used as a fallback.
        return true;
    }
}
