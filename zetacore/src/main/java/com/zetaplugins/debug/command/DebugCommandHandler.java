package com.zetaplugins.debug.command;

import com.zetaplugins.debug.ReportDataCollector;
import com.zetaplugins.debug.ReportFileWriter;
import com.zetaplugins.debug.data.DebugReport;
import com.zetaplugins.debug.uploader.MclogsReportUploader;
import com.zetaplugins.services.MessageService;
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
import java.util.logging.Level;

/**
 * DebugCommandHandler is a command handler for the debug command.
 */
public final class DebugCommandHandler implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final File pluginFile;
    private final String permission;
    private final DebugCommandMessages messages;

    /**
     * Constructor for DebugCommandHandler.
     * @param plugin the JavaPlugin instance
     * @param pluginFile the file of the plugin, used to generate a hash. Can be obtained using JavaPlugin#getFile(} inside a plugin's main class.
     * @param permission the permission required to execute the command
     */
    public DebugCommandHandler(JavaPlugin plugin, File pluginFile, String permission) {
        this(plugin, pluginFile, permission, new DebugCommandMessages());
    }

    /**
     * Constructor for DebugCommandHandler.
     * @param plugin the JavaPlugin instance
     * @param pluginFile the file of the plugin, used to generate a hash. Can be obtained using JavaPlugin#getFile(} inside a plugin's main class.
     * @param permission the permission required to execute the command
     * @param messages the messages used in the command
     */
    public DebugCommandHandler(JavaPlugin plugin, File pluginFile, String permission, DebugCommandMessages messages) {
        this.plugin = plugin;
        this.pluginFile = pluginFile;
        this.permission = permission;
        this.messages = messages;
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
        sender.sendMessage(MessageService.formatMsg(
                usage,
                new MessageService.Replaceable<>("%command%", commandName)
        ));
    }

    private void throwPermissionError(@NotNull CommandSender sender) {
        sender.sendMessage(MessageService.formatMsg(messages.noPermissionMessage()));
    }

    private boolean handleUpload(CommandSender sender, boolean confirmed, String commandName) {
        if (!confirmed) {
            sender.sendMessage(MessageService.formatMsg(
                    messages.uploadConfirmMessage(),
                    new MessageService.Replaceable<>(
                            "%command%",
                            "/" + commandName + " upload confirm"
                    )
            ));
            return true;
        }

        if (!sender.hasPermission(permission)) {
            throwPermissionError(sender);
            return true;
        }

        DebugReport report = ReportDataCollector.collect(plugin, pluginFile, null);
        String url = MclogsReportUploader.uploadReport(report, plugin);

        if (url == null) {
            sender.sendMessage(MessageService.formatMsg(
                    messages.failToUploadMessage(),
                    new MessageService.Replaceable<>("%error%", "Failed to upload report.")
            ));
            plugin.getLogger().log(Level.SEVERE, "Failed to upload debug report.");
            return false;
        }

        String formattedUrl = url.replaceAll("\\\\", "");

        sender.sendMessage(MessageService.formatMsg(
                messages.uploadSuccessMessage(),
                new MessageService.Replaceable<>("%url%", formattedUrl)
        ));
        return true;
    }

    /**
     * Handles the generate command.
     * @param sender the CommandSender who executed the command
     * @return true if the command was handled successfully, false otherwise
     */
    private boolean handleGenerate(CommandSender sender) {
        DebugReport report = ReportDataCollector.collect(plugin, pluginFile, null);
        File reportJson = new File("debug-report.json");
        File reportTxt = new File("debug-report.txt");

        try {
            ReportFileWriter.writeJsonReportToFile(report, reportJson);
            ReportFileWriter.writeTextReportToFile(report, reportTxt);
        } catch (IOException e) {
            sender.sendMessage(MessageService.formatMsg(
                    messages.failedToCreateFileMessage(),
                    new MessageService.Replaceable<>("%error%", e.getMessage())
            ));
            plugin.getLogger().log(Level.SEVERE, "Failed to write debug report", e);
            return false;
        }

        sender.sendMessage(MessageService.formatMsg(
                messages.fileCreateSuccessMessage(),
                new MessageService.Replaceable<>("%jsonPath%", reportJson.getAbsolutePath()),
                new MessageService.Replaceable<>("%txtPath%", reportTxt.getAbsolutePath())
        ));
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
