package dev.rfj.asqs.scans.impl;

import dev.rfj.asqs.scans.ContainsAssetWithEndingZipEntryBasedScan;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class UsesCertificatePinning extends ContainsAssetWithEndingZipEntryBasedScan {

    private static final Set<String> FILE_ENDINGS = new HashSet<>(Arrays.asList(
            ".crt",
            ".cert",
            ".keystore",
            ".jks",
            ".crt.txt",
            ".cert.txt",
            ".pks"
    ));

    @Override
    protected Set<String> fileEndings() {
        return FILE_ENDINGS;
    }
}
