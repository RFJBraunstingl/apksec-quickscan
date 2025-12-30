package dev.rfj.asqs.rules.impl;

import dev.rfj.asqs.rules.ZipEntryBasedRule;

import java.util.logging.Logger;
import java.util.zip.ZipEntry;

public class PrintZipEntries extends ZipEntryBasedRule {

    private static final Logger log = Logger.getLogger(PrintZipEntries.class.getName());

    @Override
    protected boolean zipEntryRaisesRedFlag(ZipEntry zipEntry) {
        log.info("apk entry " + zipEntry.getName() + " has size " + zipEntry.getSize());
        return false;
    }
}
