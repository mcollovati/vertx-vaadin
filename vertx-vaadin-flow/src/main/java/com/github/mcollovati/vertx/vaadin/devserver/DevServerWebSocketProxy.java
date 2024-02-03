package com.github.mcollovati.vertx.vaadin.devserver;

import java.util.function.Supplier;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketConnectOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DevServerWebSocketProxy implements Handler<ServerWebSocket> {

    private static final Logger logger = LoggerFactory.getLogger(DevServerWebSocketProxy.class);

    private static final int DEFAULT_TIMEOUT = 120 * 1000;

    private Supplier<Future<WebSocket>> wsClientFactory;

    private DevServerWebSocketProxy(Supplier<Future<WebSocket>> wsClientFactory) {
        this.wsClientFactory = wsClientFactory;
    }

    @Override
    public void handle(ServerWebSocket browserSocket) {
        new Proxy(browserSocket, wsClientFactory.get());
    }

    private static class Proxy {
        private final ServerWebSocket browserSocket;
        private WebSocket devServerSocket;

        Proxy(ServerWebSocket browserSocket, Future<WebSocket> devServerSocket) {
            Promise<Integer> handshakePromise = Promise.promise();
            this.browserSocket = browserSocket;
            this.browserSocket.setHandshake(handshakePromise.future());
            this.browserSocket.textMessageHandler(this::sendToDevServer);
            this.browserSocket.closeHandler(closeEvent -> this.closeDevServerSession());

            devServerSocket.andThen(ev -> {
                if (ev.succeeded()) {
                    handshakePromise.complete(101);
                    this.devServerSocket = ev.result();
                    this.devServerSocket.textMessageHandler(this::sendToBrowser);
                    this.devServerSocket.closeHandler(unused -> this.closeBrowserSession());
                } else {
                    handshakePromise.fail(ev.cause());
                    logger.error("Cannot connect to Dev Server WebSocket", ev.cause());
                }
            });
        }

        private void sendToBrowser(String message) {
            if (browserSocket != null) {
                browserSocket.writeTextMessage(message, res -> {
                    if (res.succeeded()) {
                        logger.debug("Message sent to browser: {}", message);
                    } else {
                        logger.debug("Error sending message to browser: {}", message, res.cause());
                    }
                });
            } else {
                logger.debug("Cannot forward message to Browser because websocket is not ready: {}", message);
            }
        }

        private void closeBrowserSession() {
            if (browserSocket != null) {
                browserSocket.close(devServerSocket.closeStatusCode(), devServerSocket.closeReason());
            } else {
                logger.debug("Cannot close Browser websocket because it is not ready");
            }
        }

        private void sendToDevServer(String message) {
            if (devServerSocket != null) {
                devServerSocket.writeTextMessage(message, res -> {
                    if (res.succeeded()) {
                        logger.debug("Got message from browser: {}", message);
                    } else {
                        logger.debug("Error sending message to Dev Server: {}", message, res.cause());
                    }
                });
            } else {
                logger.debug("Cannot forward message to Dev Server because websocket is not ready: {}", message);
            }
        }

        private void closeDevServerSession() {
            if (devServerSocket != null) {
                devServerSocket.close();
            } else {
                logger.debug("Cannot close Dev Server websocket because it is not ready");
            }
        }
    }

    public static DevServerWebSocketProxy createWebsocketProxy(Vertx vertx, int port, String path) {
        HttpClientOptions options = new HttpClientOptions()
                .setLogActivity(true)
                .setConnectTimeout(DEFAULT_TIMEOUT)
                .setIdleTimeout(DEFAULT_TIMEOUT)
                .setDefaultHost("localhost")
                .setDefaultPort(port);
        WebSocketConnectOptions wsOptions =
                new WebSocketConnectOptions().addSubProtocol("vite-hmr").setURI(path);
        return new DevServerWebSocketProxy(() -> vertx.createHttpClient(options).webSocket(wsOptions));
    }
}
