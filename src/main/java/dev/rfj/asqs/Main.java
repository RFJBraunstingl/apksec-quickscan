package dev.rfj.asqs;

import dev.rfj.asqs.rules.impl.PrintAndroidManifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java -jar asqs.jar <pattern>");
            return;
        }

        log.info("Starting ASQS");
        String pattern = args[1];
        long startTime = System.currentTimeMillis();

        log.debug("grabbing files for pattern '{}'", pattern);
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        matcher.

        Path path = Paths.get("src", "test", "resources", "dummy-apks", "navigation-drawer-example-debug.apk");
        File apk = path.toFile();
        new PrintAndroidManifest().raisesRedFlag(apk);
    }
}
