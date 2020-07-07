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

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;

import com.github.mcollovati.vertx.vaadin.VertxVaadinRequest;
import com.vaadin.flow.server.DevModeHandler;
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

public class HttpReverseProxy {

    private final static Logger logger = LoggerFactory.getLogger(HttpReverseProxy.class);
    private static final int DEFAULT_TIMEOUT = 120 * 1000;


    private WebClient client;

    public HttpReverseProxy(CompletableFuture<WebClient> webClientFuture) {
        webClientFuture.whenComplete((wc, err) -> {
            if (err == null) {
                client = wc;
            }
        });
    }


    public static HttpReverseProxy create(Vertx vertx, DevModeHandler devModeHandler) {
        CompletableFuture<WebClient> webClientFuture = getDevModeHandlerFuture(devModeHandler)
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

        return new HttpReverseProxy(webClientFuture);
    }

    public void forward(RoutingContext routingContext) {
        if (client == null) {
            logger.debug("DevMode serve not yet started");
            routingContext.next();
        } else {

            HttpServerRequest serverRequest = routingContext.request();
            String requestURI = serverRequest.uri().substring(VertxVaadinRequest.extractContextPath(routingContext).length());
            logger.debug("Forwarding {} to webpack as {}", requestURI, serverRequest.uri());


            HttpRequest<Buffer> clientRequest = client.request(serverRequest.method(), requestURI);

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
                        logger.debug("Served resource by webpack: {} {}", clientResponse.statusCode(), requestURI);
                        serverResponse.setStatusCode(statusCode).setChunked(true);
                        serverResponse.headers().setAll(clientResponse.headers());
                        serverResponse.end(clientResponse.body());
                    } else if (statusCode == HttpResponseStatus.NOT_FOUND.code()) {
                        logger.debug("Resource not served by webpack {}", requestURI);
                        routingContext.next();
                    } else if (statusCode < 400) {
                        logger.debug("Webpack response {} for resource {}", statusCode, requestURI);
                        serverResponse.headers().setAll(clientResponse.headers());
                        serverResponse.setStatusCode(statusCode).end();
                    } else {
                        logger.debug("Webpack failed with status {} for resource {}", statusCode, requestURI);
                        routingContext.fail(statusCode);
                    }
                } else {
                    logger.warn("Request to webpack failed: {}", requestURI, ar.cause());
                    routingContext.next();
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    private static CompletableFuture<Integer> getDevModeHandlerFuture(DevModeHandler devModeHandler) {
        try {
            Field devServerStartFuture = DevModeHandler.class.getDeclaredField("devServerStartFuture");
            devServerStartFuture.setAccessible(true);
            return ((CompletableFuture<Void>) devServerStartFuture.get(devModeHandler))
                .thenApply(unused -> devModeHandler.getPort());
        } catch (Exception ex) {
            logger.error("Cannot get DevModHandler future", ex);
            CompletableFuture<Integer> future = new CompletableFuture<>();
            future.completeExceptionally(ex);
            return future;
        }
    }

}
