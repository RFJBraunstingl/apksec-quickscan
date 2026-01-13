package dev.rfj.asqs.scans;

import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public abstract class ContainsAssetWithEndingZipEntryBasedScan extends AffectedZipEntryScan {

    @Override
    protected boolean isAffected(ZipFile zipFile, ZipEntry zipEntry) {
        String fileName = zipEntry.getName();
        return fileEndings().stream().anyMatch(ending -> fileName.startsWith("assets/") && fileName.endsWith(ending));
    }

    protected abstract Set<String> fileEndings();
}
