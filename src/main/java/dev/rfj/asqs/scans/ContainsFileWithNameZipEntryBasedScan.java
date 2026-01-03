package dev.rfj.asqs.scans;

import java.util.zip.ZipEntry;

public abstract class ContainsFileWithNameZipEntryBasedScan extends ZipEntryBasedScan {
    @Override
    protected boolean isFoundInZipEntry(ZipEntry zipEntry) {
        return zipEntry.getName().endsWith(fileName());
    }

    protected abstract String fileName();
}
