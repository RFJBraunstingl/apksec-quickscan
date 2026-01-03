package dev.rfj.asqs.scans.impl;

import dev.rfj.asqs.scans.AndroidManifestBasedScan;

public class AllowsCleartextTraffic extends AndroidManifestBasedScan {
    @Override
    protected boolean isFoundInManifest(String androidManifest) {
        return androidManifest.contains("usesCleartextTraffic=\"true\"");
    }
}
