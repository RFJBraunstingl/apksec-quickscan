package dev.rfj.asqs.firmwarematcher;

import dev.rfj.asqs.scans.impl.ContainsSuspectedFirmwareFile;
import dev.rfj.asqs.util.EntropyCalculator;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *  based on the following query:
 *  <code>
 *   SELECT * FROM files
 *   WHERE lower(filePath) LIKE '%firmware%'
 *   AND NOT starts_with(filePath, 'res/')
 *   AND ext NOT IN ('png', 'webp', 'svg', 'json', 'ai')
 *   AND (ext = 'bin' OR entropy > 4);
 *  </code>
 */
public class SimpleFirmwareMatcher implements FirmwareMatcher {

    private static final Logger LOGGER = Logger.getLogger(SimpleFirmwareMatcher.class.getName());
    private static final double ENTROPY_THRESHOLD = 0.5;
    private static final String[] IGNORED_FILE_ENDINGS = new String[]{
            ".png",
            ".webp",
            ".svg",
            ".json",
            ".html",
            ".ai",
            ".aidl",
    };


    @Override
    public boolean isSuspectedFirmwareFile(ZipFile zipFile, ZipEntry zipEntry) {
        String filePath = zipEntry.getName().toLowerCase();
        if (filePath.startsWith("res/")) {
            return false;
        }
        if (filePath.endsWith(".bin")) {
            return filePath.startsWith("assets/") && !filePath.endsWith("assetmanifest.bin");
        }
        if (!filePath.contains("firmware")) {
            return false;
        }
        for (String ending : IGNORED_FILE_ENDINGS) {
            if (filePath.endsWith(ending)) {
                return false;
            }
        }
        try (InputStream is = zipFile.getInputStream(zipEntry)) {
            double entropy = EntropyCalculator.calculateEntropy(is);
            return entropy > ENTROPY_THRESHOLD;
        } catch (IOException e) {
            LOGGER.warning("failed to read input stream for firmware entry: " + zipEntry.getName());
            return false;
        }
    }
}
