package com.vaadin.flow.uitest.vertx;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.github.mcollovati.vertx.vaadin.VaadinVerticle;
import com.github.mcollovati.vertx.vaadin.VertxVaadinService;
import com.vaadin.flow.internal.DevModeHandler;
import com.vaadin.flow.internal.DevModeHandlerManager;
import com.vaadin.flow.server.VaadinService;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestBootVerticle extends VaadinVerticle {

    private static final Map<String, ViewClassLocator> viewLocators = new HashMap<>();
    public static final Logger LOGGER = LoggerFactory.getLogger(TestBootVerticle.class);

    @Override
    protected void serviceInitialized(VertxVaadinService service, Router router) {
        String mountPoint = service.getVaadinOptions().mountPoint();
        config().getJsonArray("mountAliases", new JsonArray())
            .stream()
            .map(String.class::cast)
            .forEach(alias -> router.routeWithRegex(alias + "/.*").handler(ctx -> {

                ctx.reroute(ctx.request().uri()
                    .replaceFirst(alias + "/", mountPoint + "/"));
            }));
        TestBootVerticle.viewLocators.put(deploymentID(), new ViewClassLocator(getClass().getClassLoader()));
        router.get("/__check-start").order(0).handler(ctx -> {
            LOGGER.trace("======================== check start");
            HttpServerResponse response = ctx.response();
            DevModeHandler devModeHandler = DevModeHandlerManager.getDevModeHandler(service).orElse(null);
            response.setStatusCode(404);
            if (devModeHandler != null) {
                LOGGER.trace("======================== check start. dev mod 1");
                try {
                    Method isRunningMethod = findIsRunningMethod(devModeHandler.getClass());
                    boolean isRunning = (boolean) isRunningMethod.invoke(devModeHandler);
                    if (isRunning) {
                        LOGGER.info("DevModeHandler ready");
                        response.setStatusCode(200);
                    }
                } catch (NoSuchFieldException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    LOGGER.trace("======================== check start. dev mod err ");
                }
            } else if (service.getDeploymentConfiguration().isProductionMode()) {
                response.setStatusCode(200);
            }
            LOGGER.trace("======================== check start -> " + response.getStatusCode());
            response.end();
        });
    }

    private Method findIsRunningMethod(Class<?> clazz) throws NoSuchFieldException {
        do {
            try {
                Method method = clazz.getDeclaredMethod("isRunning");
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException e) {
                clazz = clazz.getSuperclass();
            }
        }
        while (clazz != Object.class);
        throw new NoSuchFieldException("Cannot find devServerStartFuture");
    }

    public static ViewClassLocator getViewLocator(VaadinService vaadinService) {
        String deploymentID = ((VertxVaadinService) vaadinService).getVertx().getOrCreateContext().deploymentID();
        return TestBootVerticle.viewLocators.get(deploymentID);
    }

}
