package dev.rfj.asqs.util;

import java.io.IOException;
import java.io.InputStream;

public class EntropyCalculator {

    public static final int BYTE_BUFFER_SIZE = 8192;
    public static final double LOGN_2 = Math.log(2);

    private EntropyCalculator() {}

    public static double calculateEntropy(InputStream is) throws IOException {
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

        // calculate total file size
        long totalSizeInBytes = 0;
        for (long byteCount : byteCounts) {
            totalSizeInBytes += byteCount;
        }

        // calculate entropy
        double entropy = 0.0;
        for (int i = 0; i < 256; i++) {
            double p = (double) byteCounts[i] / totalSizeInBytes;
            if (p > 0) {
                entropy -= p * log2(p);
            }
        }

        return entropy;
    }

    private static double log2(double x) {
        return Math.log(x) / LOGN_2;
    }
}
