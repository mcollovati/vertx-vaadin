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
package com.github.mcollovati.vertx.vaadin.connect;

import com.github.mcollovati.vertx.vaadin.VertxVaadinRequest;
import com.github.mcollovati.vertx.vaadin.VertxVaadinService;
import com.github.mcollovati.vertx.vaadin.connect.auth.VertxVaadinConnectAccessChecker;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.connect.EndpointNameChecker;
import com.vaadin.flow.server.connect.ExplicitNullableTypeChecker;
import com.vaadin.flow.server.startup.ApplicationConfiguration;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class VaadinConnectHandler {

    private final VertxVaadinConnectEndpointService endpointService;
    private final VertxVaadinService vaadinService;

    VaadinConnectHandler(VaadinEndpointRegistry endpointRegistry, VertxVaadinService vaadinService) {
        this(new VertxVaadinConnectEndpointService(
                DatabindCodec.mapper(), endpointRegistry, new VertxVaadinConnectAccessChecker(vaadinService.getContext()), new ExplicitNullableTypeChecker()
        ), vaadinService);

    }

    VaadinConnectHandler(VertxVaadinConnectEndpointService service, VertxVaadinService vaadinService) {
        this.endpointService = service;
        this.vaadinService = vaadinService;
    }

    void handle(RoutingContext routingContext) {
        String endpoint = routingContext.request().getParam("endpoint");
        String method = routingContext.request().getParam("method");
        VaadinConnectResponse connectResponse = endpointService.serveEndpoint(endpoint, method, new VertxConnectRequestContext(routingContext));

        HttpServerResponse response = routingContext.response();
        response.setStatusCode(connectResponse.getStatusCode());
        String body = connectResponse.getBody();
        if (body != null) {
            response.end(body);
        } else {
            response.end();
        }
    }

    public static VaadinConnectHandler register(Router router, VertxVaadinService service) {
        VaadinEndpointRegistry endpointRegistry = service.getContext().getAttribute(VaadinEndpointRegistry.class);
        if (endpointRegistry == null) {
            endpointRegistry = new VertxEndpointRegistry(new EndpointNameChecker());
        }
        VaadinConnectHandler connectHandler = new VaadinConnectHandler(endpointRegistry, service);
        connectHandler.register(router);
        return connectHandler;
    }

    public static VaadinConnectHandler register(Router router, VertxVaadinConnectEndpointService service) {
        VaadinConnectHandler connectHandler = new VaadinConnectHandler(service, null);
        connectHandler.register(router);
        return connectHandler;
    }

    private void register(Router router) {
        router.post("/:endpoint/:method")
                .consumes("application/json")
                .produces("application/json")
                .handler(ctx -> {
                    if (vaadinService != null) {
                        VaadinService.setCurrent(vaadinService);
                        CurrentInstance.set(VaadinRequest.class, new VertxVaadinRequest(vaadinService, ctx));
                    }
                    try {
                        handle(ctx);
                    } finally {
                        if (vaadinService != null) {
                            CurrentInstance.clearAll();
                        }
                    }
                });
    }

}
