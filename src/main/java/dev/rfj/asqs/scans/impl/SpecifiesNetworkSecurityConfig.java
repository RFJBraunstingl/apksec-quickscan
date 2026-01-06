package dev.rfj.asqs.scans.impl;

import dev.rfj.asqs.scans.AndroidManifestBasedScan;

public class SpecifiesNetworkSecurityConfig extends AndroidManifestBasedScan {
    @Override
    protected String scanManifest(String androidManifest) {
        return String.valueOf(androidManifest.contains("android:networkSecurityConfig="));
    }
}
