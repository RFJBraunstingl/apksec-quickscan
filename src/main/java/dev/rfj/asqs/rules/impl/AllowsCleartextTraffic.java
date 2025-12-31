package dev.rfj.asqs.rules.impl;

import dev.rfj.asqs.rules.AndroidManifestBasedRule;

public class AllowsCleartextTraffic extends AndroidManifestBasedRule {
    @Override
    protected boolean androidManifestRaisesRedFlag(String androidManifest) {
        return androidManifest.contains("usesCleartextTraffic=\"true\"");
    }
}
