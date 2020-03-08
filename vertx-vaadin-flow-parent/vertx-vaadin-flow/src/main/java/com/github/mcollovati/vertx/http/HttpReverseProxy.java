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

import com.github.mcollovati.vertx.vaadin.VertxVaadinRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.vaadin.flow.server.Constants.VAADIN_MAPPING;

public class HttpReverseProxy {

    private final static Logger logger = LoggerFactory.getLogger(HttpReverseProxy.class);
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
        String requestURI = serverRequest.uri().substring(VertxVaadinRequest.extractContextPath(routingContext).length())
            .replace(VAADIN_MAPPING, "");
        logger.debug("Forwarding {}  to webpack as {}" + requestURI, serverRequest.uri());

        serverRequest.pause();
        HttpClientRequest clientRequest = client.request(serverRequest.method(), requestURI, clientResponse -> {

            if (clientResponse.statusCode() == HttpResponseStatus.NOT_FOUND.code()) {
                logger.debug("Resource not served by webpack {}", requestURI);
                routingContext.next();
            } else {
                logger.debug("Served resource by webpack: {} {}}", clientResponse.statusCode(), requestURI);
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
