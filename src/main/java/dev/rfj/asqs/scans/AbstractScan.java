package dev.rfj.asqs.scans;

import java.util.zip.ZipFile;

public abstract class AbstractScan {

    public abstract boolean isFound(ZipFile apk);
}
