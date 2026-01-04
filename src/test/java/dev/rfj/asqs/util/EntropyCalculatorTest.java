package dev.rfj.asqs.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

class EntropyCalculatorTests {

    public static final String PATH_TO_PERFECTLY_GOOD_ENTROPY_FILE = "src/test/resources/all_bytes_0_to_255.bin";
    public static final String PATH_TO_PERFECTLY_BAD_ENTROPY_FILE = "src/test/resources/all_zeros_256_bytes.bin";

    @Test
    void goodEntropyFile_shouldHaveEntropy_8() throws IOException {
        try (InputStream is = Files.newInputStream(Paths.get(PATH_TO_PERFECTLY_GOOD_ENTROPY_FILE))) {
            double actual = EntropyCalculator.calculateEntropy(is);
            Assertions.assertTrue(actual > 7.99);
            Assertions.assertTrue(actual < 8.01);
        }
    }

    @Test
    void badEntropyFile_shouldHaveEntropy_0() throws IOException {
        try (InputStream is = Files.newInputStream(Paths.get(PATH_TO_PERFECTLY_BAD_ENTROPY_FILE))) {
            double actual = EntropyCalculator.calculateEntropy(is);
            Assertions.assertTrue(actual > -0.01);
            Assertions.assertTrue(actual < 0.01);
        }
    }
}