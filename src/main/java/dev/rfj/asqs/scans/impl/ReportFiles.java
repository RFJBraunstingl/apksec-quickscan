package dev.rfj.asqs.scans.impl;

import dev.rfj.asqs.scans.AbstractScan;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Simple scan to log all file names with their sizes
 */
public class ReportFiles extends AbstractScan {

    public static final int BYTE_BUFFER_SIZE = 8192;

    private static final Logger log = Logger.getLogger(ReportFiles.class.getName());

    private final boolean calculateEntropy;

    public ReportFiles() {
        this(false);
    }

    public ReportFiles(boolean calculateEntropy) {
        this.calculateEntropy = calculateEntropy;
    }

    @Override
    public boolean isFound(ZipFile apk) {
        apk.stream().forEach(entry -> {
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
        });
        return false;
    }

    private String calculateEntropy(ZipFile apk, ZipEntry entry) {
        try (InputStream is = apk.getInputStream(entry)) {
            // count bytes
            long[] byteCounts = new long[256];
            byte[] buf = new byte[BYTE_BUFFER_SIZE];
            int numOfBytesRead;
            while ((numOfBytesRead = is.read(buf)) != -1) {
                for (int i = 0; i < numOfBytesRead; i++) {
                    int b = buf[i] & 0xFF;
                    byteCounts[b]++;
                }
            }

            // calculate entropy
            double entropy = 0.0;
            for (int i = 0; i < 256; i++) {
                double p = (double) byteCounts[i] / entry.getSize();
                if (p > 0) {
                    entropy -= p * Math.log(p);
                }
            }

            return String.format("%.2f", entropy);

        } catch (IOException e) {
            log.info("error calculating entropy: " + e.getMessage());
            return "error";
        }
    }
}
