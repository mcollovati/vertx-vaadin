package com.github.mcollovati.vertx.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

public class ClassGraphTest {

    public static final String META_INF_RESOURCES_PATH = "META-INF/resources";

    @Test
    public void testME() {
        Path resourcePath = Paths.get(META_INF_RESOURCES_PATH);
        try (ScanResult scanResult = new ClassGraph()
            .verbose()
            .whitelistPaths(META_INF_RESOURCES_PATH)
            .removeTemporaryFilesAfterScan()
            .scan()) {

            scanResult.getAllResources()
                .nonClassFilesOnly()
                .stream()
                .map(Resource::getPathRelativeToClasspathElement)
                .map(Paths::get)
                .map(resourcePath::relativize)
                .forEach(System.out::println);
        }
        Assert.assertTrue(true);
    }
}
