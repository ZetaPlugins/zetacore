package com.zetaplugins.pluginTest;

import com.zetaplugins.zetacore.debug.ReportDataCollector;
import com.zetaplugins.zetacore.debug.ReportFileWriter;
import com.zetaplugins.zetacore.debug.data.DebugReport;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public final class PluginTest extends JavaPlugin {

    @Override
    public void onEnable() {
        new CommandManager(this).registerCommands();

        DebugReport debugReport = ReportDataCollector.collect(
                this,
                getFile(),
                new HashMap<>() {{
                    put("config.yml", getConfig().saveToString());
                }}
        );

        try {
            ReportFileWriter.writeJsonReportToFile(debugReport, new File(getDataFolder(), "debug-report.json"));
            ReportFileWriter.writeTextReportToFile(debugReport, new File(getDataFolder(), "debug-report.txt"));
            getLogger().info("Debug report generated successfully.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        String url = MclogsReportUploader.uploadReport(debugReport, this);
//        getLogger().info("Debug report uploaded to: " + url);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public File getPluginFile() {
        return getFile();
    }
}
