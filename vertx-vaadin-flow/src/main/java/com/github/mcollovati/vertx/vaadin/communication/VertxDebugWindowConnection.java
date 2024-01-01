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
package com.github.mcollovati.vertx.vaadin.communication;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.vaadin.base.devserver.DebugWindowMessage;
import com.vaadin.base.devserver.FeatureFlagMessage;
import com.vaadin.base.devserver.ServerInfo;
import com.vaadin.base.devserver.stats.DevModeUsageStatistics;
import com.vaadin.experimental.FeatureFlags;
import com.vaadin.pro.licensechecker.LicenseChecker;
import com.vaadin.pro.licensechecker.Product;
import elemental.json.JsonObject;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mcollovati.vertx.vaadin.VertxVaadinService;
import com.github.mcollovati.vertx.vaadin.sockjs.communication.VertxVaadinLiveReload;

public class VertxDebugWindowConnection implements VertxVaadinLiveReload {

    private static final Logger logger = LoggerFactory.getLogger(VertxDebugWindowConnection.class);

    private VertxVaadinService service;
    private final Map<String, Consumer<String>> liveReload = new ConcurrentHashMap<>();

    public VertxDebugWindowConnection() {}

    public VertxDebugWindowConnection(VertxVaadinService service) {
        this.service = service;
    }

    public void attachService(VertxVaadinService service) {
        this.service = service;
    }

    public void onConnect(String websocketId, Consumer<String> producer) {
        this.liveReload.put(websocketId, producer);
        producer.accept("{\"command\": \"hello\"}");
        send(websocketId, "serverInfo", new ServerInfo());
        send(
                websocketId,
                "featureFlags",
                new FeatureFlagMessage(FeatureFlags.get(service.getContext()).getFeatures().stream()
                        .filter(feature -> !feature.equals(FeatureFlags.EXAMPLE))
                        .collect(Collectors.toList())));
    }

    public void onMessage(String websocketId, String message) {
        if (message.isEmpty()) {
            logger.debug("Received live reload heartbeat");
            return;
        }
        elemental.json.JsonObject json = elemental.json.Json.parse(message);
        String command = json.getString("command");
        if ("setFeature".equals(command)) {
            elemental.json.JsonObject data = json.getObject("data");
            FeatureFlags.get(service.getContext()).setEnabled(data.getString("featureId"), data.getBoolean("enabled"));
        } else if ("reportTelemetry".equals(command)) {
            elemental.json.JsonObject data = json.getObject("data");
            DevModeUsageStatistics.handleBrowserData(data);
        } else if ("checkLicense".equals(command)) {
            JsonObject data = json.getObject("data");
            String name = data.getString("name");
            String version = data.getString("version");
            Product product = new Product(name, version);
            boolean ok;
            String errorMessage = "";

            try {
                LicenseChecker.checkLicense(product.getName(), product.getVersion(), keyUrl -> {
                    send(websocketId, "license-check-nokey", new ProductAndMessage(product, keyUrl));
                });
                ok = true;
            } catch (Exception e) {
                ok = false;
                errorMessage = e.getMessage();
            }
            if (ok) {
                send(websocketId, "license-check-ok", product);
            } else {
                ProductAndMessage pm = new ProductAndMessage(product, errorMessage);
                send(websocketId, "license-check-failed", pm);
            }
        } else {
            getLogger().info("Unknown command from the browser: " + command);
        }
    }

    private void send(String websocketId, String command, Object data) {
        try {
            Optional.ofNullable(liveReload.get(websocketId))
                    .ifPresent(producer -> producer.accept(Json.encode(new DebugWindowMessage(command, data))));
        } catch (Exception e) {
            getLogger().error("Error sending message", e);
        }
    }

    public void onClose(String websocketId) {
        logger.debug("Live reload connection disconnected for {}", websocketId);
        // Optional.ofNullable(liveReload.get(websocketId)).ifPresent(MessageProducer::close);
        liveReload.remove(websocketId);
    }

    public void reload() {
        liveReload.values().stream()
                .filter(Objects::nonNull)
                .forEach(socket -> socket.accept("{\"command\": \"reload\"}"));
    }

    private void sendDebugWindowCommand(ServerWebSocket webSocket, String command, Object data) {
        try {
            webSocket.writeTextMessage(Json.encode(new DebugWindowMessage(command, data)));
        } catch (Exception e) {
            logger.error("Error sending message", e);
        }
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(VertxDebugWindowConnection.class.getName());
    }

    static class ProductAndMessage implements Serializable {
        private Product product;
        private String message;

        public ProductAndMessage(Product product, String message) {
            this.product = product;
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public Product getProduct() {
            return product;
        }
    }
}
