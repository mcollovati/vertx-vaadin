/*
 * The MIT License
 * Copyright © 2016-2020 Marco Collovati (mcollovati@gmail.com)
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
package com.github.mcollovati.vertx.http;

import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.vaadin.base.devserver.ViteHandler;
import com.vaadin.flow.internal.DevModeHandler;
import com.vaadin.flow.internal.UrlUtil;
import com.vaadin.flow.server.StaticFileServer;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mcollovati.vertx.vaadin.devserver.VertxDevModeHandlerManager;

import static com.vaadin.flow.server.frontend.FrontendUtils.INDEX_HTML;
import static com.vaadin.flow.server.frontend.FrontendUtils.SERVICE_WORKER_SRC_JS;
import static com.vaadin.flow.server.frontend.FrontendUtils.WEB_COMPONENT_HTML;

public class HttpReverseProxy {

    private static final Logger logger = LoggerFactory.getLogger(HttpReverseProxy.class);
    private static final int DEFAULT_TIMEOUT = 120 * 1000;

    private static final String[] FILES_IN_ROOT = new String[] {INDEX_HTML, WEB_COMPONENT_HTML, SERVICE_WORKER_SRC_JS};

    private WebClient client;
    private UnaryOperator<String> uriCustomizer;

    public HttpReverseProxy(CompletableFuture<WebClient> webClientFuture, UnaryOperator<String> uriCustomizer) {
        webClientFuture.whenComplete((wc, err) -> {
            if (err == null) {
                client = wc;
            }
        });
        this.uriCustomizer = uriCustomizer;
    }

    private static UnaryOperator<String> createUriCustomizer(DevModeHandler devModeHandler) {
        boolean isVite = devModeHandler instanceof ViteHandler;
        if (isVite) {
            return path -> {
                if (Stream.of(FILES_IN_ROOT).anyMatch(file -> path.equals("/" + file))) {
                    return "/VAADIN" + path;
                }
                // if ("/index.html".equals(path)) {
                //    return "/VAADIN/index.html";
                // }
                return path;
            };
        }
        return UnaryOperator.identity();
    }

    public static HttpReverseProxy create(Vertx vertx, DevModeHandler devModeHandler) {
        CompletableFuture<WebClient> webClientFuture = VertxDevModeHandlerManager.getDevModeHandlerPort(
                        devModeHandler)
                .thenApply(port -> {
                    logger.debug("Starting DevMode proxy on port {}", port);
                    WebClientOptions options = new WebClientOptions()
                            .setLogActivity(true)
                            .setConnectTimeout(DEFAULT_TIMEOUT)
                            .setIdleTimeout(DEFAULT_TIMEOUT)
                            .setDefaultHost("localhost")
                            .setDefaultPort(port);
                    return WebClient.create(vertx, options);
                });
        return new HttpReverseProxy(webClientFuture, createUriCustomizer(devModeHandler));
    }

    public void forward(RoutingContext routingContext) {
        HttpServerRequest serverRequest = routingContext.request();
        String requestURI = serverRequest.uri();
        if (client == null) {
            logger.debug("DevMode serve not yet started");
            routingContext.next();
        } else if (requestURI.equals("") || requestURI.equals("/")) {
            // Index file must be handled by IndexHtmlRequestHandler
            logger.debug("Not a DevMode server request, Index file must be handled by IndexHtmlRequestHandler");
            routingContext.next();
        } else {

            if (StaticFileServer.APP_THEME_ASSETS_PATTERN.matcher(requestURI).find()) {
                requestURI = "/VAADIN/static" + requestURI;
            }
            requestURI = uriCustomizer.apply(requestURI);

            logger.debug("Forwarding {} to dev-server as {}", serverRequest.uri(), requestURI);

            String devServerRequestPath = UrlUtil.encodeURI(requestURI)
                    + ((serverRequest.query() != null) ? "?" + serverRequest.query() : "");

            HttpRequest<Buffer> clientRequest = client.request(serverRequest.method(), devServerRequestPath);
            serverRequest.headers().forEach(entry -> {
                String valueOk = "Connection".equals(entry.getKey()) ? "close" : entry.getValue();
                clientRequest.putHeader(entry.getKey(), valueOk);
            });

            clientRequest.sendBuffer(routingContext.getBody(), ar -> {
                if (ar.succeeded()) {
                    HttpResponse<Buffer> clientResponse = ar.result();
                    int statusCode = clientResponse.statusCode();
                    HttpServerResponse serverResponse = routingContext.response();
                    if (statusCode == HttpResponseStatus.OK.code()) {
                        logger.debug(
                                "Served resource by dev-server: {} {}",
                                clientResponse.statusCode(),
                                devServerRequestPath);
                        serverResponse.setStatusCode(statusCode).setChunked(true);
                        serverResponse.headers().setAll(clientResponse.headers());
                        serverResponse.end(clientResponse.body());
                    } else if (statusCode == HttpResponseStatus.NOT_FOUND.code()) {
                        logger.debug("Resource not served by dev-server {}", devServerRequestPath);
                        routingContext.next();
                    } else if (statusCode < 400) {
                        logger.debug("dev-server response {} for resource {}", statusCode, devServerRequestPath);
                        serverResponse.headers().setAll(clientResponse.headers());
                        serverResponse.setStatusCode(statusCode).end();
                    } else {
                        logger.debug(
                                "dev-server failed with status {} for resource {}", statusCode, devServerRequestPath);
                        routingContext.fail(statusCode);
                    }
                } else {
                    logger.warn("Request to dev-server failed: {}", devServerRequestPath, ar.cause());
                    routingContext.next();
                }
            });
        }
    }
}
