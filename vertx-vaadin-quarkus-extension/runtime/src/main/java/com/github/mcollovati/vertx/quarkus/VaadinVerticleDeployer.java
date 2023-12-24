package com.github.mcollovati.vertx.quarkus;

import com.github.mcollovati.vertx.vaadin.VaadinVerticle;
import io.quarkus.logging.Log;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class VaadinVerticleDeployer {

    public static Handler<Throwable> errorHandler(VaadinVerticle verticle) {
        String errorMessage = "Error starting Vaadin Verticle "
                + verticle.getClass().getName();
        return error -> Log.error(VaadinVerticleDeployer.class.getName(),
                errorMessage, error);
    }

    public static void startVerticle(Vertx vertx, VaadinVerticle verticle,
            VaadinVerticleConfigurer configurer,
            Handler<Throwable> errorHandler) {
        DeploymentOptions deploymentOptions = new DeploymentOptions();
        JsonObject config = new JsonObject();
        configurer.apply(config);
        deploymentOptions.setConfig(config);
        vertx.deployVerticle(verticle, deploymentOptions)
                .onFailure(errorHandler);
    }

}
