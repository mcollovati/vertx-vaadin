/*
 * The MIT License
 * Copyright © 2016-2018 Marco Collovati (mcollovati@gmail.com)
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

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.component.page.Inline;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinServletConfiguration;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.startup.AnnotationValidator;
import com.vaadin.flow.server.startup.ErrorNavigationTargetInitializer;
import com.vaadin.flow.server.startup.RouteRegistryInitializer;
import com.vaadin.flow.shared.ApplicationConstants;
import com.vaadin.flow.theme.Theme;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.VertxException;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by marco on 16/07/16.
 */
public class VaadinVerticle extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(VaadinVerticle.class);

    private HttpServer httpServer;
    private VertxVaadinService vaadinService;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        log.info("Starting vaadin verticle " + getClass().getName());

        VaadinVerticleConfiguration vaadinVerticleConfiguration = getClass().getAnnotation(VaadinVerticleConfiguration.class);

        JsonObject vaadinConfig = new JsonObject();

        Optional.ofNullable(vaadinVerticleConfiguration)
            .map(VaadinVerticleConfiguration::serviceName)
            .ifPresent(serviceName -> vaadinConfig.put("serviceName", serviceName));
        vaadinConfig.put("mountPoint", Optional.ofNullable(vaadinVerticleConfiguration)
            .map(VaadinVerticleConfiguration::mountPoint).orElse("/")
        );
        Optional.ofNullable(vaadinVerticleConfiguration).map(VaadinVerticleConfiguration::basePackages)
            .map(pkgs -> new JsonArray(Arrays.asList(pkgs)))
            .ifPresent(pkgs -> vaadinConfig.put("flowBasePackages", pkgs));
        readUiFromEnclosingClass(vaadinConfig);
        readConfigurationAnnotation(vaadinConfig);
        vaadinConfig.mergeIn(config().getJsonObject("vaadin", new JsonObject()));

        String mountPoint = vaadinConfig.getString("mountPoint");
        vaadinConfig.put(ApplicationConstants.CONTEXT_ROOT_URL, mountPoint);

        initFlow(vaadinConfig);

        VertxVaadin vertxVaadin = createVertxVaadin(vaadinConfig);
        vaadinService = vertxVaadin.vaadinService();

        HttpServerOptions serverOptions = new HttpServerOptions().setCompressionSupported(true);
        httpServer = vertx.createHttpServer(serverOptions);

        Router router = Router.router(vertx);
        router.mountSubRouter(mountPoint, vertxVaadin.router());

        Integer httpPort = httpPort();
        httpServer.requestHandler(router::accept).listen(httpPort);

        serviceInitialized(vaadinService, router);

        log.info("Started vaadin verticle " + getClass().getName() + " on port " + httpPort);
        startFuture.complete();
    }

    private Integer httpPort() throws IOException {
        Integer httpPort = config().getInteger("httpPort", 8080);
        if (httpPort == 0) {
            try (ServerSocket socket = new ServerSocket(0)) {
                httpPort = socket.getLocalPort();
            }
        }
        return httpPort;
    }

    protected VertxVaadin createVertxVaadin(JsonObject vaadinConfig) {
        return VertxVaadin.create(vertx, vaadinConfig);
    }

    protected void serviceInitialized(VertxVaadinService service, Router router) {
        // empty by default
    }


    @Override
    public void stop() throws Exception {
        log.info("Stopping vaadin verticle " + getClass().getName());
        try {
            vaadinService.destroy();
        } catch (Exception ex) {
            log.error("Error during Vaadin service destroy", ex);
        }
        httpServer.close();
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

    @SuppressWarnings("unchecked")
    private void initFlow(JsonObject vaadinConfig) throws ServletException {
        List<String> pkgs = vaadinConfig.getJsonArray("flowBasePackages", new JsonArray()).getList();
        boolean isDebug = vaadinConfig.getBoolean("debug", false);

        Map<Class<?>, Set<Class<?>>> map = new HashMap<>();

        log.debug("Scanning packages {}", String.join(", ", pkgs));

        Function<Class<?>[], ClassInfoList.ClassInfoFilter> annotationFilterFactory = annotationClazzes -> {
            List<String> clazzNames = Stream.of(annotationClazzes).map(Class::getName).collect(Collectors.toList());
            return classInfo -> clazzNames.stream().anyMatch(classInfo::hasAnnotation);
        };


        ClassGraph classGraph = new ClassGraph();
        if (isDebug) {
            classGraph.verbose();
        }
        classGraph.ignoreParentClassLoaders()
            .enableClassInfo()
            .enableAnnotationInfo()
            .whitelistPackages(pkgs.toArray(new String[0]))
            .ignoreParentClassLoaders()
            .removeTemporaryFilesAfterScan();
        try (ScanResult scanResult = classGraph.scan()) {

            map.put(RouteRegistryInitializer.class, new HashSet<>(
                scanResult.getAllClasses()
                    .filter(annotationFilterFactory.apply(new Class[]{Route.class, RouteAlias.class}))
                    .loadClasses()
            ));
            map.put(AnnotationValidator.class, new HashSet<>(
                scanResult.getAllClasses()
                    .filter(annotationFilterFactory.apply(new Class[]{
                        Viewport.class, BodySize.class, Inline.class, Theme.class, Push.class
                    })).loadClasses()
            ));
            map.put(ErrorNavigationTargetInitializer.class, new HashSet<>(
                scanResult.getClassesImplementing(HasErrorParameter.class.getName())
                    .loadClasses()
            ));
        }

        StubServletContext servletContext = new StubServletContext(vertx);

        new RouteRegistryInitializer().onStartup(map.get(RouteRegistryInitializer.class), servletContext);
        new ErrorNavigationTargetInitializer().onStartup(map.get(ErrorNavigationTargetInitializer.class), servletContext);
        new AnnotationValidator().onStartup(map.get(AnnotationValidator.class), servletContext);
    }

}
