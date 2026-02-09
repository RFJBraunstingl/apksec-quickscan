package dev.rfj.asqs.scans.impl;

import dev.rfj.asqs.scans.ContainsAssetWithEndingZipEntryBasedScan;

import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.singletonList;

public class UsesReactNative extends ContainsAssetWithEndingZipEntryBasedScan {

    private static final Set<String> FILE_ENDINGS = new HashSet<>(singletonList("index.android.bundle"));

    @Override
    protected Set<String> fileEndings() {
        return FILE_ENDINGS;
    }
}
