package dev.rfj.asqs.rules;

import java.io.File;
import java.util.zip.ZipFile;

public abstract class AbstractRule {

    public abstract boolean raisesRedFlag(ZipFile apk);
}
