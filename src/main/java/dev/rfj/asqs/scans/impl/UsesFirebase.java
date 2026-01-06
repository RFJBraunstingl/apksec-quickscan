package dev.rfj.asqs.scans.impl;

import dev.rfj.asqs.scans.ContainsFileWithNameBooleanZipEntryBasedScan;

public class UsesFirebase extends ContainsFileWithNameBooleanZipEntryBasedScan {
    @Override
    protected String fileName() {
        return "google-services.json";
    }
}
