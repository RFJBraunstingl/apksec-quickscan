package dev.rfj.asqs;

import dev.rfj.asqs.rules.impl.PrintAndroidManifest;
import dev.rfj.asqs.rules.impl.PrintZipEntries;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        Path path = Paths.get("src", "test", "resources", "dummy-apks", "navigation-drawer-example-debug.apk");
        File apk = path.toFile();
        new PrintAndroidManifest().raisesRedFlag(apk);
    }
}
