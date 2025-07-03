package com.zetaplugins.zetacore.debug.uploader;

import com.zetaplugins.zetacore.debug.data.DebugReport;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Abstract class for uploading debug reports to a remote server.
 */
public abstract class ReportUploader {
    private final JavaPlugin plugin;

    public ReportUploader(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Uploads the report to a remote server.
     * @param debugReport the DebugReport to upload
     * @return the url to access the debug report
     */
    abstract String uploadReport(DebugReport debugReport);

    /**
     * Checks if the upload server is ready
     * @return true if ready, false otherwise
     */
    abstract boolean isReady();

    protected @NotNull HttpURLConnection getHttpURLConnection() throws IOException {
        URL url = new URL("https://api.mclo.gs/1/log");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("User-Agent", getPlugin().getName() + "/" + getPlugin().getDescription().getVersion());
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        return connection;
    }

    protected JavaPlugin getPlugin() {
        return plugin;
    }
}
