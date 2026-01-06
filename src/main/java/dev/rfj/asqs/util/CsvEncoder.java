package dev.rfj.asqs.util;

public class CsvEncoder {

    private CsvEncoder() {}

    public static String encodeString(String string) {
        return "\"" + string.replaceAll("\"", "\"\"") + "\"";
    }
}
