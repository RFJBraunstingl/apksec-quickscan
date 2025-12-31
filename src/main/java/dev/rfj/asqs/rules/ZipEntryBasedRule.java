package dev.rfj.asqs.rules;

import com.sun.org.slf4j.internal.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public abstract class ZipEntryBasedRule extends AbstractRule {

    private static final Logger logger = Logger.getLogger(ZipEntryBasedRule.class.getName());

    @Override
    public boolean raisesRedFlag(File apk) {
        try (ZipFile zipFile = new ZipFile(apk)) {
            return zipFile.stream().anyMatch(this::zipEntryRaisesRedFlag);
        } catch (ZipException e) {
            logger.severe(
                    String.format(
                            "could not process apk file '%s' due to ZipException! " + e.getMessage(),
                            apk.getName()
                    )
            );
            return false;
        } catch (IOException e) {
            logger.severe(
                    String.format(
                            "could not process apk file '%s' due to I/O error! " + e.getMessage(),
                            apk.getName()
                    )
            );
            return false;
        }
    }

    protected abstract boolean zipEntryRaisesRedFlag(ZipEntry zipEntry);
}
