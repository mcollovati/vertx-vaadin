package com.github.mcollovati.vertx.http;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.eventbus.impl.codecs.JsonObjectMessageCodec;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import static com.vaadin.flow.server.Constants.VAADIN_MAPPING;

public class HttpReverseProxy {

    private static final int DEFAULT_TIMEOUT = 120 * 1000;

    private final HttpClient client;

    public HttpReverseProxy(HttpClient client) {
        this.client = client;
    }


    public static HttpReverseProxy create(Vertx vertx, int devServerPort) {
        HttpClientOptions options = new HttpClientOptions()
            .setLogActivity(true)
            .setConnectTimeout(DEFAULT_TIMEOUT)
            .setIdleTimeout(DEFAULT_TIMEOUT)
            .setDefaultHost("localhost")
            .setDefaultPort(devServerPort);
        return new HttpReverseProxy(vertx.createHttpClient(options));
    }

    public void forward(RoutingContext routingContext) {
        HttpServerRequest serverRequest = routingContext.request();
        String requestURI = serverRequest.uri().replace(VAADIN_MAPPING, "");
        System.out.println("Forwarding " + requestURI + " to webpack");

        serverRequest.pause();
        HttpClientRequest clientRequest = client.request(serverRequest.method(), requestURI);
        clientRequest.handler(clientResponse -> {

            if (clientResponse.statusCode() == HttpResponseStatus.NOT_FOUND.code()) {
                System.out.printf("Resource not served by webpack %s%n", requestURI);
                routingContext.next();
            } else {
                System.out.printf("Served resource by webpack: %d %s%n", clientResponse.statusCode(), requestURI);
                serverRequest.response().setChunked(true);
                serverRequest.response().setStatusCode(clientResponse.statusCode());
                serverRequest.response().headers().setAll(clientResponse.headers());
                clientResponse.handler(data -> serverRequest.response().write(data));
                clientResponse.endHandler(unused -> serverRequest.response().end());
            }
        });
        serverRequest.headers().forEach(entry -> {
            String valueOk = "Connection".equals(entry.getKey()) ? "close" : entry.getValue();
            clientRequest.putHeader(entry.getKey(), valueOk);
        });
        clientRequest.setChunked(true);
        clientRequest.end(routingContext.getBody());
        serverRequest.resume();
    }

}
