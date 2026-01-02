package dev.rfj.asqs.rules;

import java.util.Set;
import java.util.zip.ZipFile;

public abstract class ContainsFileWithEndingZipEntryBasedRule extends AbstractRule {

    @Override
    public boolean raisesRedFlag(ZipFile apk) {
        return apk.stream()
                .anyMatch(f -> {
                    String fileName = f.getName();
                    String ending = fileName.substring(fileName.lastIndexOf(".") + 1);
                    return fileEndings().contains(ending);
                });
    }

    protected abstract Set<String> fileEndings();
}
