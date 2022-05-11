package com.github.mcollovati.vertx.vaadin.communication;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.github.mcollovati.vertx.vaadin.VertxVaadinService;
import com.github.mcollovati.vertx.vaadin.sockjs.communication.VertxVaadinLiveReload;
import com.vaadin.base.devserver.DebugWindowMessage;
import com.vaadin.base.devserver.FeatureFlagMessage;
import com.vaadin.base.devserver.ServerInfo;
import com.vaadin.base.devserver.stats.DevModeUsageStatistics;
import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.shared.ApplicationConstants;
import com.vaadin.pro.licensechecker.LicenseChecker;
import com.vaadin.pro.licensechecker.Product;
import elemental.json.JsonObject;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VertxDebugWindowConnection implements VertxVaadinLiveReload {

    private static final Logger logger = LoggerFactory.getLogger(VertxDebugWindowConnection.class);

    private VertxVaadinService service;
    private final Map<String, MessageProducer<String>> liveReload = new ConcurrentHashMap<>();


    public VertxDebugWindowConnection() {
    }

    public VertxDebugWindowConnection(VertxVaadinService service) {
        this.service = service;
    }

    public void attachService(VertxVaadinService service) {
        this.service = service;
    }


    public void handle(ServerWebSocket webSocket) {
        if (isDebugWindowConnection(webSocket)) {
            String websocketId = webSocket.textHandlerID();
            liveReload.put(websocketId, service.getVertx().eventBus().publisher(websocketId));
            webSocket.textMessageHandler(message -> onMessage(websocketId, message));
            webSocket.closeHandler(unused -> onClose(websocketId));

            webSocket.writeTextMessage("{\"command\": \"hello\"}");
            sendDebugWindowCommand(webSocket, "serverInfo", new ServerInfo());
            sendDebugWindowCommand(webSocket, "featureFlags", new FeatureFlagMessage(FeatureFlags
                .get(service.getContext()).getFeatures().stream()
                .filter(feature -> !feature.equals(FeatureFlags.EXAMPLE))
                .collect(Collectors.toList())));
        }
    }

    private void onMessage(String websocketId, String message) {
        if (message.isEmpty()) {
            logger.debug("Received live reload heartbeat");
            return;
        }
        elemental.json.JsonObject json = elemental.json.Json.parse(message);
        String command = json.getString("command");
        if ("setFeature".equals(command)) {
            elemental.json.JsonObject data = json.getObject("data");
            FeatureFlags.get(service.getContext()).setEnabled(data.getString("featureId"),
                data.getBoolean("enabled"));
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
                LicenseChecker.checkLicense(product.getName(),
                    product.getVersion(), keyUrl -> {
                        send(websocketId, "license-check-nokey",
                            new ProductAndMessage(product, keyUrl));
                    });
                ok = true;
            } catch (Exception e) {
                ok = false;
                errorMessage = e.getMessage();
            }
            if (ok) {
                send(websocketId, "license-check-ok", product);
            } else {
                ProductAndMessage pm = new ProductAndMessage(product,
                    errorMessage);
                send(websocketId, "license-check-failed", pm);
            }
        } else {
            getLogger().info("Unknown command from the browser: " + command);
        }
    }

    private void send(String websocketId, String command,
                      Object data) {
        try {
            Optional.ofNullable(liveReload.get(websocketId))
                .ifPresent(producer -> producer.write(Json.encode(new DebugWindowMessage(command, data))));
        } catch (Exception e) {
            getLogger().error("Error sending message", e);
        }
    }


    private void onClose(String websocketId) {
        logger.debug("Live reload connection disconnected for {}", websocketId);
        Optional.ofNullable(liveReload.get(websocketId)).ifPresent(MessageProducer::close);
    }

    public void reload() {
        liveReload.values().stream()
            .filter(Objects::nonNull)
            .forEach(socket -> {
                try {
                    socket.write("{\"command\": \"reload\"}");
                } catch (Exception ex) {
                    // ignore
                }
            });
    }


    private void sendDebugWindowCommand(ServerWebSocket webSocket, String command, Object data) {
        try {
            webSocket.writeTextMessage(Json.encode(new DebugWindowMessage(command, data)));
        } catch (Exception e) {
            logger.error("Error sending message", e);
        }
    }

    private boolean isDebugWindowConnection(ServerWebSocket webSocket) {
        return service.getDeploymentConfiguration().isDevModeLiveReloadEnabled()
            && webSocket.query().contains(ApplicationConstants.DEBUG_WINDOW_CONNECTION);
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
