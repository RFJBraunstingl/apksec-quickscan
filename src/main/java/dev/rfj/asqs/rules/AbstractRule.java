package dev.rfj.asqs.rules;

import java.io.File;

public abstract class AbstractRule {

    public abstract boolean raisesRedFlag(File apk);
}
