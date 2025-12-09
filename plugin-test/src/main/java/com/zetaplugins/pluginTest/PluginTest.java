package com.zetaplugins.pluginTest;

import com.zetaplugins.zetacore.ZetaCorePlugin;
import com.zetaplugins.zetacore.debug.command.DebugCommandHandler;
import com.zetaplugins.zetacore.services.bStats.Metrics;
import com.zetaplugins.zetacore.services.commands.AutoCommandRegistrar;
import com.zetaplugins.zetacore.services.di.ManagerRegistry;
import com.zetaplugins.zetacore.services.events.AutoEventRegistrar;
import com.zetaplugins.zetacore.services.events.ManagerRegistryBuilder;
import com.zetaplugins.zetacore.services.localization.BukkitLocalizationService;
import com.zetaplugins.zetacore.services.messages.AdventureMessenger;
import com.zetaplugins.zetacore.services.messages.Messenger;
import com.zetaplugins.zetacore.services.papi.PapiExpansionService;
import com.zetaplugins.zetacore.services.updatechecker.GitHubUpdateChecker;
import com.zetaplugins.zetacore.services.updatechecker.HangarUpdateChecker;
import com.zetaplugins.zetacore.services.updatechecker.UpdateChecker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PluginTest extends ZetaCorePlugin {
    private static final String PACKAGE_PREFIX = "com.zetaplugins.pluginTest";

    private Messenger messenger;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        // getLogger().info("Config:\n"+ getConfig().saveToString());

        // Localization and Messenger setup
        var localizationService = new BukkitLocalizationService(this, new ArrayList<>(List.of("en-US")));
        messenger = new AdventureMessenger(localizationService);

        // Dependency Injection and Manager Registry setup
        var managerRegistry = new ManagerRegistryBuilder()
                .setPlugin(this)
                .setPackagePrefix(PACKAGE_PREFIX)
                .setRequireManagerAnnotation(true)
                .build();
        managerRegistry.initializeEagerManagers();
        System.out.println("Initialized Managers!");

        // Event and Command Registration
        new AutoEventRegistrar(this, PACKAGE_PREFIX, managerRegistry).registerAllListeners();
        var cmdRegistrar = new AutoCommandRegistrar.Builder()
                .setPlugin(this)
                .setPackagePrefix(PACKAGE_PREFIX)
                .setManagerRegistry(managerRegistry)
                .build();
        var commands = cmdRegistrar.registerAllCommands();
        cmdRegistrar.registerCommand("count", new CountCommand(this));
        getLogger().info("Registered commands: " + String.join(", ", commands));
        Map<String, String> configs = new HashMap<>();
        configs.put("config.yml", getConfig().saveToString());
        cmdRegistrar.registerCommand("testpldebug", new DebugCommandHandler("MODRINTHID", this, getPluginFile(), "testplugin.debug", configs, getMessenger()));

        // bStats Metrics
        var metrics = createBStatsMetrics(0);
        metrics.addCustomChart(new Metrics.SimplePie("example_chart", () -> "example_value"));

        // Update Checker
        UpdateChecker mr = new HangarUpdateChecker(this, "KartoffelChipss", "EssentialZ");
        mr.checkForUpdates(true);
        if (mr.isNewVersionAvailable()) getLogger().info("A new version is available: " + mr.getLatestVersion());

        // PlaceholderAPI Expansion Registration
        boolean papiSuccess = new PapiExpansionService(this)
                .setAuthor("ZetaPlugins")
                .addPlaceholder("example", (player, args) -> "ExampleValue for " + player.getName())
                .addAnnotatedPlaceholders(managerRegistry.getOrCreate(CountPlaceholders.class))
                .register();
        getLogger().info("PAPI expansion registration successful: " + papiSuccess);

//        DebugReport debugReport = ReportDataCollector.collect(
//                "MODRINTHID",
//                this,
//                getFile(),
//                new HashMap<>() {{
//                    put("config.yml", getConfig().saveToString());
//                }}
//        );

//        try {
//            ReportFileWriter.writeJsonReportToFile(debugReport, new File(getDataFolder(), "debug-report.json"));
//            ReportFileWriter.writeTextReportToFile(debugReport, new File(getDataFolder(), "debug-report.txt"));
//            getLogger().info("Debug report generated successfully.");
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

//        String url = MclogsReportUploader.uploadReport(debugReport, this);
//        getLogger().info("Debug report uploaded to: " + url);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public Messenger getMessenger() {
        return messenger;
    }
}
