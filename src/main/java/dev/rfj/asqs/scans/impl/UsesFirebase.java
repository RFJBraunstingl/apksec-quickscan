package dev.rfj.asqs.scans.impl;

import dev.rfj.asqs.scans.ContainsFileWithNameZipEntryBasedScan;

public class UsesFirebase extends ContainsFileWithNameZipEntryBasedScan {
    @Override
    protected String fileName() {
        return "google-services.json";
    }
}
