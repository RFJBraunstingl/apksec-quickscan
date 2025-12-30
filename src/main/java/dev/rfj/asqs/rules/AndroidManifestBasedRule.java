package dev.rfj.asqs.rules;

import dev.rfj.asqs.util.AndroidManifestDecoder;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public abstract class AndroidManifestBasedRule extends AbstractRule {

    private static final Logger logger = Logger.getLogger(AndroidManifestBasedRule.class.getName());

    @Override
    public boolean raisesRedFlag(File apk) {
        try {
            return androidManifestRaisesRedFlag(
                    AndroidManifestDecoder.decodeManifestFromApk(apk)
            );
        } catch (IOException e) {
            logger.warning("failed to extract manifest from apk: " + apk.getAbsolutePath());
        }
        return false;
    }

    protected abstract boolean androidManifestRaisesRedFlag(String androidManifest);
}
