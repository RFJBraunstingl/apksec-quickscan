package dev.rfj.asqs.scans.impl;

import dev.rfj.asqs.scans.AndroidManifestBasedScan;

import java.util.logging.Logger;

public class PrintAndroidManifest extends AndroidManifestBasedScan {

    private static final Logger log = Logger.getLogger(PrintAndroidManifest.class.getName());

    @Override
    protected boolean isFoundInManifest(String androidManifest) {
        log.info("extracted manifest");
        log.info(androidManifest);
        return false;
    }
}
