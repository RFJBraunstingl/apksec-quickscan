package dev.rfj.asqs.firmwarematcher;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public interface FirmwareMatcher {

    boolean isSuspectedFirmwareFile(ZipFile zipFile, ZipEntry entry);
}
