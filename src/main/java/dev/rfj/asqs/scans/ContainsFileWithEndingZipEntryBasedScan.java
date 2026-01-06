package dev.rfj.asqs.scans;

import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public abstract class ContainsFileWithEndingZipEntryBasedScan extends AffectedZipEntryScan {

    @Override
    protected boolean isAffected(ZipFile zipFile, ZipEntry zipEntry) {
        String fileName = zipEntry.getName();
        String ending = fileName.substring(fileName.lastIndexOf(".") + 1);
        return fileEndings().contains(ending);
    }

    protected abstract Set<String> fileEndings();
}
