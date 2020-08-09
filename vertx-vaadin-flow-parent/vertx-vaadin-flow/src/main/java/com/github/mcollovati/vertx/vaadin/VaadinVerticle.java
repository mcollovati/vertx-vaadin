/*
 * The MIT License
 * Copyright © 2016-2020 Marco Collovati (mcollovati@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.mcollovati.vertx.vaadin;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.mcollovati.vertx.support.StartupContext;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.Constants;
import com.vaadin.flow.server.DevModeHandler;
import com.vaadin.flow.server.VaadinServletConfiguration;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.frontend.FrontendUtils;
import com.vaadin.flow.server.startup.AnnotationValidator;
import com.vaadin.flow.server.startup.DevModeInitializer;
import com.vaadin.flow.server.startup.ErrorNavigationTargetInitializer;
import com.vaadin.flow.server.startup.RouteRegistryInitializer;
import com.vaadin.flow.server.startup.WebComponentConfigurationRegistryInitializer;
import com.vaadin.flow.server.startup.WebComponentExporterAwareValidator;
import com.vaadin.flow.shared.ApplicationConstants;
import elemental.json.impl.JsonUtil;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.VertxException;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.vaadin.flow.server.Constants.SERVLET_PARAMETER_COMPATIBILITY_MODE;
import static com.vaadin.flow.server.Constants.SERVLET_PARAMETER_PRODUCTION_MODE;
import static com.vaadin.flow.server.Constants.VAADIN_PREFIX;
import static com.vaadin.flow.server.Constants.VAADIN_SERVLET_RESOURCES;
import static com.vaadin.flow.server.frontend.FrontendUtils.PARAM_TOKEN_FILE;
import static com.vaadin.flow.server.frontend.FrontendUtils.PROJECT_BASEDIR;
import static com.vaadin.flow.server.frontend.FrontendUtils.TOKEN_FILE;
import static java.util.Arrays.asList;

/**
 * Created by marco on 16/07/16.
 */
