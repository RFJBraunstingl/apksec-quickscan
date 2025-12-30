package dev.rfj.asqs.rules;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public abstract class ZipEntryBasedRule extends AbstractRule {

    @Override
    public boolean raisesRedFlag(File apk) {
        try (ZipFile zipFile = new ZipFile(apk)) {
            return zipFile.stream().anyMatch(this::zipEntryRaisesRedFlag);
        } catch (ZipException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException("I/O error while processing apk file", e);
        }
    }

    protected abstract boolean zipEntryRaisesRedFlag(ZipEntry zipEntry);
}
