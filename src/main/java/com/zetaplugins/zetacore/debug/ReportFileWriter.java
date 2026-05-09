package com.zetaplugins.zetacore.debug;

import com.zetaplugins.zetacore.debug.data.DebugReport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.Function;

/**
 * ReportFileWriter is responsible for writing a DebugReport to a file.
 */
public final class ReportFileWriter {
    private ReportFileWriter() {}

    /**
     * Writes the provided DebugReport to a file at the specified file.
     * @param report the DebugReport to write
     * @param file the file where the report will be written
     * @throws IOException if an I/O error occurs while writing to the file
     */
    public static void writeJsonReportToFile(DebugReport report, File file) throws IOException {
        writeReportToFile(report, file, DebugReport::toJsonString);
    }

    /**
     * Writes the provided DebugReport to a text file at the specified file.
     * @param report the DebugReport to write
     * @param file the file where the report will be written
     * @throws IOException if an I/O error occurs while writing to the file
     */
    public static void writeTextReportToFile(DebugReport report, File file) throws IOException {
        writeReportToFile(report, file, DebugReport::toReadableText);
    }

    /**
     * Writes the provided DebugReport to a file using a custom formatter.
     * @param report the DebugReport to write
     * @param file the file where the report will be written
     * @param formatter a function that formats the DebugReport into a String
     * @throws IOException if an I/O error occurs while writing to the file
     */
    private static void writeReportToFile(DebugReport report, File file, Function<DebugReport, String> formatter) throws IOException {
        if (report == null) throw new IllegalArgumentException("DebugReport cannot be null");
        if (file == null) throw new IllegalArgumentException("File cannot be null");

        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Failed to create directories for file: " + file.getPath());
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(formatter.apply(report));
        }
    }
}
