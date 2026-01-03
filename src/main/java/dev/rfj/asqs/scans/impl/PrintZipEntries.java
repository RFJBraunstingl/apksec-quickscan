package dev.rfj.asqs.scans.impl;

import dev.rfj.asqs.scans.ZipEntryBasedScan;

import java.util.logging.Logger;
import java.util.zip.ZipEntry;

public class PrintZipEntries extends ZipEntryBasedScan {

    private static final Logger log = Logger.getLogger(PrintZipEntries.class.getName());

    @Override
    protected boolean isFoundInZipEntry(ZipEntry zipEntry) {
        log.info("apk entry " + zipEntry.getName() + " has size " + zipEntry.getSize());
        return false;
    }
}
