package dev.rfj.asqs.scans;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public abstract class ZipEntryBasedScan extends AbstractScan {

    @Override
    public boolean isFound(ZipFile apk) {
        return apk.stream().anyMatch(this::isFoundInZipEntry);
    }

    protected abstract boolean isFoundInZipEntry(ZipEntry zipEntry);
}
