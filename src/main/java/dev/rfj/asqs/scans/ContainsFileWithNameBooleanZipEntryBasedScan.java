package dev.rfj.asqs.scans;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public abstract class ContainsFileWithNameBooleanZipEntryBasedScan extends AffectedZipEntryScan {
    @Override
    protected boolean isAffected(ZipFile zipFile, ZipEntry zipEntry) {
        return zipEntry.getName().endsWith(fileName());
    }

    protected abstract String fileName();
}
