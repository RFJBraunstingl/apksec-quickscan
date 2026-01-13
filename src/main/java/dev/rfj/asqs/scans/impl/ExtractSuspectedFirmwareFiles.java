package dev.rfj.asqs.scans.impl;

import dev.rfj.asqs.firmwarematcher.FirmwareMatcher;
import dev.rfj.asqs.firmwarematcher.SimpleFirmwareMatcher;
import dev.rfj.asqs.scans.AbstractScan;

import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ExtractSuspectedFirmwareFiles extends AbstractScan {

    private final FirmwareMatcher firmwareMatcher;

    public ExtractSuspectedFirmwareFiles() {
        this.firmwareMatcher = new SimpleFirmwareMatcher();
    }

    @Override
    public String scan(ZipFile apk) {
        return apk.stream()
                .filter(entry -> firmwareMatcher.isSuspectedFirmwareFile(apk, entry))
                .map(ZipEntry::getName)
                .collect(Collectors.joining(","));
    }
}
