package dev.rfj.asqs.rules;

import java.util.zip.ZipEntry;

public abstract class ContainsFileWithNameZipEntryBasedRule extends ZipEntryBasedRule {
    @Override
    protected boolean zipEntryRaisesRedFlag(ZipEntry zipEntry) {
        return zipEntry.getName().endsWith(fileName());
    }

    protected abstract String fileName();
}
