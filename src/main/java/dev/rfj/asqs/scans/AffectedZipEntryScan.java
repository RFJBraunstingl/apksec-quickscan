package dev.rfj.asqs.scans;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Find affected files (zip entries) in the apk file.
 */
public abstract class AffectedZipEntryScan extends AbstractScan {
    @Override
    public String scan(ZipFile apk) {
        return apk.stream()
                .filter(entry -> isAffected(apk, entry))
                .findFirst()
                .map(ZipEntry::getName)
                .orElse("");
    }

    protected abstract boolean isAffected(ZipFile zipFile, ZipEntry zipEntry);
}
