package dev.rfj.asqs.scans.impl;

import dev.rfj.asqs.scans.AffectedZipEntryScan;

import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PrintZipEntries extends AffectedZipEntryScan {

    private static final Logger log = Logger.getLogger(PrintZipEntries.class.getName());

    @Override
    protected boolean isAffected(ZipFile zipFile, ZipEntry zipEntry) {
        log.info("apk entry " + zipEntry.getName() + " has size " + zipEntry.getSize());
        return false;
    }
}
