package com.zetaplugins.pluginTest;

import com.zetaplugins.zetacore.ZetaCorePlugin;
import com.zetaplugins.zetacore.command.registration.AutoCommandRegistrar;
import com.zetaplugins.zetacore.debug.command.DebugCommandHandler;
import com.zetaplugins.zetacore.di.ServiceRegistry;
import com.zetaplugins.zetacore.event.registration.AutoEventRegistrar;
import com.zetaplugins.zetacore.integration.bstats.Metrics;
import com.zetaplugins.zetacore.integration.papi.PapiExpansionService;
import com.zetaplugins.zetacore.integration.updatechecker.HangarUpdateChecker;
import com.zetaplugins.zetacore.integration.updatechecker.UpdateChecker;
import com.zetaplugins.zetacore.messaging.AdventureMessenger;
import com.zetaplugins.zetacore.messaging.Messenger;
import com.zetaplugins.zetacore.messaging.localization.BukkitLocalizationService;

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
        var serviceRegistry = new ServiceRegistry.Builder()
                .setPlugin(this)
                .setPackagePrefix(PACKAGE_PREFIX)
                .setRequireManagerAnnotation(true)
                .build();
        serviceRegistry.initializeEagerServices();
        System.out.println("Initialized Managers!");

        // Event and Command Registration
        new AutoEventRegistrar(this, PACKAGE_PREFIX, serviceRegistry).registerAllListeners();
        var cmdRegistrar = new AutoCommandRegistrar.Builder()
                .setPlugin(this)
                .setPackagePrefix(PACKAGE_PREFIX)
                .setServiceRegistry(serviceRegistry)
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
                .addAnnotatedPlaceholders(serviceRegistry.getOrCreate(CountPlaceholders.class))
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
