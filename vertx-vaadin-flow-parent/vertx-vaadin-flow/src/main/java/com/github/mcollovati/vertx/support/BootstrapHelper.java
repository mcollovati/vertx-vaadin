package com.github.mcollovati.vertx.support;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.mcollovati.vertx.vaadin.VaadinOptions;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.emptySet;

public final class BootstrapHelper {

    private static final Logger logger = LoggerFactory.getLogger(BootstrapHelper.class);
    public static final String JSON_SCAN_RESULT = "META-INF/vertxvaadin/classgrapsh-scanresult.json";

    private static final BootstrapTypes STATIC_SCAN_RESULT = new BootstrapTypes(false);

    private BootstrapHelper() {
    }

    public static void scanResources(VaadinOptions opts, Consumer<Set<String>> onSuccess, Consumer<Exception> onError) {
        if (STATIC_SCAN_RESULT.staticallyLoaded) {
            logger.debug("Loading resources from static scan result");
            onSuccess.accept(new HashSet<>(STATIC_SCAN_RESULT.resources));
            return;
        }
        logger.debug("Scanning for resources...");
        scan(opts, classGraph -> classGraph.whitelistPaths().removeTemporaryFilesAfterScan(),
            scanResult -> onSuccess.accept(scanResult.getAllResources()
                .nonClassFilesOnly()
                .stream()
                .map(Resource::getPathRelativeToClasspathElement)
                .collect(Collectors.toSet())),
            onError).run();
    }

    public static void scanVaadinComponents(VaadinOptions options, Consumer<BootstrapTypes> onSuccess, Consumer<Exception> onError) {
        if (STATIC_SCAN_RESULT.staticallyLoaded) {
            logger.debug("Loading vaadin components from static scan result");
            onSuccess.accept(STATIC_SCAN_RESULT);
            return;
        }

        List<String> pkgs = options.flowBasePackages();
        if (!pkgs.isEmpty()) {
            pkgs.add("com.vaadin");
            pkgs.add("com.github.mcollovati.vertx.vaadin");
        }
        logger.debug("Scanning for vaadin components ({})", pkgs);
        scan(options, classGraph -> classGraph.ignoreParentClassLoaders()
                .enableClassInfo()
                .enableAnnotationInfo()
                .whitelistPackages(pkgs.toArray(new String[0]))
                .ignoreParentClassLoaders()
                .removeTemporaryFilesAfterScan(),
            scanResult -> onSuccess.accept(new BootstrapTypes(scanResult)),
            onError).run();
    }

    private static Runnable scan(VaadinOptions options, Consumer<ClassGraph> configurer,
                                 Consumer<ScanResult> onSuccess, Consumer<Exception> onError) {
        ClassGraph classGraph = new ClassGraph();
        configurer.accept(classGraph);
        if (options.debug()) {
            classGraph.verbose();
        }
        return () -> {
            try (ScanResult scanResult = classGraph.scan()) {
                onSuccess.accept(scanResult);
            } catch (Exception ex) {
                onError.accept(ex);
            }
        };
    }


    @SuppressWarnings("unchecked")
    public static final class BootstrapTypes {

        private final Set<String> resources = new HashSet<>();
        private final Set<String> vaadinVerticles = new HashSet<>();
        private final Map<String, Boolean> loadedTypes = new HashMap<>();
        private final Map<Class<?>, Set<Class<?>>> bootstrapTypes = new HashMap<>();
        private final transient ScanResult scanResult;
        private final boolean staticallyLoaded;

        private BootstrapTypes(ScanResult scanResult) {
            this.scanResult = scanResult;
            staticallyLoaded = false;
        }

        private BootstrapTypes(boolean staticallyLoaded) {
            this.staticallyLoaded = staticallyLoaded;
            scanResult = null;
        }

        public boolean hasClass(String className) {
            if (scanResult != null) {
                return loadedTypes.computeIfAbsent(className, k -> scanResult.getClassInfo(className) != null);
            } else {
                return loadedTypes.getOrDefault(className, false);
            }
        }


