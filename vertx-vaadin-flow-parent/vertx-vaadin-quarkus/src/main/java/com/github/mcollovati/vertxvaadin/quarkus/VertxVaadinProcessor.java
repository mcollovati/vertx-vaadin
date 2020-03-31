package com.github.mcollovati.vertxvaadin.quarkus;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.github.mcollovati.vertx.support.BootstrapHelper;
import com.github.mcollovati.vertx.vaadin.VaadinOptions;
import com.github.mcollovati.vertx.vaadin.VaadinVerticle;
import com.github.mcollovati.vertxvaadin.quarkus.config.VertxVaadinConfiguration;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.GeneratedResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class VertxVaadinProcessor {

    VertxVaadinConfiguration config;

    @BuildStep
    FeatureBuildItem featureBuildItem() {
        System.out.println("========================= Add Feature For Vertx-Vaadin: " + config);
        return new FeatureBuildItem("quarkus-vertx-vaadin");
    }

    @Record(ExecutionTime.STATIC_INIT)
    @BuildStep(loadsApplicationClasses = true)
    void build(BuildProducer<NativeImageResourceBuildItem> resource,
               BuildProducer<ReflectiveClassBuildItem> reflectiveClasses,
               BuildProducer<RuntimeInitializedClassBuildItem> runtimeInitializedClasses,
               BuildProducer<GeneratedResourceBuildItem> resourceProducer,
               BootstrapHelperRecorder recorder) {
        System.out.println("========================== vaadin.project.basedir=" + System.getProperty("vaadin.project.basedir"));
        reflectiveClasses.produce(ReflectiveClassBuildItem.builder(
            BootstrapHelper.class.getName()
        ).constructors(true).methods(true).fields(true).finalFieldsWritable(true).build());
        VaadinOptions opts = createVaadinOptions();
        List<String> resourceNames = new ArrayList<>();
        BootstrapHelper.scanResources(opts,
            r -> {
                resourceNames.addAll(r);
                r.stream().map(NativeImageResourceBuildItem::new).forEach(resource::produce);
            },
            Throwable::printStackTrace
        );
        // Collect vaadin classes
        BootstrapHelper.scanVaadinComponents(opts,
            bootstrapTypes -> {
                VaadinVerticle.collectVaadinComponentClasses(bootstrapTypes);
                registerVaadinClasses(bootstrapTypes, reflectiveClasses);
                registerApplicationClasses(bootstrapTypes, reflectiveClasses);
                registerVaadinVerticles(bootstrapTypes, runtimeInitializedClasses);
                resourceProducer.produce(new GeneratedResourceBuildItem(
                    BootstrapHelper.JSON_SCAN_RESULT, bootstrapTypes.toJSON(resourceNames).getBytes(StandardCharsets.UTF_8)));
                resource.produce(new NativeImageResourceBuildItem(BootstrapHelper.JSON_SCAN_RESULT));
            }, Throwable::printStackTrace);


    }


    @Record(ExecutionTime.RUNTIME_INIT)
    @BuildStep
    void build2(BootstrapHelperRecorder recorder) {
        recorder.loadScanResult("Runtime init");
    }

    private void registerVaadinVerticles(BootstrapHelper.BootstrapTypes bootstrapTypes,
                                         BuildProducer<RuntimeInitializedClassBuildItem> runtimeInitializedClasses) {
        bootstrapTypes.getVaadinVerticles()
            .forEach(cl -> {
                System.out.println("============== VertxVaadin verticle " + cl);
                runtimeInitializedClasses.produce(new RuntimeInitializedClassBuildItem(cl));
            });
    }

    private void registerApplicationClasses(BootstrapHelper.BootstrapTypes bootstrapTypes,
                                            BuildProducer<ReflectiveClassBuildItem> reflectiveClasses) {
        String[] classes = VaadinVerticle.collectVaadinComponentClasses(bootstrapTypes).entrySet().stream()
            .flatMap(e -> e.getValue().stream().map(Class::getName))
            .toArray(String[]::new);
        //////System.out.println("============== Application classes " + Arrays.toString(classes));

        reflectiveClasses.produce(ReflectiveClassBuildItem.builder(classes)
            .constructors(true).build());
    }

    private static final String[] VAADIN_PACAKGES = new String[]{
        "com.vaadin.flow.component", "com.vaadin.flow.router", "com.vaadin.flow.theme"
    };
    private static final String[] EXCLUDE_CLASSNAMES = new String[] {
        "com.vaadin.flow.server.communication.AtmospherePushConnection",
        "com.vaadin.flow.server.communication.PushAtmosphereHandler",
        "com.vaadin.flow.server.communication.PushHandler",
        "com.vaadin.flow.server.communication.PushRequestHandler"
    };

    private void registerVaadinClasses(BootstrapHelper.BootstrapTypes bootstrapTypes,
                                       BuildProducer<ReflectiveClassBuildItem> reflectiveClasses) {

        String[] classes = bootstrapTypes.findClasses(className ->
            className.startsWith("com.vaadin") &&
            //Stream.of(VAADIN_PACAKGES).anyMatch(className::startsWith) &&
                Stream.of(EXCLUDE_CLASSNAMES).noneMatch(className::equals)
        ).toArray(new String[0]);
        /*
        String[] classes = bootstrapTypes.getAllClasses()
            //.filter(classInfo -> classInfo.getPackageName().startsWith("com.vaadin.flow"))
            .filter(classInfo -> Stream.of(VAADIN_PACAKGES).anyMatch(pkg -> classInfo.getName().startsWith(pkg)))
            .filter(classInfo -> !classInfo.getSimpleName().startsWith("Atmosphere"))
            .getNames()
            .toArray(new String[0]);

         */
        System.out.println("============== Vaadin classes " + Arrays.toString(classes));

        reflectiveClasses.produce(ReflectiveClassBuildItem.builder(classes)
            .constructors(true).build());
    }

    private VaadinOptions createVaadinOptions() {
        JsonObject vaadinConfig = new JsonObject();
        vaadinConfig.put("debug", config.debug);
        config.vaadin.flowBasePackages.ifPresent(pkgs ->
            vaadinConfig.put("flowBasePackages", new JsonArray(pkgs))
        );
        return new VaadinOptions(vaadinConfig);
    }

}
