package com.zetaplugins.zetacore.debug.command;

import com.zetaplugins.zetacore.ZetaCorePlugin;
import com.zetaplugins.zetacore.messaging.Messenger;
import com.zetaplugins.zetacore.permission.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;

public final class DebugCommandHandlerBuilder {
    private JavaPlugin plugin;
    private File pluginFile;
    private String permission = "zetacore.debug";
    private DebugCommandMessages messages = new DebugCommandMessages();
    private String modrinthId;
    private Map<String, String> configs;
    private Messenger messenger;

    public DebugCommandHandlerBuilder withPlugin(JavaPlugin plugin) {
        if (plugin instanceof ZetaCorePlugin) this.pluginFile = ((ZetaCorePlugin) plugin).getPluginFile();
        this.plugin = plugin;
        return this;
    }

    public DebugCommandHandlerBuilder withPluginFile(File pluginFile) {
        this.pluginFile = pluginFile;
        return this;
    }

    public DebugCommandHandlerBuilder withPermission(String permission) {
        this.permission = permission;
        return this;
    }

    public DebugCommandHandlerBuilder withPermission(Permission permission) {
        this.permission = permission.getValue();
        return this;
    }

    public DebugCommandHandlerBuilder withMessages(DebugCommandMessages messages) {
        this.messages = messages;
        return this;
    }

    public DebugCommandHandlerBuilder withModrinthId(String modrinthId) {
        this.modrinthId = modrinthId;
        return this;
    }

    public DebugCommandHandlerBuilder withConfigs(Map<String, String> configs) {
        this.configs = configs;
        return this;
    }

    public DebugCommandHandlerBuilder withMessenger(Messenger messenger) {
        this.messenger = messenger;
        return this;
    }

    public DebugCommandHandler build() {
        if (plugin == null) {
            throw new IllegalStateException("Plugin must be set");
        }
        if (pluginFile == null) {
            throw new IllegalStateException("Plugin file must be set");
        }
        if (modrinthId == null || modrinthId.isEmpty()) {
            throw new IllegalStateException("Modrinth ID must be set");
        }
        if (messenger == null) {
            throw new IllegalStateException("Messagenger must be set");
        }
        return new DebugCommandHandler(modrinthId, plugin, pluginFile, permission, configs, messages, messenger);
    }
}
