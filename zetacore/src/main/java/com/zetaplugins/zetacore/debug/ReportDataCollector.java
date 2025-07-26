package com.zetaplugins.zetacore.debug;

import com.zetaplugins.zetacore.debug.data.DebugReport;
import com.zetaplugins.zetacore.debug.data.InstalledPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * ReportDataCollector is responsible for collecting data for a debug report.
 * It gathers information about the plugin, server, and installed plugins,
 * and generates a {@link DebugReport} object containing this data.
 */
public final class ReportDataCollector {
    private static final int MAX_LOG_LINES = 50_000;

    private final JavaPlugin plugin;
    private final File pluginFile;
    private final Map<String, String> configs;
    private final String modrinthId;

    /**
     * Creates a new ReportDataCollector instance.
     * @param modrinthId the Modrinth ID of the plugin, used to identify the report
     * @param plugin the JavaPlugin instance for which the report is being collected
     * @param pluginFile the file of the plugin, used to generate a hash. Can be obtained using JavaPlugin#getFile(} inside a plugin's main class.
     * @param configs a map of configuration settings, where the key is the configuration file name and the value is the configuration saved as a string
     */
    public ReportDataCollector(String modrinthId, JavaPlugin plugin, File pluginFile, Map<String, String> configs) {
        this.plugin = plugin;
        this.pluginFile = pluginFile;
        this.configs = configs;
        this.modrinthId = modrinthId;
    }

    /**
     * Collects a debug report for the specified plugin.
     * @param plugin the JavaPlugin instance for which the report is being collected
     * @param pluginFile the file of the plugin, used to generate a hash. Can be obtained using JavaPlugin#getFile(} inside a plugin's main class.
     * @param configs a map of configuration settings, where the key is the configuration file name and the value is the configuration saved as a string
     * @return a DebugReport object containing the collected data
     */
    public static DebugReport collect(String modrinthId, JavaPlugin plugin, File pluginFile, Map<String, String> configs) {
        return new ReportDataCollector(modrinthId, plugin, pluginFile, configs).collectReport();
    }

    public DebugReport collectReport() {
        long now = System.currentTimeMillis();

        String pluginName = plugin.getName();
        String pluginVersion = plugin.getDescription().getVersion();
        String pluginHash = generatePluginHash();

        String minecraftVersion = Bukkit.getVersion();
        String serverSoftware = Bukkit.getName();
        String javaVersion = System.getProperty("java.version");
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");

        String latestLogs = getLatestLogs();

        Set<InstalledPlugin> installedPlugins = Arrays.stream(plugin.getServer().getPluginManager().getPlugins())
                .map(p -> new InstalledPlugin(p.getName(), p.getDescription().getVersion(), p.isEnabled()))
                .collect(Collectors.toSet());

        return new DebugReport(
                modrinthId,
                now,
                pluginName,
                pluginVersion,
                pluginHash,
                minecraftVersion,
                javaVersion,
                serverSoftware,
                osName,
                osVersion,
                latestLogs,
                installedPlugins,
                configs
        );
    }

    /**
     * Generates a SHA-256 hash of the plugin file.
     * @return the SHA-256 hash of the plugin file, or "UNKNOWN" if the file does not exist
     */
    private String generatePluginHash() {
        try {
            if (pluginFile == null || !pluginFile.exists()) {
                return "UNKNOWN";
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (FileInputStream fis = new FileInputStream(pluginFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }

            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                hexString.append(String.format("%02x", hashByte));
            }
            return hexString.toString();
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to generate plugin hash", e);
            return "ERROR";
        }
    }

    private File getLogFile() {
        File logFile = new File("logs/latest.log");
        if (!logFile.exists()) {
            // Try to find the log file in a different location for some server types
            File serverDir = new File(".");
            Optional<File> latestLog = Arrays.stream(serverDir.listFiles())
                    .filter(f -> f.isFile() && f.getName().endsWith(".log"))
                    .max(Comparator.comparingLong(File::lastModified));

            if (latestLog.isPresent()) logFile = latestLog.get();
            else return null;
        }

        return logFile;
    }

    /**
     * Synchronously retrieves the latest logs from the server.
     * @return the content of the latest log file, or an error message if it cannot be read
     */
    private String getLatestLogs() {
        File logFile = getLogFile();
        if (logFile == null) return "No log file found";

        try {
            List<String> allLines = Files.readAllLines(logFile.toPath());

            List<String> lastLines = allLines.size() > MAX_LOG_LINES
                    ? allLines.subList(allLines.size() - MAX_LOG_LINES, allLines.size())
                    : allLines;

            StringBuilder builder = new StringBuilder();
            for (String line : lastLines) {
                builder.append(line).append("\n");
            }

            return maskIpAddresses(builder.toString());
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to read latest log file", e);
            return "ERROR";
        }
    }

    private static String maskIpAddresses(String input) {
        String ipRegex = "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b";
        Pattern pattern = Pattern.compile(ipRegex);
        Matcher matcher = pattern.matcher(input);

        // Replace each IP with a masked version
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String ip = matcher.group();
            matcher.appendReplacement(result, "***.***.***.***");
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String maskIpv6Addresses(String input) {
        // regex for most standard/compressed ipv6 adresses
        String ipv6Regex = "\\b(?:[\\da-fA-F]{1,4}:){1,7}[\\da-fA-F]{1,4}\\b|\\b(?:[\\da-fA-F]{1,4}:){1,7}:|::(?:[\\da-fA-F]{1,4}:){0,6}[\\da-fA-F]{1,4}\\b";
        Pattern pattern = Pattern.compile(ipv6Regex);
        Matcher matcher = pattern.matcher(input);

        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(result, "****:****:****:****:****:****:****:****");
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
