package com.zetaplugins.pluginTest;

import com.zetaplugins.zetacore.ZetaCorePlugin;
import com.zetaplugins.zetacore.debug.ReportDataCollector;
import com.zetaplugins.zetacore.debug.ReportFileWriter;
import com.zetaplugins.zetacore.debug.data.DebugReport;
import com.zetaplugins.zetacore.services.EventRegistrar;
import com.zetaplugins.zetacore.services.localization.BukkitLocalizationService;
import com.zetaplugins.zetacore.services.messages.AdventureMessenger;
import com.zetaplugins.zetacore.services.messages.Messenger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class PluginTest extends ZetaCorePlugin {

    private Messenger messenger;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        // getLogger().info("Config:\n"+ getConfig().saveToString());

        var localizationService = new BukkitLocalizationService(this, new ArrayList<>(List.of("en-US")));
        messenger = new AdventureMessenger(localizationService);

        new CommandManager(this).registerCommands();

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
