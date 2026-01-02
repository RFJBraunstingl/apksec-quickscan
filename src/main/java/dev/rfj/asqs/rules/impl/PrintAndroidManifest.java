package dev.rfj.asqs.rules.impl;

import dev.rfj.asqs.rules.AndroidManifestBasedRule;

import java.util.logging.Logger;

public class PrintAndroidManifest extends AndroidManifestBasedRule {

    private static final Logger log = Logger.getLogger(PrintAndroidManifest.class.getName());

    @Override
    protected boolean androidManifestRaisesRedFlag(String androidManifest) {
        log.info("extracted manifest");
        log.info(androidManifest);
        return false;
    }
}
