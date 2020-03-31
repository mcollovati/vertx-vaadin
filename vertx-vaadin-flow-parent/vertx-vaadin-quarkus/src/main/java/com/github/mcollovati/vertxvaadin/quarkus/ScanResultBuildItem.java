package com.github.mcollovati.vertxvaadin.quarkus;

import io.github.classgraph.ScanResult;
import io.quarkus.builder.item.SimpleBuildItem;

public final class ScanResultBuildItem extends SimpleBuildItem {

    final ScanResult scanResult;

    public ScanResultBuildItem(ScanResult scanResult) {
        this.scanResult = scanResult;
    }

    public ScanResult getScanResult() {
        return scanResult;
    }
}
