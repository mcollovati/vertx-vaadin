package com.github.mcollovati.vertx.utils;

import java.io.File;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.junit.Test;

public class FastClasspathScannerTest {

    @Test
    public void testME() {
        new FastClasspathScanner()
            .alwaysScanClasspathElementRoot()
            //.matchFilenamePattern("META-INF/resources/.*", (File classpathElt, String relativePath) -> {
            .matchFilenamePattern("(META-INF/resources/).*(?!\\.class)", (File classpathElt, String relativePath) -> {
                System.out.println("===================== " + classpathElt + " ---> " + relativePath);
            })
            //.verbose()
            .removeTemporaryFilesAfterScan(true)
            .scan();
    }
}
