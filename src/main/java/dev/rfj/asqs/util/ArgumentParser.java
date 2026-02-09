package dev.rfj.asqs.util;

import dev.rfj.asqs.scans.AbstractScan;
import dev.rfj.asqs.scans.impl.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ArgumentParser {

    private static final Map<String, AbstractScan> SCANS_MAP = createScanMap();
    public static final String DEFAULT_PATTERN = "*.apk";
    private static final List<String> DEFAULT_SCAN_KEYS = Arrays.asList(
            "allows-cleartext-traffic",
            "network-security-config",
            "uses-certificate-pinning",
            "contains-suspected-firmware"
    );

    private static Map<String, AbstractScan> createScanMap() {
        Map<String, AbstractScan> scanMap = new HashMap<>();

        scanMap.put("allows-cleartext-traffic", new AllowsCleartextTraffic());
        scanMap.put("network-security-config", new SpecifiesNetworkSecurityConfig());
        scanMap.put("uses-certificate-pinning", new UsesCertificatePinning());
        scanMap.put("contains-suspected-firmware", new ContainsSuspectedFirmwareFile());
        scanMap.put("print-android-manifest", new PrintAndroidManifest());
        scanMap.put("print-file-report", new PrintFileReport());
        scanMap.put("print-file-entropy", new PrintFileReport(true));
        scanMap.put("print-zip-entries", new PrintZipEntries());
        scanMap.put("extract-suspected-firmware", new ExtractSuspectedFirmwareFiles());
        scanMap.put("uses-react-native", new UsesReactNative());

        return scanMap;
    }

    private ArgumentParser() {
    }

    public static ApplicationConfig parse(String[] args) {
        for (String arg : args) {
            if ("-h".equalsIgnoreCase(arg)) {
                return ApplicationConfig.shouldPrintUsageAndExit();
            }
        }

        ApplicationConfig config = ApplicationConfig.emptyConfig();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg == null || arg.isEmpty()) {
                continue;
            }

            if (arg.startsWith("--scan-")) {
                String scanKey = arg.substring("--scan-".length());
                config = config.withAddedScans(SCANS_MAP.get(scanKey));
                continue;
            }

            if (arg.startsWith("--log-")) {
                String logLevel = arg.substring("--log-".length());
                if ("error".equalsIgnoreCase(logLevel)) {
                    config = config.withLogLevel(Level.SEVERE);
                } else if ("warn".equalsIgnoreCase(logLevel)) {
                    config = config.withLogLevel(Level.WARNING);
                } else if ("debug".equalsIgnoreCase(logLevel)) {
                    config = config.withLogLevel(Level.FINE);
                } else if ("trace".equalsIgnoreCase(logLevel)) {
                    config = config.withLogLevel(Level.FINEST);
                }
                continue;
            }

            if ("-o".equalsIgnoreCase(arg)) {
                String outputFileName = args[++i];
                config = config.withOutputFileName(outputFileName);
                continue;
            }

            if (arg.startsWith("-")) {
                System.err.println("unrecognized option: " + arg);
                return ApplicationConfig.shouldPrintUsageAndExit();
            }

            config = config.withAddedPattern(arg);
        }

        if (config.scans.length == 0) {
            for (String defaultScanKey : DEFAULT_SCAN_KEYS) {
                config = config.withAddedScans(SCANS_MAP.get(defaultScanKey));
            }
        }

        if (config.filePatterns.length == 0) {
            config = config.withAddedPattern(DEFAULT_PATTERN);
        }

        return config;
    }
}
