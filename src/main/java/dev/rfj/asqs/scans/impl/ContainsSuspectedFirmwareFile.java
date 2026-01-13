package dev.rfj.asqs.scans.impl;

import dev.rfj.asqs.firmwarematcher.FirmwareMatcher;
import dev.rfj.asqs.firmwarematcher.SimpleFirmwareMatcher;
import dev.rfj.asqs.scans.AffectedZipEntryScan;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Scan the apk for files which look like firmware images.
 */
public class ContainsSuspectedFirmwareFile extends AffectedZipEntryScan {

    private final FirmwareMatcher firmwareMatcher;

    public ContainsSuspectedFirmwareFile() {
        this.firmwareMatcher = new SimpleFirmwareMatcher();
    }

    @Override
    protected boolean isAffected(ZipFile zipFile, ZipEntry zipEntry) {
        return firmwareMatcher.isSuspectedFirmwareFile(zipFile, zipEntry);
    }
}
