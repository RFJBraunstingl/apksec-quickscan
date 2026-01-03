package dev.rfj.asqs.scans;

import java.util.Set;
import java.util.zip.ZipFile;

public abstract class ContainsFileWithEndingZipEntryBasedScan extends AbstractScan {

    @Override
    public boolean isFound(ZipFile apk) {
        return apk.stream()
                .anyMatch(f -> {
                    String fileName = f.getName();
                    String ending = fileName.substring(fileName.lastIndexOf(".") + 1);
                    return fileEndings().contains(ending);
                });
    }

    protected abstract Set<String> fileEndings();
}
