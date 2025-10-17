package com.zetaplugins.zetacore.debug.command;

import com.zetaplugins.zetacore.debug.ReportDataCollector;
import com.zetaplugins.zetacore.debug.ReportFileWriter;
import com.zetaplugins.zetacore.debug.data.DebugReport;
import com.zetaplugins.zetacore.debug.uploader.ZetaDebugReportUploader;
import com.zetaplugins.zetacore.services.messages.Messenger;
import com.zetaplugins.zetacore.services.messages.Replaceable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * DebugCommandHandler is a command handler for the debug command.
 */
public final class DebugCommandHandler implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final File pluginFile;
    private final String permission;
    private final DebugCommandMessages messages;
    private final String modrinthId;
    private final Map<String, String> configs;
    private final Messenger messenger;

    /**
     * Constructor for DebugCommandHandler.
     * @param modrinthId the Modrinth ID of the plugin, used to identify the report
     * @param plugin the JavaPlugin instance
     * @param pluginFile the file of the plugin, used to generate a hash. Can be obtained using JavaPlugin#getFile(} inside a plugin's main class.
     * @param permission the permission required to execute the command
     */
    public DebugCommandHandler(String modrinthId, JavaPlugin plugin, File pluginFile, String permission, Messenger messenger) {
        this(modrinthId, plugin, pluginFile, permission, null, new DebugCommandMessages(), messenger);
    }

    /**
     * Constructor for DebugCommandHandler.
     * @param modrinthId the Modrinth ID of the plugin, used to identify the report
     * @param plugin the JavaPlugin instance
     * @param pluginFile the file of the plugin, used to generate a hash. Can be obtained using JavaPlugin#getFile(} inside a plugin's main class.
     * @param permission the permission required to execute the command
     * @param configs a map of configuration settings, where the key is the configuration file name and the value is the configuration saved as a string
     */
    public DebugCommandHandler(String modrinthId, JavaPlugin plugin, File pluginFile, String permission, Map<String, String> configs, Messenger messenger) {
        this(modrinthId, plugin, pluginFile, permission, configs, new DebugCommandMessages(), messenger);
    }

    /**
     * Constructor for DebugCommandHandler.
     * @param modrinthId the Modrinth ID of the plugin, used to identify the report
     * @param plugin the JavaPlugin instance
     * @param pluginFile the file of the plugin, used to generate a hash. Can be obtained using JavaPlugin#getFile(} inside a plugin's main class.
     * @param permission the permission required to execute the command
     * @param configs a map of configuration settings, where the key is the configuration file name and the value is the configuration saved as a string
     * @param messages the messages used in the command
     */
    public DebugCommandHandler(String modrinthId, JavaPlugin plugin, File pluginFile, String permission, Map<String, String> configs, DebugCommandMessages messages, Messenger messenger) {
        this.plugin = plugin;
        this.pluginFile = pluginFile;
        this.permission = permission;
        this.messages = messages;
        this.modrinthId = modrinthId;
        this.configs = configs;
        this.messenger = messenger;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            throwUsageError(sender, messages.usageMessage(), command.getName());
            return true;
        }

        String subCommand = args[0].toLowerCase();

        return switch (subCommand) {
            case "upload" -> {
                if (!sender.hasPermission(permission)) {
                    throwPermissionError(sender);
                    yield true;
                }
                yield handleUpload(
                        sender,
                        args.length > 1 && args[1].equalsIgnoreCase("confirm"),
                        command.getName()
                );
            }
            case "generate" -> {
                if (!sender.hasPermission(permission)) {
                    throwPermissionError(sender);
                    yield true;
                }
                yield handleGenerate(sender);
            }
            default -> {
                throwUsageError(sender, messages.usageMessage(), command.getName());
                yield true;
            }
        };
    }

    private void throwUsageError(@NotNull CommandSender sender, String usage, String commandName) {
        messenger.sendRaw(
                sender,
                usage,
                new Replaceable<>("%command%", commandName)
        );
    }

    private void throwPermissionError(@NotNull CommandSender sender) {
        messenger.sendRaw(
                sender,
                messages.noPermissionMessage()
        );
    }

    private boolean handleUpload(CommandSender sender, boolean confirmed, String commandName) {
        if (!confirmed) {
            messenger.sendRaw(
                    sender,
                    messages.uploadConfirmMessage(),
                    new Replaceable<>(
                            "%command%",
                            "/" + commandName + " upload confirm"
                    )
            );
            return true;
        }

        if (!sender.hasPermission(permission)) {
            throwPermissionError(sender);
            return true;
        }

        DebugReport report = ReportDataCollector.collect(modrinthId, plugin, pluginFile, configs);
        String url = ZetaDebugReportUploader.uploadReport(report, plugin);

        if (url == null) {
            messenger.sendRaw(
                    sender,
                    messages.failToUploadMessage(),
                    new Replaceable<>("%error%", "Failed to upload report.")
            );
            return false;
        }

        String formattedUrl = url.replaceAll("\\\\", "");

        messenger.sendRaw(
                sender,
                messages.uploadSuccessMessage(),
                new Replaceable<>("%url%", formattedUrl)
        );
        return true;
    }

    /**
     * Handles the generate command.
     * @param sender the CommandSender who executed the command
     * @return true if the command was handled successfully, false otherwise
     */
    private boolean handleGenerate(CommandSender sender) {
        DebugReport report = ReportDataCollector.collect(modrinthId, plugin, pluginFile, configs);
        File reportJson = new File("debug-report.json");
        File reportTxt = new File("debug-report.txt");

        try {
            ReportFileWriter.writeJsonReportToFile(report, reportJson);
            ReportFileWriter.writeTextReportToFile(report, reportTxt);
        } catch (IOException e) {
            messenger.sendRaw(
                    sender,
                    messages.failedToCreateFileMessage(),
                    new Replaceable<>("%error%", e.getMessage())
            );
            plugin.getLogger().log(Level.SEVERE, "Failed to write debug report", e);
            return false;
        }

        messenger.sendRaw(
                sender,
                messages.fileCreateSuccessMessage(),
                new Replaceable<>("%jsonPath%", reportJson.getAbsolutePath()),
                new Replaceable<>("%txtPath%", reportTxt.getAbsolutePath())
        );
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            return List.of("upload", "generate");
        }
        return null;
    }
}
