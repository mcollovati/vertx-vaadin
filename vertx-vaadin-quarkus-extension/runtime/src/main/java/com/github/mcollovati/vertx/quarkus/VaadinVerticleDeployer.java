/*
 * The MIT License
 * Copyright © 2024 Marco Collovati (mcollovati@gmail.com)
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
package com.github.mcollovati.vertx.quarkus;

import io.quarkus.logging.Log;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.github.mcollovati.vertx.vaadin.VaadinVerticle;

public class VaadinVerticleDeployer {

    public static Handler<Throwable> errorHandler(VaadinVerticle verticle) {
        String errorMessage =
                "Error starting Vaadin Verticle " + verticle.getClass().getName();
        return error -> Log.error(VaadinVerticleDeployer.class.getName(), errorMessage, error);
    }

    public static void startVerticle(
            Vertx vertx,
            VaadinVerticle verticle,
            VaadinVerticleConfigurer configurer,
            Handler<Throwable> errorHandler) {
        DeploymentOptions deploymentOptions = new DeploymentOptions();
        JsonObject config = new JsonObject();
        configurer.apply(config);
        deploymentOptions.setConfig(config);
        vertx.deployVerticle(verticle, deploymentOptions).onFailure(errorHandler);
    }
}
