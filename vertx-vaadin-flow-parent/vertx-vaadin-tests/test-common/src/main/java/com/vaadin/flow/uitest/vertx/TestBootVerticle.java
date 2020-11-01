package com.vaadin.flow.uitest.vertx;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.github.mcollovati.vertx.vaadin.VaadinVerticle;
import com.github.mcollovati.vertx.vaadin.VertxVaadinService;
import com.vaadin.flow.server.DevModeHandler;
import com.vaadin.flow.server.VaadinService;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;
import nonapi.io.github.classgraph.utils.ReflectionUtils;

public class TestBootVerticle extends VaadinVerticle {

    private static final Map<String, ViewClassLocator> viewLocators = new HashMap<>();

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
        router.get("/__check-start").handler(ctx -> {
            System.out.println("======================== check start");
            HttpServerResponse response = ctx.response();
            DevModeHandler devModeHandler = DevModeHandler.getDevModeHandler();
            response.setStatusCode(404);
            if (devModeHandler != null) {
                System.out.println("======================== check start. dev mod 1");
                try {
                    Field startFutureField = devModeHandler.getClass().getDeclaredField("devServerStartFuture");
                    startFutureField.setAccessible(true);
                    CompletableFuture<?> o = (CompletableFuture)startFutureField.get(devModeHandler);
                    if (o.isDone()) {
                        System.out.println("======================== check start. dev mod 2");
                        response.setStatusCode(200);
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                    System.out.println("======================== check start. dev mod err ");
                }
            }
            System.out.println("======================== check start -> " + response.getStatusCode());
            response.end();
        });
    }

    public static ViewClassLocator getViewLocator(VaadinService vaadinService) {
        String deploymentID = ((VertxVaadinService) vaadinService).getVertx().getOrCreateContext().deploymentID();
        return TestBootVerticle.viewLocators.get(deploymentID);
    }

}
