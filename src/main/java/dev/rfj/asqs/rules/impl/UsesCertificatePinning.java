package dev.rfj.asqs.rules.impl;

import dev.rfj.asqs.rules.ContainsFileWithEndingZipEntryBasedRule;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class UsesCertificatePinning extends ContainsFileWithEndingZipEntryBasedRule {

    private static final Set<String> FILE_ENDINGS = new HashSet<>(Arrays.asList(
            "crt",
            "cert",
            "keystore",
            "jks"
    ));

    @Override
    protected Set<String> fileEndings() {
        return FILE_ENDINGS;
    }
}
