package dev.rfj.asqs.rules.impl;

import dev.rfj.asqs.rules.ContainsFileWithNameZipEntryBasedRule;

public class UsesFirebase extends ContainsFileWithNameZipEntryBasedRule {
    @Override
    protected String fileName() {
        return "google-services.json";
    }
}
