package dev.rfj.asqs.util;

import dev.rfj.asqs.scans.AbstractScan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class ApplicationConfig {

    public static final Level DEFAULT_LOG_LEVEL = Level.INFO;
    public static final String DEFAULT_OUTPUT_FILE = "asqs-report.csv";

    public final boolean shouldPrintUsageAndExit;
    public final Level logLevel;
    public final AbstractScan[] scans;
    public final boolean isSingleThreaded;
    public final String[] filePatterns;
    public final String outputFileName;

    public static ApplicationConfig emptyConfig() {
        return new ApplicationConfig(
                false,
                DEFAULT_LOG_LEVEL,
                new AbstractScan[] {},
                false,
                new String[] {},
                DEFAULT_OUTPUT_FILE
        );
    }

    public static ApplicationConfig shouldPrintUsageAndExit() {
        return emptyConfig().withShouldPrintUsageAndExit(true);
    }

    private ApplicationConfig(
            boolean shouldPrintUsageAndExit,
            Level logLevel,
            AbstractScan[] scans,
            boolean isSingleThreaded,
            String[] filePatterns,
            String outputFileName
    ) {
        this.shouldPrintUsageAndExit = shouldPrintUsageAndExit;
        this.logLevel = logLevel;
        this.scans = scans;
        this.isSingleThreaded = isSingleThreaded;
        this.filePatterns = filePatterns;
        this.outputFileName = outputFileName;
    }

    public ApplicationConfig withShouldPrintUsageAndExit(boolean shouldPrintUsageAndExit) {
        return new ApplicationConfig(
                shouldPrintUsageAndExit,
                logLevel,
                scans,
                isSingleThreaded,
                filePatterns,
                outputFileName
        );
    }

    public ApplicationConfig withLogLevel(Level logLevel) {
        return new ApplicationConfig(
                shouldPrintUsageAndExit,
                logLevel,
                scans,
                isSingleThreaded,
                filePatterns,
                outputFileName
        );
    }

    public ApplicationConfig withAddedScans(AbstractScan... additionalScans) {
        List<AbstractScan> newScans = new ArrayList<>(scans.length + additionalScans.length);
        newScans.addAll(Arrays.asList(scans));
        newScans.addAll(Arrays.asList(additionalScans));

        return new ApplicationConfig(
                shouldPrintUsageAndExit,
                logLevel,
                newScans.toArray(new AbstractScan[0]),
                isSingleThreaded,
                filePatterns,
                outputFileName
        );
    }

    public ApplicationConfig withSingleThreaded(boolean isSingleThreaded) {
        return new ApplicationConfig(
                shouldPrintUsageAndExit,
                logLevel,
                scans,
                isSingleThreaded,
                filePatterns,
                outputFileName
        );
    }

    public ApplicationConfig withAddedPattern(String pattern) {
        List<String> newPatterns = new ArrayList<>(filePatterns.length + 1);
        newPatterns.addAll(Arrays.asList(filePatterns));
        newPatterns.add(pattern);

        return new ApplicationConfig(
                shouldPrintUsageAndExit,
                logLevel,
                scans,
                isSingleThreaded,
                newPatterns.toArray(new String[0]),
                outputFileName
        );
    }

    public ApplicationConfig withOutputFileName(String outputFileName) {
        return new ApplicationConfig(
                shouldPrintUsageAndExit,
                logLevel,
                scans,
                isSingleThreaded,
                filePatterns,
                outputFileName
        );
    }
}