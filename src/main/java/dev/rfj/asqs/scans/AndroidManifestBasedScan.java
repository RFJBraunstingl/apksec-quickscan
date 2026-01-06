package dev.rfj.asqs.scans;

import dev.rfj.asqs.util.AndroidManifestDecoder;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

public abstract class AndroidManifestBasedScan extends AbstractScan {

    private static final Logger logger = Logger.getLogger(AndroidManifestBasedScan.class.getName());

    @Override
    public String scan(ZipFile apk) {
        try {
            return scanManifest(
                    AndroidManifestDecoder.decodeManifestFromZipFile(apk)
            );
        } catch (IOException e) {
            logger.warning("failed to extract manifest from apk: " + apk.getName());
            return "false";
        }
    }

    protected abstract String scanManifest(String androidManifest);
}