public class VaadinVerticle extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(VaadinVerticle.class);

    private HttpServer httpServer;
    private VertxVaadinService vaadinService;

    @Override
    public void start(Promise<Void> startFuture) throws Exception {
        log.info("Starting vaadin verticle " + getClass().getName());

        prepareConfig()
            .compose(vaadinOptions -> StartupContext.of(vertx, vaadinOptions))
            .compose(this::initVertxVaadin)
            .compose(this::startupHttpServer)
            .<Void>map(router -> {
                serviceInitialized(vaadinService, router);
                return null;
            }).setHandler(startFuture);

        // Perform potential synchronous startup tasks
        start();
    }

    private Future<Router> startupHttpServer(VertxVaadin vertxVaadin) {
        String mountPoint = vertxVaadin.config().mountPoint();
        Router router = Router.router(vertx);
        router.mountSubRouter(mountPoint, vertxVaadin.router());
        log.debug("Mounted Vaadin router on {}", mountPoint);

        HttpServerOptions serverOptions = new HttpServerOptions(
            config().getJsonObject("server", new JsonObject())
        ).setCompressionSupported(true);

        httpServer = vertx.createHttpServer(serverOptions).requestHandler(router);
        Promise<Router> promise = Promise.promise();

        httpPort().flatMap(port -> {
            Promise<HttpServer> p = Promise.promise();
            httpServer.listen(port, p);
            return p.future();
        }).compose(srv -> {
            log.info("Started Vaadin verticle {} on port {}", getClass().getName(), srv.actualPort());
            return Future.succeededFuture(router);
        }, t -> {
            log.error("Cannot start http server", t);
            return Future.failedFuture(t);
        }).setHandler(promise);
        return promise.future();
    }

    private Future<Integer> httpPort() {
        Promise<Integer> promise = Promise.promise();
        // search first new port key server.port, then, for backward compatibility
        // fallback to httpPort
        Integer httpPort = config().getJsonObject("server", new JsonObject())
            .getInteger("port", config().getInteger("httpPort", 8080));
        if (httpPort == 0) {
            try (ServerSocket socket = new ServerSocket(0)) {
                promise.complete(socket.getLocalPort());
            } catch (Exception e) {
                promise.fail(e);
            }
        } else {
            promise.complete(httpPort);
        }
        return promise.future();
    }

    protected VertxVaadin createVertxVaadin(StartupContext startupContext) {
        return VertxVaadin.create(vertx, startupContext);
    }

    protected void serviceInitialized(VertxVaadinService service, Router router) {
        // empty by default
    }

    protected Future<VaadinOptions> prepareConfig() {
        JsonObject vaadinConfig = new JsonObject();
        VaadinVerticleConfiguration vaadinVerticleConfiguration = getClass().getAnnotation(VaadinVerticleConfiguration.class);

        Optional.ofNullable(vaadinVerticleConfiguration)
            .map(VaadinVerticleConfiguration::serviceName)
            .ifPresent(serviceName -> vaadinConfig.put("serviceName", serviceName));
        vaadinConfig.put("mountPoint", Optional.ofNullable(vaadinVerticleConfiguration)
            .map(VaadinVerticleConfiguration::mountPoint).orElse("/")
        );
        Optional.ofNullable(vaadinVerticleConfiguration).map(VaadinVerticleConfiguration::basePackages)
            .map(pkgs -> new JsonArray(asList(pkgs)))
            .ifPresent(pkgs -> vaadinConfig.put("flowBasePackages", pkgs));
        readUiFromEnclosingClass(vaadinConfig);
        readConfigurationAnnotation(vaadinConfig);
        readBuildInfo(vaadinConfig);
        vaadinConfig.mergeIn(config().getJsonObject("vaadin", new JsonObject()));

        String mountPoint = vaadinConfig.getString("mountPoint");
        vaadinConfig.put(ApplicationConstants.CONTEXT_ROOT_URL, mountPoint);
        vaadinConfig.put(Constants.SERVLET_PARAMETER_PUSH_URL, mountPoint);
        vaadinConfig.put(Constants.DISABLE_AUTOMATIC_SERVLET_REGISTRATION, true);

        return Future.succeededFuture(new VaadinOptions(vaadinConfig));
    }

    @Override
    public void stop(Promise<Void> stopFuture) {
        log.info("Stopping vaadin verticle " + getClass().getName());
        try {
            vaadinService.destroy();
        } catch (Exception ex) {
            log.error("Error during Vaadin service destroy", ex);
        }

        DevModeHandler handler = DevModeHandler.getDevModeHandler();
        if (handler != null && !handler.reuseDevServer()) {
            handler.stop();
        }

        try {
            // Perform potential synchronous clean-up tasks
            stop();
        } catch (Exception ex) {
            log.error("Error during Verticle synchronous stop", ex);
        }
        httpServer.close(stopFuture);
        log.info("Stopped vaadin verticle " + getClass().getName());
    }

    // From VaadinServlet
    private void readUiFromEnclosingClass(JsonObject vaadinConfig) {
        Class<?> enclosingClass = getClass().getEnclosingClass();

        if (enclosingClass != null && UI.class.isAssignableFrom(enclosingClass)) {
            vaadinConfig.put(VaadinSession.UI_PARAMETER, enclosingClass.getName());
        }
    }

    // From VaadinServlet
    private void readConfigurationAnnotation(JsonObject vaadinConfig) {

        VaadinServletConfiguration configAnnotation = getClass().getAnnotation(VaadinServletConfiguration.class);
        if (configAnnotation != null) {
            Method[] methods = VaadinServletConfiguration.class
                .getDeclaredMethods();
            for (Method method : methods) {
                VaadinServletConfiguration.InitParameterName name =
                    method.getAnnotation(VaadinServletConfiguration.InitParameterName.class);
                assert name !=
                    null : "All methods declared in VaadinServletConfiguration should have a @InitParameterName annotation";

                try {
                    Object value = method.invoke(configAnnotation);

                    String stringValue;
                    if (value instanceof Class<?>) {
                        stringValue = ((Class<?>) value).getName();
                    } else {
                        stringValue = value.toString();
                    }

                    vaadinConfig.put(name.value(), stringValue);
                } catch (Exception e) {
                    // This should never happen
                    throw new VertxException(
                        "Could not read @VaadinServletConfiguration value "
                            + method.getName(), e);
                }
            }
        }
    }

    private Future<VertxVaadin> initVertxVaadin(StartupContext startupContext) {
        VaadinOptions vaadinConfig = startupContext.vaadinOptions();
        List<String> pkgs = vaadinConfig.flowBasePackages();
        if (!pkgs.isEmpty()) {
            pkgs.add("com.vaadin");
            pkgs.add("com.github.mcollovati.vertx.vaadin");
        }
        boolean isDebug = vaadinConfig.debug();

        Map<Class<?>, Set<Class<?>>> map = new HashMap<>();

        log.debug("Scanning packages {}", String.join(", ", pkgs));

        Promise<VertxVaadin> promise = Promise.promise();
        vertx.executeBlocking(event -> {

            ClassGraph classGraph = new ClassGraph();
            if (isDebug) {
                classGraph.verbose();
            }
            classGraph.ignoreParentClassLoaders()
                .enableClassInfo()
                .ignoreClassVisibility()
                .enableAnnotationInfo()
                .whitelistPackages(pkgs.toArray(new String[0]))
                .ignoreParentClassLoaders()
                .removeTemporaryFilesAfterScan();
            try (ScanResult scanResult = classGraph.scan()) {
                map.putAll(seekRequiredClasses(scanResult));

                boolean haSockJS = scanResult.getClassInfo("com.github.mcollovati.vertx.vaadin.sockjs.communication.SockJSPushConnection") != null;
                vaadinConfig.sockJSSupport(haSockJS);
            }

            Promise<Void> initializerFuture = Promise.promise();
            runInitializers(startupContext, initializerFuture, map);
            initializerFuture.future().map(unused -> {
                VertxVaadin vertxVaadin = createVertxVaadin(startupContext);
                vaadinService = vertxVaadin.vaadinService();
                return vertxVaadin;
            }).setHandler(event);
        }, promise);
        return promise.future();
    }

    private void runInitializers(StartupContext startupContext, Promise<Void> promise, Map<Class<?>, Set<Class<?>>> classes) {
        Function<ServletContainerInitializer, Handler<Promise<Void>>> initializerFactory = initializer -> event2 -> {
            try {
                initializer.onStartup(classes.get(initializer.getClass()), startupContext.servletContext());
                event2.complete();
            } catch (Exception ex) {
                event2.fail(ex);
            }
        };

        CompositeFuture.join(
            runInitializer(initializerFactory.apply(new RouteRegistryInitializer())),
            runInitializer(initializerFactory.apply(new ErrorNavigationTargetInitializer())),
            runInitializer(initializerFactory.apply(new WebComponentConfigurationRegistryInitializer())),
            runInitializer(initializerFactory.apply(new AnnotationValidator())),
            runInitializer(initializerFactory.apply(new WebComponentExporterAwareValidator())),
            runInitializer(event2 -> initializeDevModeHandler(getClass(), event2, startupContext, classes.get(DevModeInitializer.class)))
        ).setHandler(event2 -> {
            if (event2.succeeded()) {
                promise.complete();
            } else {
                promise.fail(event2.cause());
            }
        });
    }

    private static synchronized void initializeDevModeHandler(Class<?> verticleClass, Promise<Object> promise,
                                                              StartupContext startupContext, Set<Class<?>> classes) {
        DevModeHandler devModeHandler = DevModeHandler.getDevModeHandler();
        if (devModeHandler == null) {
            try {
                DevModeInitializer.initDevModeHandler(classes, startupContext.servletContext(),
                    DeploymentConfigurationFactory.createDeploymentConfiguration(verticleClass, startupContext.vaadinOptions())
                );
                promise.complete(DevModeHandler.getDevModeHandler());
            } catch (ServletException e) {
                promise.fail(e);
            }
        } else {
            log.info("DevModeHandler already running on port {}", devModeHandler.getPort());
            promise.complete(devModeHandler);
        }
    }

    private Map<Class<?>, Set<Class<?>>> seekRequiredClasses(ScanResult scanResult) {
        Map<Class<?>, Set<Class<?>>> map = new HashMap<>();
        Stream.of(
            RouteRegistryInitializer.class, AnnotationValidator.class, ErrorNavigationTargetInitializer.class,
            WebComponentConfigurationRegistryInitializer.class, WebComponentExporterAwareValidator.class,
            DevModeInitializer.class
        ).forEach(type -> registerHandledTypes(scanResult, type, map));
        return map;
    }

    private void registerHandledTypes(ScanResult scanResult, Class<?> initializerClass, Map<Class<?>, Set<Class<?>>> map) {

        HandlesTypes handledTypes = initializerClass.getAnnotation(HandlesTypes.class);
        if (handledTypes != null) {
            Function<Class<?>, ClassInfoList> classFinder = type -> {
                if (type.isAnnotation()) {
                    return scanResult.getClassesWithAnnotation(type.getCanonicalName());
                } else if (type.isInterface()) {
                    return scanResult.getClassesImplementing(type.getCanonicalName());
                } else {
                    return scanResult.getSubclasses(type.getCanonicalName());
                }
            };

            Set<Class<?>> classes = Stream.of(handledTypes.value())
                .map(classFinder)
                .flatMap(c -> c.loadClasses().stream())
                .collect(Collectors.toSet());

            if (!classes.isEmpty()) {
                map.put(initializerClass, classes);
            }
        }
    }

    private <T> Future<T> runInitializer(Handler<Promise<T>> op) {
        Promise<T> promise = Promise.promise();
        context.executeBlocking(op, promise);
        return promise.future();
    }

    private void readBuildInfo(JsonObject config) { // NOSONAR
        try {
            String json = null;
            // token file location passed via init parameter property
            String tokenLocation = config.getString(PARAM_TOKEN_FILE);
            if (tokenLocation != null) {
                File tokenFile = new File(tokenLocation);
                if (tokenFile.canRead()) {
                    json = FileUtils.readFileToString(tokenFile, "UTF-8");
                }
            }

            // token file is in the class-path of the application
            if (json == null) {
                URL resource = VaadinVerticle.class
                    .getClassLoader()
                    .getResource(VAADIN_SERVLET_RESOURCES + TOKEN_FILE);
                if (resource != null) {
                    json = FrontendUtils.streamToString(resource.openStream());
                }
            }

            // Read the json and set the appropriate system properties if not
            // already set.
            if (json != null) {
                elemental.json.JsonObject buildInfo = JsonUtil.parse(json);
                if (buildInfo.hasKey(SERVLET_PARAMETER_PRODUCTION_MODE)) {
                    config.put(
                        SERVLET_PARAMETER_PRODUCTION_MODE,
                        String.valueOf(buildInfo.getBoolean(
                            SERVLET_PARAMETER_PRODUCTION_MODE)));
                    // Need to be sure that we remove the system property,
                    // because
                    // it has priority in the configuration getter
                    System.clearProperty(
                        VAADIN_PREFIX + SERVLET_PARAMETER_PRODUCTION_MODE);
                }
                if (buildInfo.hasKey(SERVLET_PARAMETER_COMPATIBILITY_MODE)) {
                    config.put(
                        SERVLET_PARAMETER_COMPATIBILITY_MODE,
                        String.valueOf(buildInfo.getBoolean(
                            SERVLET_PARAMETER_COMPATIBILITY_MODE)));
                    // Need to be sure that we remove the system property,
                    // because it has priority in the configuration getter
                    System.clearProperty(VAADIN_PREFIX
                        + SERVLET_PARAMETER_COMPATIBILITY_MODE);
                }
                if (System.getProperty(PROJECT_BASEDIR) == null
                    && buildInfo.hasKey("npmFolder")) {
                    System.setProperty(PROJECT_BASEDIR,
                        buildInfo.getString("npmFolder"));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
