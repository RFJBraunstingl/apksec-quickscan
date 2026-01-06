package dev.rfj.asqs.scans.impl;

import dev.rfj.asqs.scans.AffectedZipEntryScan;
import dev.rfj.asqs.util.EntropyCalculator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Simple scan to log all file names with their sizes
 */
public class ReportFiles extends AffectedZipEntryScan {

    private static final Logger log = Logger.getLogger(ReportFiles.class.getName());

    private final boolean calculateEntropy;

    public ReportFiles() {
        this(false);
    }

    public ReportFiles(boolean calculateEntropy) {
        this.calculateEntropy = calculateEntropy;
    }

    @Override
    protected boolean isAffected(ZipFile apk, ZipEntry entry) {
        String appName = new File(apk.getName()).getName().replaceAll("\"", "\"\"");
        String filePath = entry.getName().replaceAll("\"", "\"\"");
        String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
        String size = String.valueOf(entry.getSize());
        System.out.println('"' + appName + '"' + ',' +
                '"' + filePath + '"' + ',' +
                '"' + fileName + '"' + ',' +
                size +
                (calculateEntropy ? ',' + calculateEntropy(apk, entry) : "")
        );
        return false;
    }

    private String calculateEntropy(ZipFile apk, ZipEntry entry) {
        try (InputStream is = apk.getInputStream(entry)) {
            double entropy = EntropyCalculator.calculateEntropy(is);
            return String.format("%.2f", entropy);

        } catch (IOException e) {
            log.info("error calculating entropy: " + e.getMessage());
            return "error";
        }
    }
}
