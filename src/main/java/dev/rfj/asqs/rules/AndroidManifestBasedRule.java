package dev.rfj.asqs.rules;

import dev.rfj.asqs.util.AndroidManifestDecoder;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

public abstract class AndroidManifestBasedRule extends AbstractRule {

    private static final Logger logger = Logger.getLogger(AndroidManifestBasedRule.class.getName());

    @Override
    public boolean raisesRedFlag(ZipFile apk) {
        try {
            return androidManifestRaisesRedFlag(
                    AndroidManifestDecoder.decodeManifestFromZipFile(apk)
            );
        } catch (IOException e) {
            logger.warning("failed to extract manifest from apk: " + apk.getName());
            return false;
        }
    }

    protected abstract boolean androidManifestRaisesRedFlag(String androidManifest);
}
