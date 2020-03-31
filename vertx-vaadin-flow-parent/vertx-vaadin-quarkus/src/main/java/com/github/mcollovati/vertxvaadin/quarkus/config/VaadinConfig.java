package com.github.mcollovati.vertxvaadin.quarkus.config;

import java.util.List;
import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

@ConfigGroup
public class VaadinConfig {

    /**
     * List of packages to scan
     */
    @ConfigItem
    public Optional<List<String>> flowBasePackages;

    @Override
    public String toString() {
        return "VaadinConfig{" +
            ", flowBasePackages=" + flowBasePackages +
            '}';
    }
}
