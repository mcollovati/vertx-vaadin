package com.github.mcollovati.vertx.vaadin.setup;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

public class ClassFinder {

    private final ClassGraph classGraph;
    private final Vertx vertx;

    private ClassFinder(Vertx vertx, boolean debug, List<String> packagesToScan) {
        this.vertx = vertx;
        classGraph = new ClassGraph();
        if (debug) {
            classGraph.verbose();
        }
        classGraph.ignoreParentClassLoaders()
            .enableClassInfo()
            .enableAnnotationInfo()
            .whitelistPackages(packagesToScan.toArray(new String[0]))
            .ignoreParentClassLoaders()
            .removeTemporaryFilesAfterScan();
    }

    public static ClassFinder create(Vertx vertx, boolean debugMode, List<String> packages) {
        return new ClassFinder(vertx, debugMode, packages);
    }

    public <T> void scan(Function<ScanResult, T> handler, Handler<AsyncResult<T>> resultHandler) {
        Future<ScanResult> future = Future.future();
        vertx.<T>executeBlocking(event -> {
            try (ScanResult scanResult = classGraph.scan()) {
                event.complete(handler.apply(scanResult));
            }
        }, resultHandler);
    }

}
