package dev.rfj.asqs.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CsvEncoderTest {

    private static void assertEncode(String input, String expected) {
        assertEquals(expected, CsvEncoder.encodeString(input));
    }

    @Test
    void inputIsQuoted() {
        assertEncode("foo", "\"foo\"");
    }

    @Test
    void quotesAreEscaped() {
        assertEncode("foo \"bar\"", "\"foo \"\"bar\"\"\"");
    }
}