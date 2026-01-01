package dev.rfj.asqs.rules;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public abstract class ZipEntryBasedRule extends AbstractRule {

    @Override
    public boolean raisesRedFlag(ZipFile apk) {
        return apk.stream().anyMatch(this::zipEntryRaisesRedFlag);
    }

    protected abstract boolean zipEntryRaisesRedFlag(ZipEntry zipEntry);
}
