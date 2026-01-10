package dev.rfj.asqs.util;

import dev.rfj.asqs.scans.AbstractScan;
import dev.rfj.asqs.scans.impl.*;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;

import static dev.rfj.asqs.util.ApplicationConfig.DEFAULT_LOG_LEVEL;
import static dev.rfj.asqs.util.ArgumentParser.DEFAULT_PATTERN;
import static org.junit.jupiter.api.Assertions.*;

class ArgumentParserTest {

    private ApplicationConfig parse(String... args) {
        return ArgumentParser.parse(args);
    }

    @Test
    void helpArg_shouldPrintUsage() {
        ApplicationConfig config = parse("-h");
        assertTrue(config.shouldPrintUsageAndExit);
    }

    @Test
    void emptyArgs_shouldResultInDefaultPattern() {
        ApplicationConfig config = parse();
        assertFalse(config.shouldPrintUsageAndExit);

        assertEquals(1, config.filePatterns.length);
        assertEquals(DEFAULT_PATTERN, config.filePatterns[0]);
    }

    @Test
    void supportedScanArguments_shouldBeRecognized() {
        // default is no scans
        assertInputResultsInScans(
                "",
                AllowsCleartextTraffic.class,
                SpecifiesNetworkSecurityConfig.class,
                UsesCertificatePinning.class,
                ContainsSuspectedFirmwareFile.class
        );

        assertInputResultsInScans(
                "--scan-allows-cleartext-traffic",
                AllowsCleartextTraffic.class
        );
        assertInputResultsInScans(
                "--scan-network-security-config",
                SpecifiesNetworkSecurityConfig.class
        );
        assertInputResultsInScans(
                "--scan-uses-certificate-pinning",
                UsesCertificatePinning.class
        );
        assertInputResultsInScans(
                "--scan-contains-suspected-firmware",
                ContainsSuspectedFirmwareFile.class
        );
        assertInputResultsInScans(
                "--scan-print-android-manifest",
                PrintAndroidManifest.class
        );
        assertInputResultsInScans(
                "--scan-print-file-report",
                PrintFileReport.class
        );
        assertInputResultsInScans(
                "--scan-print-file-entropy",
                PrintFileReport.class
        );
        assertInputResultsInScans(
                "--scan-print-zip-entries",
                PrintZipEntries.class
        );
    }

    private void assertInputResultsInScans(String input, Class<? extends AbstractScan>... expectedScans) {
        String[] inputs = input.split(" ");
        AbstractScan[] actualScans = parse(inputs).scans;
        assertEquals(expectedScans.length, actualScans.length);
        for (int i = 0; i < expectedScans.length; i++) {
            assertEquals(expectedScans[i], actualScans[i].getClass());
        }
    }

    @Test
    void logArguments_productDifferentLogLevels() {
        assertInputResultsInLogLevel(
                "--log-error",
                Level.SEVERE
        );
        assertInputResultsInLogLevel(
                "--log-warn",
                Level.WARNING
        );
        assertInputResultsInLogLevel(
                "",
                DEFAULT_LOG_LEVEL
        );
        assertInputResultsInLogLevel(
                "--log-debug",
                Level.FINE
        );
        assertInputResultsInLogLevel(
                "--log-trace",
                Level.FINEST
        );
    }

    private void assertInputResultsInLogLevel(String input, Level expectedLogLevel) {
        assertEquals(expectedLogLevel, parse(input.split(" ")).logLevel);
    }

    @Test
    void unrecognizedOption_shouldProduceError() {
        ApplicationConfig config = parse("--foo");
        assertTrue(config.shouldPrintUsageAndExit);
    }

    @Test
    void filePatterns_areRecognised() {
        assertInputResultsInFilePatterns(
                "",
                DEFAULT_PATTERN
        );
        assertInputResultsInFilePatterns(
                "--log-error *.zip",
                "*.zip"
        );
        assertInputResultsInFilePatterns(
                "--scan-contains-suspected-firmware ../apk/*.apk ./*",
                "../apk/*.apk",
                "./*"
        );
    }

    private void assertInputResultsInFilePatterns(String input, String... expectedPatterns) {
        String[] actualPatterns = parse(input.split(" ")).filePatterns;
        assertArrayEquals(expectedPatterns, actualPatterns);
    }
}