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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.vaadin.base.devserver.DebugWindowMessage;
import com.vaadin.base.devserver.DevToolsInterface;
import com.vaadin.base.devserver.DevToolsMessageHandler;
import com.vaadin.base.devserver.FeatureFlagMessage;
import com.vaadin.base.devserver.IdeIntegration;
import com.vaadin.base.devserver.ServerInfo;
import com.vaadin.base.devserver.stats.DevModeUsageStatistics;
import com.vaadin.base.devserver.themeeditor.ThemeEditorCommand;
import com.vaadin.base.devserver.themeeditor.ThemeEditorMessageHandler;
import com.vaadin.base.devserver.themeeditor.messages.BaseResponse;
import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.startup.ApplicationConfiguration;
import com.vaadin.pro.licensechecker.LicenseChecker;
import com.vaadin.pro.licensechecker.Product;
import elemental.json.JsonObject;
import io.vertx.core.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mcollovati.vertx.vaadin.VertxVaadinService;
import com.github.mcollovati.vertx.vaadin.sockjs.communication.VertxVaadinLiveReload;

public class VertxDebugWindowConnection implements VertxVaadinLiveReload {

    private static final Logger logger = LoggerFactory.getLogger(VertxDebugWindowConnection.class);

    private VertxVaadinService service;
    private final Map<String, Consumer<String>> liveReload = new ConcurrentHashMap<>();

    private IdeIntegration ideIntegration;

    private ThemeEditorMessageHandler themeEditorMessageHandler;

    private List<DevToolsMessageHandler> plugins;

    public VertxDebugWindowConnection() {}

    public VertxDebugWindowConnection(VertxVaadinService service) {
        attachService(service);
    }

    public void attachService(VertxVaadinService service) {
        this.service = service;
        VaadinContext context = service.getContext();
        this.themeEditorMessageHandler = new ThemeEditorMessageHandler(context);
        this.ideIntegration = new IdeIntegration(ApplicationConfiguration.get(context));
        findPlugins();
    }

    private void findPlugins() {
        ServiceLoader<DevToolsMessageHandler> loader = ServiceLoader.load(DevToolsMessageHandler.class);
        this.plugins = new ArrayList<>();
        for (DevToolsMessageHandler s : loader) {
            this.plugins.add(s);
        }
    }

    public void onConnect(String websocketId, Consumer<String> producer) {
        this.liveReload.put(websocketId, producer);
        producer.accept("{\"command\": \"hello\"}");
        for (DevToolsMessageHandler plugin : plugins) {
            plugin.handleConnect(getDevToolsInterface(websocketId));
        }
        send(websocketId, "serverInfo", new ServerInfo());
        send(
                websocketId,
                "featureFlags",
                new FeatureFlagMessage(FeatureFlags.get(service.getContext()).getFeatures().stream()
                        .filter(feature -> !feature.equals(FeatureFlags.EXAMPLE))
                        .collect(Collectors.toList())));
        if (themeEditorMessageHandler.isEnabled()) {
            send(websocketId, ThemeEditorCommand.STATE, themeEditorMessageHandler.getState());
        }
    }

    private DevToolsInterface getDevToolsInterface(String websocketId) {
        return new DevToolsInterfaceImpl(this, websocketId);
    }

    public void onMessage(String websocketId, String message) {
        if (message.isEmpty()) {
            logger.debug("Received live reload heartbeat");
            return;
        }
        elemental.json.JsonObject json = elemental.json.Json.parse(message);
        String command = json.getString("command");
        JsonObject data = json.getObject("data");
        if ("setFeature".equals(command)) {
            FeatureFlags.get(service.getContext()).setEnabled(data.getString("featureId"), data.getBoolean("enabled"));
        } else if ("reportTelemetry".equals(command)) {
            DevModeUsageStatistics.handleBrowserData(data);
        } else if ("checkLicense".equals(command)) {
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
        } else if ("showComponentCreateLocation".equals(command) || "showComponentAttachLocation".equals(command)) {
            int nodeId = (int) data.getNumber("nodeId");
            int uiId = (int) data.getNumber("uiId");
            VaadinSession session = VaadinSession.getCurrent();
            session.access(() -> {
                Element element = session.findElement(uiId, nodeId);
                Optional<Component> c = element.getComponent();
                if (c.isPresent()) {
                    if ("showComponentCreateLocation".equals(command)) {
                        ideIntegration.showComponentCreateInIde(c.get());
                    } else {
                        ideIntegration.showComponentAttachInIde(c.get());
                    }
                } else {
                    getLogger()
                            .error(
                                    "Only component locations are tracked. The given node id refers to an element and not a component");
                }
            });
        } else if (themeEditorMessageHandler.canHandle(command, data)) {
            BaseResponse resultData = themeEditorMessageHandler.handleDebugMessageData(command, data);
            send(websocketId, ThemeEditorCommand.RESPONSE, resultData);
        } else {
            boolean handled = false;
            for (DevToolsMessageHandler plugin : plugins) {
                handled = plugin.handleMessage(command, data, getDevToolsInterface(websocketId));
                if (handled) {
                    break;
                }
            }
            if (!handled) {
                getLogger().info("Unknown command from the browser: {}", command);
            }
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
        for (DevToolsMessageHandler plugin : plugins) {
            plugin.handleDisconnect(getDevToolsInterface(websocketId));
        }
        liveReload.remove(websocketId);
    }

    public void update(String path, String content) {
        JsonObject msg = elemental.json.Json.createObject();
        msg.put("command", "update");
        msg.put("path", path);
        msg.put("content", content);
        sendToAll(msg);
    }

    public void reload() {
        JsonObject msg = elemental.json.Json.createObject();
        msg.put("command", "reload");
        sendToAll(msg);
    }

    private void sendToAll(JsonObject message) {
        String json = message.toJson();
        liveReload.values().stream().filter(Objects::nonNull).forEach(socket -> socket.accept(json));
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

    private static class DevToolsInterfaceImpl implements DevToolsInterface {

        private final VertxDebugWindowConnection connection;
        private final String websocketId;

        public DevToolsInterfaceImpl(VertxDebugWindowConnection connection, String websocketId) {
            this.connection = connection;
            this.websocketId = websocketId;
        }

        @Override
        public void send(String command, JsonObject data) {
            connection.send(websocketId, command, data);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DevToolsInterfaceImpl that = (DevToolsInterfaceImpl) o;

            if (!Objects.equals(connection, that.connection)) return false;
            return Objects.equals(websocketId, that.websocketId);
        }

        @Override
        public int hashCode() {
            int result = connection != null ? connection.hashCode() : 0;
            result = 31 * result + (websocketId != null ? websocketId.hashCode() : 0);
            return result;
        }
    }
}
