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
package com.github.mcollovati.vertx.vaadin.quarkus.it;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import io.quarkus.runtime.StartupEvent;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.github.mcollovati.vertx.quarkus.VaadinVerticleConfigurer;
import com.github.mcollovati.vertx.quarkus.VaadinVerticleDeployer;

import static com.github.mcollovati.vertx.quarkus.VaadinVerticleDeployer.errorHandler;

@ApplicationScoped
public class TestVerticleDeployer {

    @ConfigProperty(name = "quarkus.http.test-port", defaultValue = "8888")
    int serverPort;

    public void init(@Observes StartupEvent e, Vertx vertx, TestVerticle verticle) {
        VaadinVerticleDeployer.startVerticle(vertx, verticle, vaadinVerticleConfigurer(), errorHandler(verticle));
    }

    VaadinVerticleConfigurer vaadinVerticleConfigurer() {
        return config -> {
            config.put("server", new JsonObject().put("port", serverPort));
        };
    }
}
