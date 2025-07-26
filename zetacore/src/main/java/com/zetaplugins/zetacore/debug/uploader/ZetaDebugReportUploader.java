package com.zetaplugins.zetacore.debug.uploader;

import com.zetaplugins.zetacore.debug.data.DebugReport;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class ZetaDebugReportUploader extends ReportUploader {
    public ZetaDebugReportUploader(JavaPlugin plugin) {
        super(plugin);
    }

    public static String uploadReport(DebugReport debugReport, JavaPlugin plugin) {
        return new ZetaDebugReportUploader(plugin).uploadReport(debugReport);
    }

    @Override
    String uploadReport(DebugReport debugReport) {
        try {
            // https://debug.zetaplugins.com/api/reports/
            String uploadUrl = "http://localhost:3000/api/reports/";
            HttpURLConnection connection = getHttpURLConnection(new URL(uploadUrl));
            connection.setRequestProperty("Content-Type", "application/json");

            JSONObject jsonReport = debugReport.toJson();
            String jsonString = jsonReport.toJSONString();

            // Write JSON to request body
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonString.getBytes(StandardCharsets.UTF_8);
                os.write(input);
            }

            // Read the response
            int status = connection.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK || status == HttpURLConnection.HTTP_CREATED) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder responseBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line);
                    }

                    // Parse JSON response
                    JSONObject jsonResponse = (JSONObject) JSONValue.parse(responseBuilder.toString());

                    String statusStr = (String) jsonResponse.get("status");
                    String link = (String) jsonResponse.get("link");

                    if ("success".equalsIgnoreCase(statusStr)) return link;
                    else {
                        getPlugin().getLogger().warning("Debug report upload failed: " + statusStr);
                        return null;
                    }
                }
            } else if (status == HttpURLConnection.HTTP_BAD_REQUEST) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    getPlugin().getLogger().warning("Debug report upload returned 400 Bad Request: " + errorResponse);
                }
            } else {
                getPlugin().getLogger().log(Level.SEVERE, "Debug report upload HTTP error code: " + status);
            }
        } catch (Exception e) {
            getPlugin().getLogger().log(Level.WARNING, "Failed to upload debug report", e);
        }

        return null;
    }

    @Override
    boolean isReady() {
        return false;
    }
}