        public Set<String> findClasses(Predicate<String> predicate) {
            if (scanResult != null) {
                List<String> names = scanResult.getAllClasses()
                    .filter(classInfo -> predicate.test(classInfo.getName()))
                    .stream()
                    .map(classInfo -> {
                        try {
                            logger.trace("findClasses :: loading {}", classInfo.getName());
                            return classInfo.loadClass(true);
                        } catch (Exception ex) {
                            logger.warn("findClasses :: Cannot load {} -> {}", classInfo.getName(), ex.getMessage());
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .map(Class::getName)
                    .collect(Collectors.toList());
                names.forEach(type -> loadedTypes.put(type, true));
                logger.trace("findClasses :: result = {}", names);
                return new HashSet<>(names);
            } else {
                return loadedTypes.entrySet().stream()
                    .filter(Map.Entry::getValue)
                    .map(Map.Entry::getKey)
                    .filter(predicate)
                    .collect(Collectors.toSet());
            }
        }

        public Set<String> getVaadinVerticles() {
            if (scanResult != null) {
                for (String vaadinVerticle : scanResult.getSubclasses("com.github.mcollovati.vertx.vaadin.VaadinVerticle")
                    .stream()
                    .map(classInfo -> {
                        try {
                            logger.trace("getVaadinVerticles :: loading {}", classInfo.getName());
                            return classInfo.loadClass(true);
                        } catch (Exception ex) {
                            logger.warn("getVaadinVerticles :: Cannot load {} -> {}", classInfo.getName(), ex.getMessage());
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .map(Class::getName)
                    .collect(Collectors.toList())) {
                    loadedTypes.put(vaadinVerticle, true);
                    vaadinVerticles.add(vaadinVerticle);
                }
                logger.trace("Verticles: {}", vaadinVerticles);
            }
            return new HashSet<>(vaadinVerticles);
        }

        public Set<Class<?>> findHandledTypes(Class<?> mainType, Class<?>... handledTypes) {

            if (scanResult != null) {
                Function<Class<?>, ClassInfoList> classFinder = classFinder();
                Set<Class<?>> classes = Stream.of(handledTypes)
                    .map(classFinder)
                    .flatMap(c -> c.loadClasses().stream())
                    .collect(Collectors.toSet());
                classes.forEach(c -> loadedTypes.put(c.getName(), true));
                bootstrapTypes.put(mainType, classes);
                return classes;
            } else {
                return new HashSet<>(bootstrapTypes.getOrDefault(mainType, emptySet()));
            }
        }

        private Function<Class<?>, ClassInfoList> classFinder() {
            return type -> {
                String typeName = type.getName();
                loadedTypes.putIfAbsent(typeName, true);
                if (type.isAnnotation()) {
                    return scanResult.getClassesWithAnnotation(typeName);
                } else if (type.isInterface()) {
                    return scanResult.getClassesImplementing(typeName);
                } else {
                    return scanResult.getSubclasses(typeName);
                }
            };
        }

        public String toJSON(List<String> resources) {
            JsonObject bootstrap = new JsonObject();
            bootstrap.put("resources", new JsonArray(resources));
            bootstrap.put("vaadinVerticles", new JsonArray(new ArrayList<>(vaadinVerticles)));
            JsonObject types = new JsonObject();
            bootstrapTypes.forEach((k, v) -> types.put(
                k.getName(), new JsonArray(v.stream().map(Class::getName).collect(Collectors.toList()))
            ));
            bootstrap.put("initializers", types);
            bootstrap.put("loadedTypes", new JsonArray(
                loadedTypes.entrySet().stream()
                    .filter(Map.Entry::getValue).map(Map.Entry::getKey)
                    .collect(Collectors.toList())
            ));
            return bootstrap.toString();
        }

        public static BootstrapTypes fromJSON(JsonObject json) throws ClassNotFoundException {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Map<String, Class<?>> loaded = new HashMap<>();
            for (Object typeName : json.getJsonArray("loadedTypes")) {
                logger.trace("BootstrapTypes::fromJSON loading {}", typeName.toString());
                Class<?> type = Class.forName(typeName.toString(), false, classLoader);
                loaded.put(typeName.toString(), type);
                logger.trace("BootstrapTypes::fromJSON {} loaded", typeName.toString());
            }
            Map<Class<?>, Set<Class<?>>> types = new HashMap<>();
            JsonObject initializers = json.getJsonObject("initializers");
            for (String initializerTypeName : initializers.fieldNames()) {
                JsonArray knownTypesArray = initializers.getJsonArray(initializerTypeName);
                Set<Class<?>> knownTypes = new HashSet<>();
                for (int i = 0; i < knownTypesArray.size(); i++) {
                    knownTypes.add(loaded.get(knownTypesArray.getString(i)));
                }
                types.put(loaded.get(initializerTypeName), knownTypes);
            }
            BootstrapTypes instance = new BootstrapTypes(true);
            loaded.keySet().forEach(t -> instance.loadedTypes.put(t, true));
            instance.bootstrapTypes.putAll(types);
            instance.resources.addAll((List<String>) json.getJsonArray("resources", new JsonArray()).getList());
            instance.vaadinVerticles.addAll((List<String>) json.getJsonArray("vaadinVerticles", new JsonArray()).getList());
            logger.trace("BootstrapTypes::fromJSON initializers: {}, verticles: {}", instance.bootstrapTypes, instance.vaadinVerticles);
            return instance;
        }

    }

    @SuppressWarnings("unused")
    private static void tryLoadStaticScan() {
        logger.trace("BootstrapTypes::tryLoadStaticScan");
        InputStream scanResultStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(JSON_SCAN_RESULT);
        if (scanResultStream != null) {
            logger.trace("BootstrapTypes::tryLoadStaticScan JSON resource found");
            try (Scanner scanner = new Scanner(scanResultStream).useDelimiter("\\A")) {
                String json = scanner.next();
                Field field = BootstrapHelper.class.getDeclaredField("STATIC_SCAN_RESULT");
                field.setAccessible(true);
                field.set(null, BootstrapTypes.fromJSON(new JsonObject(json)));
                logger.trace("BootstrapTypes::tryLoadStaticScan static resources loaded");
            } catch (Exception ex) {
                logger.error("BootstrapTypes::tryLoadStaticScan failure loading static resources from JSON", ex);
            }
        }
    }

}
