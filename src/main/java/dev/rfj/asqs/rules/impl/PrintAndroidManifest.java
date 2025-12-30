package dev.rfj.asqs.rules.impl;

import dev.rfj.asqs.rules.AndroidManifestBasedRule;

public class PrintAndroidManifest extends AndroidManifestBasedRule {
    @Override
    protected boolean androidManifestRaisesRedFlag(String androidManifest) {
        System.out.println(androidManifest);
        return false;
    }
}
