package dev.rfj.asqs.scans;

import java.util.zip.ZipFile;

/**
 * Scan a given apk (zip)file and return the string which will be printed to the CSV file.
 */
public abstract class AbstractScan {

    public abstract String scan(ZipFile apk);
}
